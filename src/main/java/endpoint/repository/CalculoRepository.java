package endpoint.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import endpoint.model.CalculoSalvo;

@Repository
public class CalculoRepository {
    private final AtomicLong proximoId = new AtomicLong(1);
    private final Map<Long, CalculoSalvo> calculos = new ConcurrentHashMap<>();

    public long gerarId() {
        return proximoId.getAndIncrement();
    }

    public CalculoSalvo salvar(CalculoSalvo calculo) {
        calculos.put(calculo.id(), calculo);
        return calculo;
    }

    public Optional<CalculoSalvo> buscarPorId(long id) {
        return Optional.ofNullable(calculos.get(id));
    }

    public List<CalculoSalvo> buscarTodos() {
        return List.copyOf(calculos.values());
    }

    public boolean removerPorId(long id) {
        return calculos.remove(id) != null;
    }
}
