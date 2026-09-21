package com.b2b.sistema.controller;

import com.b2b.sistema.dto.ItemTabelaPrecoRequestDTO;
import com.b2b.sistema.dto.ItemTabelaPrecoResponseDTO;
import com.b2b.sistema.dto.TabelaPrecoRequestDTO;
import com.b2b.sistema.dto.TabelaPrecoResponseDTO;
import com.b2b.sistema.service.TabelaPrecoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RF06 - Tabela de precos personalizada. Rotas restritas a ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/tabelas-precos")
@CrossOrigin(origins = "*")
public class TabelaPrecoController {

    private final TabelaPrecoService tabelaPrecoService;

    public TabelaPrecoController(TabelaPrecoService tabelaPrecoService) {
        this.tabelaPrecoService = tabelaPrecoService;
    }

    @GetMapping
    public List<TabelaPrecoResponseDTO> listar() {
        return tabelaPrecoService.listarTodas().stream().map(TabelaPrecoResponseDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TabelaPrecoResponseDTO buscar(@PathVariable Long id) {
        return new TabelaPrecoResponseDTO(tabelaPrecoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TabelaPrecoResponseDTO> cadastrar(@RequestBody TabelaPrecoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new TabelaPrecoResponseDTO(tabelaPrecoService.cadastrar(dto)));
    }

    @PutMapping("/{id}")
    public TabelaPrecoResponseDTO atualizar(@PathVariable Long id, @RequestBody TabelaPrecoRequestDTO dto) {
        return new TabelaPrecoResponseDTO(tabelaPrecoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        tabelaPrecoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/itens")
    public ItemTabelaPrecoResponseDTO definirPreco(@PathVariable Long id, @RequestBody ItemTabelaPrecoRequestDTO dto) {
        return new ItemTabelaPrecoResponseDTO(tabelaPrecoService.definirPreco(id, dto));
    }

    @DeleteMapping("/{id}/itens/{produtoId}")
    public ResponseEntity<Void> removerPreco(@PathVariable Long id, @PathVariable Long produtoId) {
        tabelaPrecoService.removerPreco(id, produtoId);
        return ResponseEntity.noContent().build();
    }
}
