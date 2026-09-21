package com.b2b.sistema.controller;

import com.b2b.sistema.dto.DashboardResponseDTO;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * RF15 - Dashboard de indicadores. O frontend calcula o intervalo (Hoje,
 * Ultimos 7 dias, Este mes, Personalizado) e envia inicio/fim prontos.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponseDTO montar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            HttpServletRequest request) {
        Usuario usuarioLogado = (Usuario) request.getAttribute("usuarioLogado");
        return dashboardService.montar(usuarioLogado, inicio, fim);
    }
}
