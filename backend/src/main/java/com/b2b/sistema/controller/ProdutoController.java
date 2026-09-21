package com.b2b.sistema.controller;

import com.b2b.sistema.dto.ProdutoRequestDTO;
import com.b2b.sistema.dto.ProdutoResponseDTO;
import com.b2b.sistema.model.Produto;
import com.b2b.sistema.service.ProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RF04 - Cadastro e gestao de produtos. RF05 - Controle de estoque.
 * Rotas restritas a ROLE_ADMIN (ver AutenticacaoInterceptor). Para o catalogo
 * visto por Cliente/Vendedor, ver CatalogoController.
 */
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoResponseDTO> listar() {
        return produtoService.listarTodos().stream().map(ProdutoResponseDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscar(@PathVariable Long id) {
        return new ProdutoResponseDTO(produtoService.buscarPorId(id));
    }

    /** RF15 - lista isolada dos produtos com estoque no minimo ou abaixo dele (mesmo alerta do dashboard). */
    @GetMapping("/estoque-baixo")
    public List<ProdutoResponseDTO> estoqueBaixo() {
        return produtoService.buscarComEstoqueBaixo().stream().map(ProdutoResponseDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@RequestBody ProdutoRequestDTO dto) {
        Produto salvo = produtoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProdutoResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(@PathVariable Long id, @RequestBody ProdutoRequestDTO dto) {
        return new ProdutoResponseDTO(produtoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/inativar")
    public ProdutoResponseDTO inativar(@PathVariable Long id) {
        return new ProdutoResponseDTO(produtoService.inativar(id));
    }

    @PatchMapping("/{id}/ativar")
    public ProdutoResponseDTO ativar(@PathVariable Long id) {
        return new ProdutoResponseDTO(produtoService.ativar(id));
    }
}
