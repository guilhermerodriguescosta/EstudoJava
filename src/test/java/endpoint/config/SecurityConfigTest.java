package endpoint.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

class SecurityConfigTest {
    @Configuration
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class ConfiguracaoTeste {}

    @Test
    void deveAutenticarPeloSpringSecurityComBCrypt() throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("senhas-teste", Map.of(
                    "APP_USER_PASSWORD", "senha-user-teste",
                    "APP_ADMIN_PASSWORD", "senha-admin-teste",
                    "APP_LIMITED_PASSWORD", "senha-limitado-teste")));
            context.register(ConfiguracaoTeste.class);
            context.refresh();
            var manager = context.getBean(AuthenticationConfiguration.class).getAuthenticationManager();
            var autenticacao = manager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated("usuario", "senha-user-teste"));
            assertTrue(autenticacao.isAuthenticated());
            assertThrows(BadCredentialsException.class, () -> manager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated("usuario", "123")));
        }
    }

    @Test
    void deveUsarSenhasFornecidasComHashEMantemPapeis() {
        var usuarios = new SecurityConfig().usuarios(new SecurityConfig().passwordEncoder(), "senha-user-teste", "senha-admin-teste", "senha-limitado-teste");
        var encoder = new BCryptPasswordEncoder();
        String[] nomes = {"usuario", "admin", "limitado"};
        String[] senhas = {"senha-user-teste", "senha-admin-teste", "senha-limitado-teste"};
        String[] papeis = {"ROLE_USER", "ROLE_ADMIN", "ROLE_USER"};
        for (int i = 0; i < nomes.length; i++) {
            var usuario = usuarios.loadUserByUsername(nomes[i]);
            assertTrue(encoder.matches(senhas[i], usuario.getPassword()));
            assertFalse(encoder.matches("123", usuario.getPassword()));
            assertEquals(papeis[i], usuario.getAuthorities().iterator().next().getAuthority());
        }
    }

    @Test
    void deveRejeitarSenhaVaziaEmQualquerConta() {
        var config = new SecurityConfig();
        assertThrows(IllegalArgumentException.class, () -> config.usuarios(config.passwordEncoder(), "", "admin", "limitado"));
        assertThrows(IllegalArgumentException.class, () -> config.usuarios(config.passwordEncoder(), "user", " ", "limitado"));
        assertThrows(IllegalArgumentException.class, () -> config.usuarios(config.passwordEncoder(), "user", "admin", ""));
    }
}
