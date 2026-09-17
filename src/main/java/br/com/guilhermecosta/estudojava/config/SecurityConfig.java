package br.com.guilhermecosta.estudojava.config;

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
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/historico").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(basic -> {})
                .build();
    }

    @Bean
    UserDetailsService usuarios() {
        return new InMemoryUserDetailsManager(
                User.withUsername("usuario").password("{noop}123").roles("USER").build());
    }
}
