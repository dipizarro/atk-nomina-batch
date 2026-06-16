package cl.poc.atkbatch.repository;

import cl.poc.atkbatch.domain.ControlNominaEntity;
import cl.poc.atkbatch.domain.ControlNominaId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlNominaJpaRepository extends JpaRepository<ControlNominaEntity, ControlNominaId> {

    Optional<ControlNominaEntity> findByIdJobExecutionIdAndIdNumeroNomina(Long jobExecutionId, Long numeroNomina);

    List<ControlNominaEntity> findByIdJobExecutionId(Long jobExecutionId);

    boolean existsByIdJobExecutionIdAndIdNumeroNomina(Long jobExecutionId, Long numeroNomina);
}
