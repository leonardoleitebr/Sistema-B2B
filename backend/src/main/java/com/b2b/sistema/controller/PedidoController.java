package com.b2b.sistema.controller;

import com.b2b.sistema.dto.DivergenciaRequestDTO;
import com.b2b.sistema.dto.PedidoRequestDTO;
import com.b2b.sistema.dto.PedidoResponseDTO;
import com.b2b.sistema.dto.PedidoVendedorRequestDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Pedido;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.service.PedidoService;
import com.b2b.sistema.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RF08 a RF14 - Pedidos: criacao (Cliente/Vendedor), acompanhamento, aprovacao,
 * cancelamento e o fluxo de separacao/divergencia/envio da Expedicao.
 *
 * As rotas aqui nao sao bloqueadas por perfil no AutenticacaoInterceptor (todo
 * usuario logado usa alguma parte de "/api/pedidos"); cada acao valida o perfil
 * e a posse do pedido dentro do PedidoService.
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService pedidoService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<PedidoResponseDTO> listar(HttpServletRequest request) {
        Usuario usuarioLogado = usuarioLogado(request);
        return pedidoService.listar(usuarioLogado).stream().map(PedidoResponseDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscar(@PathVariable Long id, HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.buscarPorId(id, usuarioLogado(request)));
    }

    /** RF08 - Cliente finaliza o proprio pedido. */
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@RequestBody PedidoRequestDTO dto, HttpServletRequest request) {
        Usuario usuarioLogado = usuarioLogado(request);
        if (!"ROLE_CLIENTE".equals(usuarioLogado.getRole().getNome())) {
            throw new RegraNegocioException("Somente clientes podem finalizar pedidos por aqui.");
        }
        Pedido salvo = pedidoService.criarPedidoCliente(usuarioLogado, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PedidoResponseDTO(salvo));
    }

    /** RF10 - Vendedor lanca pedido em nome de um cliente da sua base. */
    @PostMapping("/vendedor")
    public ResponseEntity<PedidoResponseDTO> criarComoVendedor(@RequestBody PedidoVendedorRequestDTO dto, HttpServletRequest request) {
        Usuario vendedor = usuarioLogado(request);
        if (!"ROLE_VENDEDOR".equals(vendedor.getRole().getNome())) {
            throw new RegraNegocioException("Somente vendedores podem lancar pedidos em nome de clientes.");
        }
        if (dto.getClienteId() == null) {
            throw new RegraNegocioException("Informe o cliente do pedido.");
        }
        Usuario cliente = usuarioService.buscarPorId(dto.getClienteId());
        Pedido salvo = pedidoService.criarPedidoVendedor(vendedor, cliente, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PedidoResponseDTO(salvo));
    }

    /** RF11 - Administrador aprova o pedido (reserva o estoque necessario). */
    @PatchMapping("/{id}/aprovar")
    public PedidoResponseDTO aprovar(@PathVariable Long id, HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.aprovar(id, usuarioLogado(request)));
    }

    /** RF11 - Administrador (a qualquer momento nao concluido) ou Cliente (so enquanto aguarda aprovacao). */
    @PatchMapping("/{id}/cancelar")
    public PedidoResponseDTO cancelar(@PathVariable Long id, HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.cancelar(id, usuarioLogado(request)));
    }

    /** RF14 - Expedicao inicia a separacao de um pedido aprovado. */
    @PatchMapping("/{id}/iniciar-separacao")
    public PedidoResponseDTO iniciarSeparacao(@PathVariable Long id, HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.iniciarSeparacao(id, usuarioLogado(request)));
    }

    /** RF14 - Expedicao registra uma divergencia encontrada na separacao. */
    @PostMapping("/{id}/divergencias")
    public PedidoResponseDTO registrarDivergencia(@PathVariable Long id, @RequestBody DivergenciaRequestDTO dto,
                                                   HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.registrarDivergencia(id, usuarioLogado(request), dto));
    }

    /** RF14 - Expedicao marca o pedido como enviado (baixa definitiva do estoque). */
    @PatchMapping("/{id}/enviar")
    public PedidoResponseDTO enviar(@PathVariable Long id, HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.enviar(id, usuarioLogado(request)));
    }

    /** Fecha o ciclo do pedido apos a entrega confirmada. */
    @PatchMapping("/{id}/concluir")
    public PedidoResponseDTO concluir(@PathVariable Long id, HttpServletRequest request) {
        return new PedidoResponseDTO(pedidoService.concluir(id, usuarioLogado(request)));
    }

    private Usuario usuarioLogado(HttpServletRequest request) {
        return (Usuario) request.getAttribute("usuarioLogado");
    }
}
