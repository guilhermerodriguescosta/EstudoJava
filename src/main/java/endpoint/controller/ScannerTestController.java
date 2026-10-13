package endpoint.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Fixture intencionalmente vulneravel para validar o Total Scanner. */
@RestController
@Profile("scanner-test")
public class ScannerTestController {
    private final DataSource dataSource;

    public ScannerTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/scanner-test/pedidos")
    public List<String> buscarPedidos(@RequestParam String descricao) throws SQLException {
        // CWE-89: entrada HTTP concatenada em SQL, exclusivamente para teste de SAST.
        String sql = "SELECT descricao FROM pedidos WHERE descricao = '" + descricao + "'";
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            List<String> pedidos = new ArrayList<>();
            while (resultSet.next()) {
                pedidos.add(resultSet.getString("descricao"));
            }
            return pedidos;
        }
    }
}
