package cl.atk.nomina.batch.repository;

import cl.atk.nomina.batch.domain.ControlNominaEntity;
import cl.atk.nomina.batch.domain.ControlNominaId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlNominaJpaRepository extends JpaRepository<ControlNominaEntity, ControlNominaId> {

    Optional<ControlNominaEntity> findByIdJobExecutionIdAndIdNumeroNomina(Long jobExecutionId, Long numeroNomina);

    Optional<ControlNominaEntity> findTopByIdNumeroNominaOrderByCreatedAtDesc(Long numeroNomina);

    List<ControlNominaEntity> findByIdJobExecutionId(Long jobExecutionId);

    boolean existsByIdJobExecutionIdAndIdNumeroNomina(Long jobExecutionId, Long numeroNomina);
}
