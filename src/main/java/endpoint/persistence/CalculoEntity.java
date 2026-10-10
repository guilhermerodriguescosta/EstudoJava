package endpoint.persistence;

import endpoint.model.CalculoSalvo;
import endpoint.model.Operacao;
import endpoint.model.ResultadoOperacao;
import jakarta.persistence.*;

@Entity
@Table(name = "calculos")
public class CalculoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private double numero1;
    @Column(nullable = false)
    private double numero2;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Operacao operacao;
    @Column(nullable = false)
    private double resultado;

    protected CalculoEntity() {
    }

    public CalculoEntity(ResultadoOperacao calculo) {
        atualizar(calculo);
    }

    public void atualizar(ResultadoOperacao calculo) {
        numero1 = calculo.numero1();
        numero2 = calculo.numero2();
        operacao = calculo.operacao();
        resultado = calculo.resultado();
    }

    public CalculoSalvo toResponse() {
        return new CalculoSalvo(id, numero1, numero2, operacao, resultado);
    }
}
