package com.b2b.sistema.service;

import com.b2b.sistema.dto.CatalogoItemDTO;
import com.b2b.sistema.dto.ProdutoFrequenteDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.ItemPedido;
import com.b2b.sistema.model.ItemTabelaPreco;
import com.b2b.sistema.model.Produto;
import com.b2b.sistema.model.StatusPedido;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.ItemPedidoRepository;
import com.b2b.sistema.repository.ItemTabelaPrecoRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import com.b2b.sistema.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RF07 - Catalogo com precos personalizados.
 * RF13 - Produtos frequentes ("comprar novamente").
 * RF16 - Sugestoes de produtos para completar o pedido minimo.
 */
@Service
public class CatalogoService {

    private static final int LIMITE_SUGESTOES = 6;

    private final ProdutoRepository produtoRepository;
    private final ItemTabelaPrecoRepository itemTabelaPrecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public CatalogoService(ProdutoRepository produtoRepository, ItemTabelaPrecoRepository itemTabelaPrecoRepository,
                            UsuarioRepository usuarioRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.itemTabelaPrecoRepository = itemTabelaPrecoRepository;
        this.usuarioRepository = usuarioRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /** RF07 - lista o catalogo com o preco ja resolvido para o cliente informado. */
    public List<CatalogoItemDTO> montarCatalogo(Long clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RegraNegocioException("Cliente nao encontrado."));

        return produtoRepository.findByAtivoTrue().stream()
                .map(produto -> montarItem(produto, cliente))
                .collect(Collectors.toList());
    }

    /** RN01 - preco de um produto para um cliente: o da tabela dele, se existir; senao o preco padrao. */
    public BigDecimal resolverPreco(Produto produto, Usuario cliente) {
        if (cliente.getTabelaPreco() != null) {
            Optional<ItemTabelaPreco> itemPersonalizado = itemTabelaPrecoRepository
                    .findByTabelaPrecoIdAndProdutoId(cliente.getTabelaPreco().getId(), produto.getId());
            if (itemPersonalizado.isPresent()) {
                return itemPersonalizado.get().getPreco();
            }
        }
        return produto.getPrecoPadrao();
    }

    private CatalogoItemDTO montarItem(Produto produto, Usuario cliente) {
        BigDecimal preco = produto.getPrecoPadrao();
        boolean precoPersonalizado = false;

        if (cliente.getTabelaPreco() != null) {
            Optional<ItemTabelaPreco> itemPersonalizado = itemTabelaPrecoRepository
                    .findByTabelaPrecoIdAndProdutoId(cliente.getTabelaPreco().getId(), produto.getId());
            if (itemPersonalizado.isPresent()) {
                preco = itemPersonalizado.get().getPreco();
                precoPersonalizado = true;
            }
        }

        return new CatalogoItemDTO(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria().getNome(),
                produto.getUnidadeVenda(),
                preco,
                precoPersonalizado,
                produto.isAtivo() && produto.getQuantidadeDisponivel() > 0,
                produto.getQuantidadeDisponivel(),
                produto.isDestaque()
        );
    }

    /** RF13 - produtos que o cliente ja comprou, do mais frequente para o menos frequente. */
    public List<ProdutoFrequenteDTO> produtosFrequentes(Long clienteId, int limite) {
        List<ItemPedido> historico = itemPedidoRepository
                .findByPedido_ClienteIdAndPedido_StatusNotOrderByPedido_DataCriacaoDesc(clienteId, StatusPedido.CANCELADO);

        // Como a lista vem ordenada da compra mais recente para a mais antiga, a primeira
        // ocorrencia de cada produto ja e a "ultima quantidade comprada" (RF13 R2).
        Map<Long, ProdutoFrequenteAcumulado> porProduto = new LinkedHashMap<>();
        for (ItemPedido item : historico) {
            Produto produto = item.getProduto();
            ProdutoFrequenteAcumulado acumulado = porProduto.computeIfAbsent(produto.getId(),
                    idProduto -> new ProdutoFrequenteAcumulado(produto, item.getQuantidade()));
            acumulado.vezesComprado++;
        }

        return porProduto.values().stream()
                .sorted((a, b) -> Integer.compare(b.vezesComprado, a.vezesComprado))
                .limit(limite)
                .map(a -> new ProdutoFrequenteDTO(
                        a.produto.getId(), a.produto.getNome(), a.produto.getUnidadeVenda(),
                        a.produto.getPrecoPadrao(), a.quantidadeUltimaCompra, a.vezesComprado,
                        a.produto.isAtivo() && a.produto.getQuantidadeDisponivel() > 0))
                .collect(Collectors.toList());
    }

    /** RF16 - une produtos frequentes do cliente com produtos em destaque, para sugerir ao faltar valor para o pedido minimo. */
    public List<ProdutoFrequenteDTO> sugestoes(Long clienteId) {
        List<ProdutoFrequenteDTO> frequentes = produtosFrequentes(clienteId, LIMITE_SUGESTOES);
        List<Long> idsJaSugeridos = frequentes.stream().map(ProdutoFrequenteDTO::getProdutoId).collect(Collectors.toList());

        List<ProdutoFrequenteDTO> destaques = produtoRepository.findByDestaqueTrueAndAtivoTrue().stream()
                .filter(p -> !idsJaSugeridos.contains(p.getId()))
                .map(p -> new ProdutoFrequenteDTO(p.getId(), p.getNome(), p.getUnidadeVenda(), p.getPrecoPadrao(),
                        0, 0, p.getQuantidadeDisponivel() > 0))
                .collect(Collectors.toList());

        frequentes.addAll(destaques);
        return frequentes;
    }

    private static class ProdutoFrequenteAcumulado {
        final Produto produto;
        final int quantidadeUltimaCompra;
        int vezesComprado = 0;

        ProdutoFrequenteAcumulado(Produto produto, int quantidadeUltimaCompra) {
            this.produto = produto;
            this.quantidadeUltimaCompra = quantidadeUltimaCompra;
        }
    }
}
