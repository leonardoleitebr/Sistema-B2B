package com.b2b.sistema.controller;

import com.b2b.sistema.dto.UsuarioResumoDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RF10 - apoio ao fluxo do Vendedor: lista os clientes da sua propria base,
 * para escolher em nome de quem vai montar um pedido.
 */
@RestController
@RequestMapping("/api/vendedor")
@CrossOrigin(origins = "*")
public class VendedorController {

    private final UsuarioService usuarioService;

    public VendedorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/clientes")
    public List<UsuarioResumoDTO> meusClientes(HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute("usuarioLogado");
        if (!"ROLE_VENDEDOR".equals(usuarioLogado.getRole().getNome())) {
            throw new RegraNegocioException("Somente vendedores possuem uma base de clientes.");
        }
        return usuarioService.listarClientesDoVendedor(usuarioLogado.getId()).stream()
                .map(UsuarioResumoDTO::new)
                .collect(Collectors.toList());
    }
}
