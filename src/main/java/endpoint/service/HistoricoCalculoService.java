package endpoint.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import endpoint.exception.CalculoDuplicadoException;
import endpoint.model.CalculoSalvo;
import endpoint.model.ResultadoOperacao;
import endpoint.repository.CalculoRepository;

@Service
public class HistoricoCalculoService {
    private final CalculoRepository calculoRepository;

    public HistoricoCalculoService(CalculoRepository calculoRepository) {
        this.calculoRepository = calculoRepository;
    }

    public CalculoSalvo salvar(ResultadoOperacao resultado) {
        boolean calculoJaExiste = calculoRepository.buscarTodos().stream()
                .anyMatch(calculo -> calculo.numero1() == resultado.numero1()
                        && calculo.numero2() == resultado.numero2()
                        && calculo.operacao() == resultado.operacao());

        if (calculoJaExiste) {
            throw new CalculoDuplicadoException();
        }

        long id = calculoRepository.gerarId();
        CalculoSalvo calculo = new CalculoSalvo(id, resultado.numero1(), resultado.numero2(),
                resultado.operacao(), resultado.resultado());
        return calculoRepository.salvar(calculo);
    }

    public Optional<CalculoSalvo> buscarPorId(long id) {
        return calculoRepository.buscarPorId(id);
    }

    public Optional<CalculoSalvo> atualizar(long id, ResultadoOperacao resultado) {
        if (calculoRepository.buscarPorId(id).isEmpty()) {
            return Optional.empty();
        }

        boolean calculoJaExiste = calculoRepository.buscarTodos().stream()
                .anyMatch(entrada -> entrada.id() != id && entrada.numero1() == resultado.numero1()
                        && entrada.numero2() == resultado.numero2()
                        && entrada.operacao() == resultado.operacao());

        if (calculoJaExiste) {
            throw new CalculoDuplicadoException();
        }

        CalculoSalvo calculoAtualizado = new CalculoSalvo(id, resultado.numero1(),
                resultado.numero2(), resultado.operacao(), resultado.resultado());
        return Optional.of(calculoRepository.salvar(calculoAtualizado));
    }

    public boolean removerPorId(long id) {
        return calculoRepository.removerPorId(id);
    }
}
