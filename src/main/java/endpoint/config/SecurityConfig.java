package endpoint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        authorize -> authorize.requestMatchers(HttpMethod.DELETE, "/historico/**")
                                .hasRole("ADMIN").requestMatchers(HttpMethod.PUT, "/historico/**")
                                .hasRole("ADMIN").requestMatchers(HttpMethod.POST, "/historico")
                                .authenticated().anyRequest().permitAll())
                .httpBasic(basic -> {
                }).build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService usuarios(
            PasswordEncoder encoder,
            @Value("${APP_USER_PASSWORD}") String senhaUsuario,
            @Value("${APP_ADMIN_PASSWORD}") String senhaAdmin,
            @Value("${APP_LIMITED_PASSWORD}") String senhaLimitado) {
        if (senhaUsuario.isBlank() || senhaAdmin.isBlank() || senhaLimitado.isBlank()) {
            throw new IllegalArgumentException("Configure senhas nao vazias para as tres contas.");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername("usuario").password(encoder.encode(senhaUsuario)).roles("USER").build(),
                User.withUsername("admin").password(encoder.encode(senhaAdmin)).roles("ADMIN").build(),
                User.withUsername("limitado").password(encoder.encode(senhaLimitado)).roles("USER").build());
    }
}
