package com.b2b.sistema.service;

import com.b2b.sistema.dto.ItemTabelaPrecoRequestDTO;
import com.b2b.sistema.dto.TabelaPrecoRequestDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.ItemTabelaPreco;
import com.b2b.sistema.model.Produto;
import com.b2b.sistema.model.TabelaPreco;
import com.b2b.sistema.repository.ItemTabelaPrecoRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import com.b2b.sistema.repository.TabelaPrecoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * RF06 - Tabela de precos personalizada por cliente.
 */
@Service
public class TabelaPrecoService {

    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final ItemTabelaPrecoRepository itemTabelaPrecoRepository;
    private final ProdutoRepository produtoRepository;

    public TabelaPrecoService(TabelaPrecoRepository tabelaPrecoRepository,
                               ItemTabelaPrecoRepository itemTabelaPrecoRepository,
                               ProdutoRepository produtoRepository) {
        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.itemTabelaPrecoRepository = itemTabelaPrecoRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<TabelaPreco> listarTodas() {
        return tabelaPrecoRepository.findAll();
    }

    public TabelaPreco buscarPorId(Long id) {
        return tabelaPrecoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Tabela de precos nao encontrada."));
    }

    public TabelaPreco cadastrar(TabelaPrecoRequestDTO dto) {
        validarNome(dto.getNome());
        if (tabelaPrecoRepository.existsByNome(dto.getNome())) {
            throw new RegraNegocioException("Ja existe uma tabela de precos com este nome.");
        }
        return tabelaPrecoRepository.save(new TabelaPreco(dto.getNome()));
    }

    public TabelaPreco atualizar(Long id, TabelaPrecoRequestDTO dto) {
        TabelaPreco tabelaPreco = buscarPorId(id);
        validarNome(dto.getNome());
        if (tabelaPrecoRepository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new RegraNegocioException("Ja existe uma tabela de precos com este nome.");
        }
        tabelaPreco.setNome(dto.getNome());
        return tabelaPrecoRepository.save(tabelaPreco);
    }

    public void remover(Long id) {
        TabelaPreco tabelaPreco = buscarPorId(id);
        if (!tabelaPreco.getClientes().isEmpty()) {
            throw new RegraNegocioException("Esta tabela esta vinculada a clientes e nao pode ser removida.");
        }
        tabelaPrecoRepository.delete(tabelaPreco);
    }

    /** RF06 - define (ou atualiza) o preco de um produto especifico dentro da tabela. */
    public ItemTabelaPreco definirPreco(Long tabelaPrecoId, ItemTabelaPrecoRequestDTO dto) {
        TabelaPreco tabelaPreco = buscarPorId(tabelaPrecoId);

        if (dto.getPreco() == null || dto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O preco deve ser maior que zero.");
        }
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RegraNegocioException("Produto nao encontrado."));

        ItemTabelaPreco item = itemTabelaPrecoRepository
                .findByTabelaPrecoIdAndProdutoId(tabelaPrecoId, produto.getId())
                .orElseGet(() -> new ItemTabelaPreco(tabelaPreco, produto, dto.getPreco()));
        item.setPreco(dto.getPreco());
        return itemTabelaPrecoRepository.save(item);
    }

    /** Remove o preco personalizado de um produto na tabela (volta a usar o preco padrao do produto). */
    public void removerPreco(Long tabelaPrecoId, Long produtoId) {
        ItemTabelaPreco item = itemTabelaPrecoRepository.findByTabelaPrecoIdAndProdutoId(tabelaPrecoId, produtoId)
                .orElseThrow(() -> new RegraNegocioException("Este produto nao tem preco definido nesta tabela."));
        itemTabelaPrecoRepository.delete(item);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da tabela de precos e obrigatorio.");
        }
    }
}
