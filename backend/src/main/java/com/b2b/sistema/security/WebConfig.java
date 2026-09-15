package com.b2b.sistema.security;

import com.b2b.sistema.repository.UsuarioRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra o AutenticacaoInterceptor para todas as rotas "/api/**",
 * liberando apenas "/api/auth/**" (login/logout), que precisa ficar
 * acessivel sem token.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public WebConfig(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AutenticacaoInterceptor(tokenService, usuarioRepository))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}
