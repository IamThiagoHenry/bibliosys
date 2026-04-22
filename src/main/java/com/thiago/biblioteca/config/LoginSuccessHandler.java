package com.thiago.biblioteca.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redireciona o usuário para a área correta após o login:
 *  - BIBLIOTECARIO → dashboard admin (/)
 *  - LEITOR        → dashboard do leitor (/leitor/dashboard)
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        boolean isBibliotecario = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIO"));

        String destino = isBibliotecario ? "/" : "/leitor/dashboard";
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, destino);
    }
}
