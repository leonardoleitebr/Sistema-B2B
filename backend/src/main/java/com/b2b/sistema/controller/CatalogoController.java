package com.b2b.sistema.controller;

import com.b2b.sistema.dto.CatalogoItemDTO;
import com.b2b.sistema.dto.ProdutoFrequenteDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.service.CatalogoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RF07 - Catalogo com precos personalizados (Cliente/Vendedor).
 * RF13 - Comprar novamente. RF16 - Sugestoes de produtos.
 *
 * O parametro clienteId so e considerado quando quem chama e um Vendedor
 * (para montar o catalogo/pedido em nome de um cliente da sua base - RF10);
 * para um Cliente logado, o catalogo e sempre o dele mesmo, independente do
 * que for enviado no parametro.
 */
@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = "*")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public List<CatalogoItemDTO> catalogo(@RequestParam(required = false) Long clienteId, HttpServletRequest request) {
        Long idParaMontar = resolverClienteId(clienteId, request);
        return catalogoService.montarCatalogo(idParaMontar);
    }

    @GetMapping("/frequentes")
    public List<ProdutoFrequenteDTO> frequentes(@RequestParam(required = false) Long clienteId, HttpServletRequest request) {
        Long idParaMontar = resolverClienteId(clienteId, request);
        return catalogoService.produtosFrequentes(idParaMontar, 8);
    }

    @GetMapping("/sugestoes")
    public List<ProdutoFrequenteDTO> sugestoes(@RequestParam(required = false) Long clienteId, HttpServletRequest request) {
        Long idParaMontar = resolverClienteId(clienteId, request);
        return catalogoService.sugestoes(idParaMontar);
    }

    private Long resolverClienteId(Long clienteIdInformado, HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute("usuarioLogado");
        String perfil = usuarioLogado.getRole().getNome();

        if ("ROLE_CLIENTE".equals(perfil)) {
            return usuarioLogado.getId();
        }
        if ("ROLE_VENDEDOR".equals(perfil) || "ROLE_ADMIN".equals(perfil)) {
            if (clienteIdInformado == null) {
                throw new RegraNegocioException("Informe o cliente (clienteId) para consultar o catalogo.");
            }
            return clienteIdInformado;
        }
        throw new RegraNegocioException("Seu perfil nao tem acesso ao catalogo.");
    }
}
