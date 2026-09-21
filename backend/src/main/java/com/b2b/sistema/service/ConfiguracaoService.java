package com.b2b.sistema.service;

import com.b2b.sistema.model.Configuracao;
import com.b2b.sistema.repository.ConfiguracaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * RF09 R1 - valor de pedido minimo configuravel pelo Administrador.
 * RF15 - dias de inatividade usados para sinalizar clientes sem comprar ha muito tempo.
 */
@Service
public class ConfiguracaoService {

    private static final BigDecimal PEDIDO_MINIMO_PADRAO = BigDecimal.valueOf(200);
    private static final int DIAS_INATIVIDADE_PADRAO = 30;

    private final ConfiguracaoRepository configuracaoRepository;

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository) {
        this.configuracaoRepository = configuracaoRepository;
    }

    public Configuracao obter() {
        return configuracaoRepository.findById(1L)
                .orElseGet(() -> configuracaoRepository.save(
                        new Configuracao(PEDIDO_MINIMO_PADRAO, DIAS_INATIVIDADE_PADRAO)));
    }

    public Configuracao atualizar(BigDecimal valorPedidoMinimo, Integer diasInatividadeCliente) {
        Configuracao configuracao = obter();
        if (valorPedidoMinimo != null && valorPedidoMinimo.compareTo(BigDecimal.ZERO) >= 0) {
            configuracao.setValorPedidoMinimo(valorPedidoMinimo);
        }
        if (diasInatividadeCliente != null && diasInatividadeCliente > 0) {
            configuracao.setDiasInatividadeCliente(diasInatividadeCliente);
        }
        return configuracaoRepository.save(configuracao);
    }
}
