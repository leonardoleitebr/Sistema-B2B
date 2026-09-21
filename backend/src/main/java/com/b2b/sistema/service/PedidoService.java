package com.b2b.sistema.service;

import com.b2b.sistema.dto.DivergenciaRequestDTO;
import com.b2b.sistema.dto.PedidoItemRequestDTO;
import com.b2b.sistema.dto.PedidoRequestDTO;
import com.b2b.sistema.dto.PedidoVendedorRequestDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Divergencia;
import com.b2b.sistema.model.ItemPedido;
import com.b2b.sistema.model.Pedido;
import com.b2b.sistema.model.PedidoStatusHistorico;
import com.b2b.sistema.model.Produto;
import com.b2b.sistema.model.StatusPedido;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.DivergenciaRepository;
import com.b2b.sistema.repository.PedidoRepository;
import com.b2b.sistema.repository.PedidoStatusHistoricoRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RF08 - Cliente monta e finaliza pedido.
 * RF09 - Pedido minimo.
 * RF10 - Vendedor lanca pedido em nome do cliente, com limite de desconto.
 * RF11 - Aprovacao/cancelamento pelo Administrador; historico de status.
 * RF14 - Expedicao: separacao, divergencia e envio (baixa real de estoque).
 *
 * Regra de estoque (RF05 + devolutiva do professor): a reserva (quantidadeReservada)
 * acontece na Aprovacao; a baixa definitiva (quantidadeEstoque) acontece no Envio.
 */
@Service
public class PedidoService {

    private static final List<String> PERFIS_QUE_PODEM_VER_TUDO = List.of("ROLE_ADMIN");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoStatusHistoricoRepository historicoRepository;
    private final DivergenciaRepository divergenciaRepository;
    private final CatalogoService catalogoService;
    private final ConfiguracaoService configuracaoService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository,
                          PedidoStatusHistoricoRepository historicoRepository,
                          DivergenciaRepository divergenciaRepository, CatalogoService catalogoService,
                          ConfiguracaoService configuracaoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.historicoRepository = historicoRepository;
        this.divergenciaRepository = divergenciaRepository;
        this.catalogoService = catalogoService;
        this.configuracaoService = configuracaoService;
    }

    /** RF08 - cliente finaliza o proprio pedido. */
    @Transactional
    public Pedido criarPedidoCliente(Usuario cliente, PedidoRequestDTO dto) {
        return montarEPersistirPedido(cliente, null, dto.getItens(), BigDecimal.ZERO);
    }

    /** RF10 - vendedor lanca pedido em nome de um cliente da sua base, podendo aplicar desconto ate o seu limite. */
    @Transactional
    public Pedido criarPedidoVendedor(Usuario vendedor, Usuario cliente, PedidoVendedorRequestDTO dto) {
        if (cliente.getVendedorResponsavel() == null || !cliente.getVendedorResponsavel().getId().equals(vendedor.getId())) {
            throw new RegraNegocioException("Este cliente nao pertence a sua base.");
        }
        BigDecimal desconto = dto.getDescontoPercentual() != null ? dto.getDescontoPercentual() : BigDecimal.ZERO;
        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O desconto nao pode ser negativo.");
        }
        BigDecimal limite = vendedor.getLimiteDescontoPercentual() != null ? vendedor.getLimiteDescontoPercentual() : BigDecimal.ZERO;
        if (desconto.compareTo(limite) > 0) {
            throw new RegraNegocioException("O desconto informado (" + desconto + "%) excede o seu limite autorizado (" + limite + "%).");
        }
        return montarEPersistirPedido(cliente, vendedor, dto.getItens(), desconto);
    }

    private Pedido montarEPersistirPedido(Usuario cliente, Usuario vendedor, List<PedidoItemRequestDTO> itensRequest,
                                           BigDecimal descontoPercentual) {
        if (itensRequest == null || itensRequest.isEmpty()) {
            throw new RegraNegocioException("O pedido precisa ter pelo menos um item.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setVendedor(vendedor);
        pedido.setStatus(StatusPedido.AGUARDANDO_APROVACAO);
        pedido.setDescontoPercentual(descontoPercentual);

        BigDecimal subtotalGeral = BigDecimal.ZERO;
        List<ItemPedido> itens = new ArrayList<>();

        for (PedidoItemRequestDTO itemDto : itensRequest) {
            if (itemDto.getQuantidade() <= 0) {
                throw new RegraNegocioException("A quantidade de cada item deve ser maior que zero.");
            }
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new RegraNegocioException("Produto nao encontrado."));
            if (!produto.isAtivo()) {
                throw new RegraNegocioException("O produto \"" + produto.getNome() + "\" nao esta disponivel.");
            }
            if (produto.getQuantidadeDisponivel() < itemDto.getQuantidade()) {
                throw new RegraNegocioException("Estoque insuficiente para \"" + produto.getNome() + "\" (disponivel: "
                        + produto.getQuantidadeDisponivel() + ").");
            }

            BigDecimal preco = catalogoService.resolverPreco(produto, cliente);
            ItemPedido item = new ItemPedido(pedido, produto, itemDto.getQuantidade(), preco);
            itens.add(item);
            subtotalGeral = subtotalGeral.add(item.getSubtotal());
        }
        pedido.setItens(itens);

        BigDecimal valorTotal = subtotalGeral;
        if (descontoPercentual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fator = BigDecimal.ONE.subtract(descontoPercentual.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            valorTotal = subtotalGeral.multiply(fator).setScale(2, RoundingMode.HALF_UP);
        }
        pedido.setValorTotal(valorTotal);

        // RF09 - pedido minimo
        BigDecimal pedidoMinimo = configuracaoService.obter().getValorPedidoMinimo();
        if (valorTotal.compareTo(pedidoMinimo) < 0) {
            BigDecimal faltante = pedidoMinimo.subtract(valorTotal).setScale(2, RoundingMode.HALF_UP);
            throw new RegraNegocioException("O valor do pedido (R$ " + valorTotal.setScale(2, RoundingMode.HALF_UP)
                    + ") esta abaixo do pedido minimo de R$ " + pedidoMinimo.setScale(2, RoundingMode.HALF_UP)
                    + ". Faltam R$ " + faltante + ".");
        }

        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.AGUARDANDO_APROVACAO, "Pedido criado");
        return salvo;
    }

    /** RF11/RF14 - listagem de pedidos, adaptada ao perfil de quem esta consultando. */
    public List<Pedido> listar(Usuario usuarioLogado) {
        String perfil = usuarioLogado.getRole().getNome();

        return switch (perfil) {
            case "ROLE_ADMIN" -> pedidoRepository.findAllByOrderByDataCriacaoDesc();
            case "ROLE_CLIENTE" -> pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(usuarioLogado.getId());
            case "ROLE_VENDEDOR" -> listarPedidosDoVendedor(usuarioLogado.getId());
            case "ROLE_EXPEDICAO" -> pedidoRepository.findByStatusInOrderByDataCriacaoDesc(
                    List.of(StatusPedido.APROVADO, StatusPedido.EM_SEPARACAO, StatusPedido.DIVERGENCIA,
                            StatusPedido.ENVIADO, StatusPedido.CONCLUIDO));
            default -> List.of();
        };
    }

    private List<Pedido> listarPedidosDoVendedor(Long vendedorId) {
        Map<Long, Pedido> unicos = new LinkedHashMap<>();
        pedidoRepository.findByVendedorIdOrderByDataCriacaoDesc(vendedorId).forEach(p -> unicos.put(p.getId(), p));
        pedidoRepository.findByCliente_VendedorResponsavel_IdOrderByDataCriacaoDesc(vendedorId).forEach(p -> unicos.put(p.getId(), p));
        return unicos.values().stream()
                .sorted((a, b) -> b.getDataCriacao().compareTo(a.getDataCriacao()))
                .toList();
    }

    public Pedido buscarPorId(Long id, Usuario usuarioLogado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Pedido nao encontrado."));
        if (!podeVisualizar(pedido, usuarioLogado)) {
            throw new RegraNegocioException("Voce nao tem permissao para visualizar este pedido.");
        }
        return pedido;
    }

    private boolean podeVisualizar(Pedido pedido, Usuario usuarioLogado) {
        String perfil = usuarioLogado.getRole().getNome();
        return switch (perfil) {
            case "ROLE_ADMIN" -> true;
            case "ROLE_CLIENTE" -> pedido.getCliente().getId().equals(usuarioLogado.getId());
            case "ROLE_VENDEDOR" -> (pedido.getVendedor() != null && pedido.getVendedor().getId().equals(usuarioLogado.getId()))
                    || (pedido.getCliente().getVendedorResponsavel() != null
                        && pedido.getCliente().getVendedorResponsavel().getId().equals(usuarioLogado.getId()));
            case "ROLE_EXPEDICAO" -> pedido.getStatus() != StatusPedido.AGUARDANDO_APROVACAO
                    && pedido.getStatus() != StatusPedido.RASCUNHO && pedido.getStatus() != StatusPedido.CANCELADO;
            default -> false;
        };
    }

    /** RF11 - aprovacao do pedido pelo Administrador: reserva o estoque necessario. */
    @Transactional
    public Pedido aprovar(Long id, Usuario usuarioLogado) {
        exigirPerfil(usuarioLogado, "ROLE_ADMIN");
        Pedido pedido = buscarPorId(id, usuarioLogado);
        exigirStatus(pedido, StatusPedido.AGUARDANDO_APROVACAO);

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (produto.getQuantidadeDisponivel() < item.getQuantidade()) {
                throw new RegraNegocioException("Estoque insuficiente para aprovar: \"" + produto.getNome()
                        + "\" (disponivel: " + produto.getQuantidadeDisponivel() + ", necessario: " + item.getQuantidade() + ").");
            }
        }
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidadeReservada(produto.getQuantidadeReservada() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        pedido.setStatus(StatusPedido.APROVADO);
        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.APROVADO, "Pedido aprovado");
        return salvo;
    }

    /** RF11 - cancelamento: Admin a qualquer momento (nao concluido); Cliente somente enquanto aguarda aprovacao. */
    @Transactional
    public Pedido cancelar(Long id, Usuario usuarioLogado) {
        Pedido pedido = buscarPorId(id, usuarioLogado);
        String perfil = usuarioLogado.getRole().getNome();

        if (pedido.getStatus() == StatusPedido.CANCELADO || pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new RegraNegocioException("Este pedido nao pode mais ser cancelado (status atual: " + pedido.getStatus() + ").");
        }

        if ("ROLE_CLIENTE".equals(perfil)) {
            if (!pedido.getCliente().getId().equals(usuarioLogado.getId())) {
                throw new RegraNegocioException("Voce nao tem permissao para cancelar este pedido.");
            }
            if (pedido.getStatus() != StatusPedido.AGUARDANDO_APROVACAO) {
                throw new RegraNegocioException("So e possivel cancelar enquanto o pedido aguarda aprovacao.");
            }
        } else if (!"ROLE_ADMIN".equals(perfil)) {
            throw new RegraNegocioException("Seu perfil nao tem permissao para cancelar pedidos.");
        }

        // Libera a reserva de estoque, se ja havia sido feita (pedido aprovado ou alem)
        if (pedido.getStatus() == StatusPedido.APROVADO || pedido.getStatus() == StatusPedido.EM_SEPARACAO
                || pedido.getStatus() == StatusPedido.DIVERGENCIA) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeReservada(Math.max(0, produto.getQuantidadeReservada() - item.getQuantidade()));
                produtoRepository.save(produto);
            }
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.CANCELADO, "Pedido cancelado por " + perfil);
        return salvo;
    }

    /** RF14 - expedicao inicia a separacao de um pedido aprovado. */
    @Transactional
    public Pedido iniciarSeparacao(Long id, Usuario usuarioLogado) {
        exigirPerfil(usuarioLogado, "ROLE_EXPEDICAO");
        Pedido pedido = buscarPorId(id, usuarioLogado);
        exigirStatus(pedido, StatusPedido.APROVADO);

        pedido.setStatus(StatusPedido.EM_SEPARACAO);
        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.EM_SEPARACAO, "Separacao iniciada");
        return salvo;
    }

    /** RF14 - expedicao registra uma divergencia encontrada durante a separacao. */
    @Transactional
    public Pedido registrarDivergencia(Long id, Usuario usuarioLogado, DivergenciaRequestDTO dto) {
        exigirPerfil(usuarioLogado, "ROLE_EXPEDICAO");
        Pedido pedido = buscarPorId(id, usuarioLogado);
        if (pedido.getStatus() != StatusPedido.EM_SEPARACAO && pedido.getStatus() != StatusPedido.DIVERGENCIA) {
            throw new RegraNegocioException("Divergencias so podem ser registradas durante a separacao do pedido.");
        }
        if (dto.getDescricao() == null || dto.getDescricao().isBlank()) {
            throw new RegraNegocioException("A descricao da divergencia e obrigatoria.");
        }

        divergenciaRepository.save(new Divergencia(pedido, dto.getDescricao()));
        pedido.setStatus(StatusPedido.DIVERGENCIA);
        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.DIVERGENCIA, dto.getDescricao());
        return salvo;
    }

    /** RF14 - expedicao marca o pedido como enviado: aqui o estoque e efetivamente baixado. */
    @Transactional
    public Pedido enviar(Long id, Usuario usuarioLogado) {
        exigirPerfil(usuarioLogado, "ROLE_EXPEDICAO");
        Pedido pedido = buscarPorId(id, usuarioLogado);
        if (pedido.getStatus() != StatusPedido.EM_SEPARACAO && pedido.getStatus() != StatusPedido.DIVERGENCIA) {
            throw new RegraNegocioException("So e possivel enviar um pedido que esteja em separacao.");
        }

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            int novoEstoque = produto.getQuantidadeEstoque() - item.getQuantidade();
            if (novoEstoque < 0) {
                throw new RegraNegocioException("Estoque insuficiente para enviar \"" + produto.getNome() + "\".");
            }
            produto.setQuantidadeEstoque(novoEstoque);
            produto.setQuantidadeReservada(Math.max(0, produto.getQuantidadeReservada() - item.getQuantidade()));
            produtoRepository.save(produto);
        }

        pedido.setStatus(StatusPedido.ENVIADO);
        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.ENVIADO, "Pedido enviado");
        return salvo;
    }

    /** Fecha o ciclo do pedido apos a entrega. */
    @Transactional
    public Pedido concluir(Long id, Usuario usuarioLogado) {
        String perfil = usuarioLogado.getRole().getNome();
        if (!"ROLE_ADMIN".equals(perfil) && !"ROLE_EXPEDICAO".equals(perfil)) {
            throw new RegraNegocioException("Seu perfil nao tem permissao para concluir pedidos.");
        }
        Pedido pedido = buscarPorId(id, usuarioLogado);
        exigirStatus(pedido, StatusPedido.ENVIADO);

        pedido.setStatus(StatusPedido.CONCLUIDO);
        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, StatusPedido.CONCLUIDO, "Pedido concluido");
        return salvo;
    }

    private void exigirPerfil(Usuario usuario, String perfilEsperado) {
        if (!perfilEsperado.equals(usuario.getRole().getNome())) {
            throw new RegraNegocioException("Seu perfil nao tem permissao para esta acao.");
        }
    }

    private void exigirStatus(Pedido pedido, StatusPedido statusEsperado) {
        if (pedido.getStatus() != statusEsperado) {
            throw new RegraNegocioException("Acao invalida para o status atual do pedido (" + pedido.getStatus() + ").");
        }
    }

    private void registrarHistorico(Pedido pedido, StatusPedido status, String observacao) {
        historicoRepository.save(new PedidoStatusHistorico(pedido, status, observacao));
    }
}
