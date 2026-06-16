package cl.poc.atkbatch.service;

import cl.poc.atkbatch.domain.ControlNominaEntity;
import cl.poc.atkbatch.domain.ControlNominaStatus;
import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.repository.ControlNominaJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ControlNominaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlNominaService.class);

    private final ControlNominaJpaRepository repository;

    public ControlNominaService(ControlNominaJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ControlNominaEntity markProcessing(Long jobExecutionId, Long numeroNomina) {
        Optional<ControlNominaEntity> existing = repository.findByIdJobExecutionIdAndIdNumeroNomina(
                jobExecutionId, numeroNomina);
        if (existing.isPresent()) {
            LOGGER.warn("CONTROL_NOMINA already exists for jobExecutionId={} numeroNomina={}",
                    jobExecutionId, numeroNomina);
            return existing.get();
        }

        ControlNominaEntity entity = new ControlNominaEntity();
        entity.setJobExecutionId(jobExecutionId);
        entity.setNumeroNomina(numeroNomina);
        entity.setStatus(ControlNominaStatus.PROCESSING);
        entity.setCreatedAt(LocalDateTime.now());

        LOGGER.info("Inserting CONTROL_NOMINA PROCESSING for jobExecutionId={} numeroNomina={}",
                jobExecutionId, numeroNomina);
        return repository.save(entity);
    }

    @Transactional
    public ControlNominaEntity markCompleted(ResultadoNomina resultadoNomina) {
        ControlNominaEntity entity = repository.findByIdJobExecutionIdAndIdNumeroNomina(
                        resultadoNomina.jobExecutionId(), resultadoNomina.numeroNomina())
                .orElseGet(() -> createBaseEntity(resultadoNomina.jobExecutionId(), resultadoNomina.numeroNomina()));

        entity.setTotalDocuments(resultadoNomina.totalDocuments());
        entity.setTotalOk(resultadoNomina.totalOk());
        entity.setTotalNok(resultadoNomina.totalNok());
        entity.setTotalConciliaciones(resultadoNomina.totalConciliaciones());
        entity.setTotalDistribuciones(resultadoNomina.totalDistribuciones());
        entity.setStatus(resultadoNomina.totalNok() != null && resultadoNomina.totalNok() > 0
                ? ControlNominaStatus.NOK
                : ControlNominaStatus.OK);
        entity.setErrorMessage(null);
        entity.setUpdatedAt(LocalDateTime.now());

        LOGGER.info("[CONTROL_NOMINA] COMPLETED jobExecutionId={} numeroNomina={} status={}",
                resultadoNomina.jobExecutionId(), resultadoNomina.numeroNomina(), entity.getStatus());
        return repository.save(entity);
    }

    @Transactional
    public ControlNominaEntity markError(Long jobExecutionId, Long numeroNomina, String errorMessage) {
        ControlNominaEntity entity = repository.findByIdJobExecutionIdAndIdNumeroNomina(jobExecutionId, numeroNomina)
                .orElseGet(() -> createBaseEntity(jobExecutionId, numeroNomina));

        entity.setStatus(ControlNominaStatus.ERROR);
        entity.setErrorMessage(trimErrorMessage(errorMessage));
        entity.setUpdatedAt(LocalDateTime.now());

        LOGGER.info("[CONTROL_NOMINA] ERROR jobExecutionId={} numeroNomina={} error={}",
                jobExecutionId, numeroNomina, entity.getErrorMessage());
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ControlNominaEntity> findByJobExecutionId(Long jobExecutionId) {
        return repository.findByIdJobExecutionId(jobExecutionId);
    }

    @Transactional(readOnly = true)
    public Optional<ControlNominaEntity> findByJobExecutionIdAndNumeroNomina(Long jobExecutionId, Long numeroNomina) {
        return repository.findByIdJobExecutionIdAndIdNumeroNomina(jobExecutionId, numeroNomina);
    }

    private ControlNominaEntity createBaseEntity(Long jobExecutionId, Long numeroNomina) {
        ControlNominaEntity entity = new ControlNominaEntity();
        entity.setJobExecutionId(jobExecutionId);
        entity.setNumeroNomina(numeroNomina);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private String trimErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.length() <= 500) {
            return errorMessage;
        }
        return errorMessage.substring(0, 500);
    }
}
