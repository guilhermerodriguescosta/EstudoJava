package endpoint.persistence;

import java.util.Optional;
import endpoint.model.Operacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CalculoJpaRepository extends JpaRepository<CalculoEntity, Long> {
    boolean existsByNumero1AndNumero2AndOperacao(double numero1, double numero2, Operacao operacao);

    boolean existsByNumero1AndNumero2AndOperacaoAndIdNot(double numero1, double numero2,
            Operacao operacao, long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CalculoEntity c where c.id = :id")
    Optional<CalculoEntity> buscarParaAtualizar(@Param("id") long id);

    @Modifying
    @Query("delete from CalculoEntity c where c.id = :id")
    int remover(@Param("id") long id);
}
