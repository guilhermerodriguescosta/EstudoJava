package endpoint.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
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
        return calculoRepository.salvar(resultado);
    }

    public Optional<CalculoSalvo> buscarPorId(long id) {
        return calculoRepository.buscarPorId(id);
    }

    public Optional<CalculoSalvo> atualizar(long id, ResultadoOperacao resultado) {
        return calculoRepository.atualizar(id, resultado);
    }

    public boolean removerPorId(long id) {
        return calculoRepository.removerPorId(id);
    }
}
