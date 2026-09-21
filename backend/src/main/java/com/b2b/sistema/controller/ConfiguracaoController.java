package com.b2b.sistema.controller;

import com.b2b.sistema.dto.ConfiguracaoRequestDTO;
import com.b2b.sistema.dto.ConfiguracaoResponseDTO;
import com.b2b.sistema.service.ConfiguracaoService;
import org.springframework.web.bind.annotation.*;

/**
 * RF09 R1 - pedido minimo configuravel pelo Administrador.
 * RF15 - dias de inatividade do cliente, usados no dashboard.
 * GET e liberado para qualquer usuario logado (o Cliente precisa saber o pedido
 * minimo vigente); PUT e restrito a ROLE_ADMIN (ver AutenticacaoInterceptor).
 */
@RestController
@RequestMapping("/api/configuracoes")
@CrossOrigin(origins = "*")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    public ConfiguracaoController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @GetMapping
    public ConfiguracaoResponseDTO obter() {
        return new ConfiguracaoResponseDTO(configuracaoService.obter());
    }

    @PutMapping
    public ConfiguracaoResponseDTO atualizar(@RequestBody ConfiguracaoRequestDTO dto) {
        return new ConfiguracaoResponseDTO(
                configuracaoService.atualizar(dto.getValorPedidoMinimo(), dto.getDiasInatividadeCliente()));
    }
}
