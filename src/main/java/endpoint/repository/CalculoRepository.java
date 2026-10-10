package endpoint.repository;

import java.util.Optional;
import endpoint.exception.CalculoDuplicadoException;
import endpoint.model.CalculoSalvo;
import endpoint.model.ResultadoOperacao;
import endpoint.persistence.CalculoEntity;
import endpoint.persistence.CalculoJpaRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CalculoRepository {
    private final CalculoJpaRepository jpa;

    public CalculoRepository(CalculoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Transactional
    public CalculoSalvo salvar(ResultadoOperacao resultado) {
        if (jpa.existsByNumero1AndNumero2AndOperacao(resultado.numero1(), resultado.numero2(),
                resultado.operacao())) {
            throw new CalculoDuplicadoException();
        }
        return gravar(new CalculoEntity(resultado));
    }

    @Transactional(readOnly = true)
    public Optional<CalculoSalvo> buscarPorId(long id) {
        return jpa.findById(id).map(entidade -> entidade.toResponse());
    }

    @Transactional
    public Optional<CalculoSalvo> atualizar(long id, ResultadoOperacao resultado) {
        return jpa.buscarParaAtualizar(id).map(entidade -> {
            if (jpa.existsByNumero1AndNumero2AndOperacaoAndIdNot(resultado.numero1(),
                    resultado.numero2(), resultado.operacao(), id)) {
                throw new CalculoDuplicadoException();
            }
            entidade.atualizar(resultado);
            return gravar(entidade);
        });
    }

    @Transactional
    public boolean removerPorId(long id) {
        return jpa.remover(id) > 0;
    }

    private CalculoSalvo gravar(CalculoEntity entidade) {
        try {
            return jpa.saveAndFlush(entidade).toResponse();
        } catch (DataIntegrityViolationException exception) {
            // Apenas esta restricao vira HTTP 409; outros erros seguem intactos.
            for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
                if (causa instanceof ConstraintViolationException violacao
                        && "uk_calculos_numeros_operacao".equals(violacao.getConstraintName())) {
                    throw new CalculoDuplicadoException();
                }
            }
            throw exception;
        }
    }
}
