package com.b2b.sistema.service;

import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Categoria;
import com.b2b.sistema.repository.CategoriaRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RF03 - Cadastro e gestao de categorias de produtos.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Categoria nao encontrada."));
    }

    public Categoria cadastrar(Categoria categoria) {
        validarNome(categoria.getNome());
        if (categoriaRepository.existsByNome(categoria.getNome())) {
            throw new RegraNegocioException("Ja existe uma categoria com este nome.");
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Long id, Categoria dadosAtualizados) {
        Categoria categoria = buscarPorId(id);
        validarNome(dadosAtualizados.getNome());
        if (categoriaRepository.existsByNomeAndIdNot(dadosAtualizados.getNome(), id)) {
            throw new RegraNegocioException("Ja existe uma categoria com este nome.");
        }
        categoria.setNome(dadosAtualizados.getNome());
        return categoriaRepository.save(categoria);
    }

    /** RF03 R3 - uma categoria com produtos vinculados nao pode ser removida. */
    public void remover(Long id) {
        Categoria categoria = buscarPorId(id);
        if (produtoRepository.existsByCategoriaId(categoria.getId())) {
            throw new RegraNegocioException("Esta categoria possui produtos vinculados e nao pode ser removida.");
        }
        categoriaRepository.delete(categoria);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da categoria e obrigatorio.");
        }
    }
}
