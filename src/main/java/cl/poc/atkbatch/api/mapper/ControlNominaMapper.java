package cl.poc.atkbatch.api.mapper;

import cl.poc.atkbatch.api.dto.ControlNominaResponse;
import cl.poc.atkbatch.domain.ControlNominaEntity;
import org.springframework.stereotype.Component;

@Component
public class ControlNominaMapper {

    public ControlNominaResponse toResponse(ControlNominaEntity entity) {
        return new ControlNominaResponse(
                entity.getJobExecutionId(),
                entity.getNumeroNomina(),
                entity.getTotalDocuments(),
                entity.getTotalOk(),
                entity.getTotalNok(),
                entity.getTotalConciliaciones(),
                entity.getTotalDistribuciones(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getErrorMessage());
    }
}
