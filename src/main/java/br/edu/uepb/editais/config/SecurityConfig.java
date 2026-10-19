package br.edu.uepb.editais.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // permite usar @PreAuthorize("hasRole('PROFESSOR')") nos controllers
public class SecurityConfig {

    /** Regras principais de acesso do site. */
    @Bean
    @Order(2)
    SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // páginas públicas
                .requestMatchers("/", "/login", "/cadastro", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                // áreas restritas por perfil
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // todo o resto (inclusive o fórum /forum/**) exige login.
                // Quem pode publicar vagas é decidido no PostagemService, pela categoria.
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .defaultSuccessUrl("/painel", false)
                .failureUrl("/login?erro")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?saiu")
                .permitAll());
        return http.build();
    }

    /** Libera o console do H2 (http://localhost:8080/h2-console) somente no perfil dev. */
    @Bean
    @Order(1)
    @Profile("dev")
    SecurityFilterChain h2ConsoleSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/h2-console/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    /** BCrypt por padrão, com prefixo {bcrypt} para permitir trocar de algoritmo no futuro. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
