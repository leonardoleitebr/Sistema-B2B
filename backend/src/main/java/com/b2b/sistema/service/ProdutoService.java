package com.b2b.sistema.service;

import com.b2b.sistema.dto.ProdutoRequestDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Categoria;
import com.b2b.sistema.model.Produto;
import com.b2b.sistema.repository.CategoriaRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * RF04 - Cadastro e gestao de produtos.
 * RF05 - Controle de estoque.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Produto nao encontrado."));
    }

    public Produto cadastrar(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        aplicarDados(produto, dto);
        produto.setAtivo(true);
        produto.setQuantidadeReservada(0);
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = buscarPorId(id);
        aplicarDados(produto, dto);
        return produtoRepository.save(produto);
    }

    private void aplicarDados(Produto produto, ProdutoRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new RegraNegocioException("O nome do produto e obrigatorio.");
        }
        if (dto.getCategoriaId() == null) {
            throw new RegraNegocioException("A categoria do produto e obrigatoria.");
        }
        if (dto.getUnidadeVenda() == null || dto.getUnidadeVenda().isBlank()) {
            throw new RegraNegocioException("A unidade de venda e obrigatoria.");
        }
        if (dto.getPrecoPadrao() == null || dto.getPrecoPadrao().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O preco padrao deve ser maior que zero.");
        }
        if (dto.getQuantidadeEstoque() == null || dto.getQuantidadeEstoque() < 0) {
            // RF05 R1 - a quantidade em estoque nao pode ser negativa
            throw new RegraNegocioException("A quantidade em estoque nao pode ser negativa.");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RegraNegocioException("Categoria nao encontrada."));

        produto.setNome(dto.getNome());
        produto.setCategoria(categoria);
        produto.setUnidadeVenda(dto.getUnidadeVenda());
        produto.setPrecoPadrao(dto.getPrecoPadrao());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setEstoqueMinimo(dto.getEstoqueMinimo() != null ? Math.max(dto.getEstoqueMinimo(), 0) : 0);
        produto.setDestaque(dto.isDestaque());
    }

    /** RF05 R2 - produto com estoque zerado (indisponivel) deve poder ser sinalizado/inativado manualmente tambem. */
    public Produto inativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(false);
        return produtoRepository.save(produto);
    }

    public Produto ativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(true);
        return produtoRepository.save(produto);
    }

    public List<Produto> buscarComEstoqueBaixo() {
        return produtoRepository.buscarComEstoqueBaixo();
    }
}
