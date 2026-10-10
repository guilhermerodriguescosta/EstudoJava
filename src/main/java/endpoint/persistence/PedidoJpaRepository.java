package endpoint.persistence;

import java.util.UUID;
import endpoint.model.PedidoStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, UUID> {
    @Modifying
    @Query("update PedidoEntity p set p.status = :status where p.id = :id")
    int atualizarStatus(@Param("id") UUID id, @Param("status") PedidoStatus status);
}
