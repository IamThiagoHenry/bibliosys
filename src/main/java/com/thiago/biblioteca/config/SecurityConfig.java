package com.thiago.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           LoginSuccessHandler loginSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ── Rotas públicas ──────────────────────────────────────
                .requestMatchers("/login", "/cadastro", "/esqueci-senha",
                                 "/css/**", "/js/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/suporte/novo").permitAll()
                .requestMatchers(HttpMethod.POST, "/suporte/salvar").permitAll()

                // ── Área do LEITOR ──────────────────────────────────────
                .requestMatchers("/leitor/**",
                                 "/meus-emprestimos",
                                 "/minhas-reservas", "/minhas-reservas/**",
                                 "/catalogo", "/catalogo/**").hasRole("LEITOR")

                // ── Área exclusiva do BIBLIOTECARIO ─────────────────────
                .requestMatchers("/suporte", "/suporte/resolver/**",
                                 "/suporte/deletar/**", "/suporte/responder/**",
                                 "/suporte/ver/**").hasRole("BIBLIOTECARIO")
                .requestMatchers("/usuarios", "/usuarios/**").hasRole("BIBLIOTECARIO")

                // ── Demais rotas: qualquer usuário autenticado ───────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(loginSuccessHandler)   // redireciona por role
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/logout", "/suporte/salvar")
            )
            // 403: LEITOR vai para seu dashboard; BIBLIOTECARIO vai para /
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, denied) -> {
                    var auth = request.getUserPrincipal();
                    if (auth != null && request.isUserInRole("LEITOR")) {
                        response.sendRedirect("/leitor/dashboard");
                    } else {
                        response.sendRedirect("/");
                    }
                })
            );

        return http.build();
    }
}
