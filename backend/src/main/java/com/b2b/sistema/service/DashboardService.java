package com.b2b.sistema.service;

import com.b2b.sistema.dto.ClienteInativoDTO;
import com.b2b.sistema.dto.DashboardResponseDTO;
import com.b2b.sistema.dto.ProdutoResponseDTO;
import com.b2b.sistema.dto.ProdutoVendidoDTO;
import com.b2b.sistema.dto.StatusContagemDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.ItemPedido;
import com.b2b.sistema.model.Pedido;
import com.b2b.sistema.model.StatusPedido;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.ItemPedidoRepository;
import com.b2b.sistema.repository.PedidoRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import com.b2b.sistema.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RF15 - Dashboard de indicadores. O Administrador ve os dados globais; o
 * Vendedor ve os mesmos indicadores, mas restritos a sua propria base de
 * clientes (produtosEstoqueBaixo fica vazio para ele, pois estoque e uma
 * visao administrativa/operacional, nao comercial).
 */
@Service
public class DashboardService {

    private static final int LIMITE_PRODUTOS_MAIS_VENDIDOS = 5;

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoService configuracaoService;

    public DashboardService(PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository,
                             ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository,
                             ConfiguracaoService configuracaoService) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracaoService = configuracaoService;
    }

    public DashboardResponseDTO montar(Usuario usuarioLogado, LocalDateTime inicio, LocalDateTime fim) {
        String perfil = usuarioLogado.getRole().getNome();
        boolean admin = "ROLE_ADMIN".equals(perfil);
        Long vendedorId = "ROLE_VENDEDOR".equals(perfil) ? usuarioLogado.getId() : null;
        if (!admin && vendedorId == null) {
            throw new RegraNegocioException("Seu perfil nao tem acesso ao dashboard.");
        }

        List<Pedido> pedidosPeriodo = admin
                ? pedidoRepository.findByDataCriacaoBetweenAndStatusNot(inicio, fim, StatusPedido.CANCELADO)
                : pedidoRepository.findByDataCriacaoBetweenAndStatusNotAndCliente_VendedorResponsavel_Id(
                        inicio, fim, StatusPedido.CANCELADO, vendedorId);

        long totalPedidos = pedidosPeriodo.size();
        BigDecimal faturamentoTotal = pedidosPeriodo.stream()
                .map(Pedido::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticketMedio = totalPedidos > 0
                ? faturamentoTotal.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<StatusPedido, Long> contagemPorStatus = pedidosPeriodo.stream()
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));
        List<StatusContagemDTO> pedidosPorStatus = Arrays.stream(StatusPedido.values())
                .map(s -> new StatusContagemDTO(s.name(), contagemPorStatus.getOrDefault(s, 0L)))
                .collect(Collectors.toList());

        List<ProdutoVendidoDTO> produtosMaisVendidos = calcularProdutosMaisVendidos(inicio, fim, admin, vendedorId);

        List<ProdutoResponseDTO> produtosEstoqueBaixo = admin
                ? produtoRepository.buscarComEstoqueBaixo().stream().map(ProdutoResponseDTO::new).collect(Collectors.toList())
                : List.of();

        List<ClienteInativoDTO> clientesInativos = calcularClientesInativos(admin, vendedorId);

        return new DashboardResponseDTO(totalPedidos, faturamentoTotal, ticketMedio, pedidosPorStatus,
                produtosMaisVendidos, produtosEstoqueBaixo, clientesInativos);
    }

    private List<ProdutoVendidoDTO> calcularProdutosMaisVendidos(LocalDateTime inicio, LocalDateTime fim,
                                                                   boolean admin, Long vendedorId) {
        List<ItemPedido> itensPeriodo = itemPedidoRepository
                .findByPedido_DataCriacaoBetweenAndPedido_StatusNot(inicio, fim, StatusPedido.CANCELADO);

        if (!admin) {
            itensPeriodo = itensPeriodo.stream()
                    .filter(ip -> pertenceAoVendedor(ip.getPedido(), vendedorId))
                    .collect(Collectors.toList());
        }

        Map<Long, Long> quantidadePorProdutoId = itensPeriodo.stream()
                .collect(Collectors.groupingBy(ip -> ip.getProduto().getId(),
                        Collectors.summingLong(ItemPedido::getQuantidade)));
        Map<Long, String> nomePorProdutoId = itensPeriodo.stream()
                .collect(Collectors.toMap(ip -> ip.getProduto().getId(), ip -> ip.getProduto().getNome(), (a, b) -> a));

        return quantidadePorProdutoId.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(LIMITE_PRODUTOS_MAIS_VENDIDOS)
                .map(e -> new ProdutoVendidoDTO(e.getKey(), nomePorProdutoId.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    private boolean pertenceAoVendedor(Pedido pedido, Long vendedorId) {
        return pedido.getCliente().getVendedorResponsavel() != null
                && pedido.getCliente().getVendedorResponsavel().getId().equals(vendedorId);
    }

    /** RF15 - clientes sem comprar ha mais de "diasInatividadeCliente" (configuravel). */
    private List<ClienteInativoDTO> calcularClientesInativos(boolean admin, Long vendedorId) {
        List<Usuario> clientes = usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_CLIENTE".equals(u.getRole().getNome()))
                .filter(u -> admin || (u.getVendedorResponsavel() != null && u.getVendedorResponsavel().getId().equals(vendedorId)))
                .toList();

        int diasInatividade = configuracaoService.obter().getDiasInatividadeCliente();
        LocalDateTime limite = LocalDateTime.now().minusDays(diasInatividade);

        List<ClienteInativoDTO> inativos = new ArrayList<>();
        for (Usuario cliente : clientes) {
            Optional<Pedido> ultimoPedidoValido = pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(cliente.getId())
                    .stream().filter(p -> p.getStatus() != StatusPedido.CANCELADO).findFirst();
            LocalDateTime ultimaCompra = ultimoPedidoValido.map(Pedido::getDataCriacao).orElse(null);
            if (ultimaCompra == null || ultimaCompra.isBefore(limite)) {
                inativos.add(new ClienteInativoDTO(cliente.getId(), cliente.getNome(), ultimaCompra));
            }
        }
        return inativos;
    }
}
