package br.com.guilhermecosta.estudojava.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import br.com.guilhermecosta.estudojava.exception.CalculoDuplicadoException;
import br.com.guilhermecosta.estudojava.model.CalculoSalvo;
import br.com.guilhermecosta.estudojava.model.ResultadoOperacao;

@Service
public class HistoricoCalculoService {
    private final AtomicLong proximoId = new AtomicLong(1);
    private final Map<Long, CalculoSalvo> calculos = new ConcurrentHashMap<>();

    public CalculoSalvo salvar(ResultadoOperacao resultado) {
        boolean calculoJaExiste = calculos.values().stream().anyMatch(calculo ->
                calculo.numero1() == resultado.numero1()
                        && calculo.numero2() == resultado.numero2()
                        && calculo.operacao() == resultado.operacao());

        if (calculoJaExiste) {
            throw new CalculoDuplicadoException();
        }

        long id = proximoId.getAndIncrement();
        CalculoSalvo calculo = new CalculoSalvo(
                id,
                resultado.numero1(),
                resultado.numero2(),
                resultado.operacao(),
                resultado.resultado());
        calculos.put(id, calculo);
        return calculo;
    }

    public Optional<CalculoSalvo> buscarPorId(long id) {
        return Optional.ofNullable(calculos.get(id));
    }

    public boolean removerPorId(long id) {
        return calculos.remove(id) != null;
    }
}
