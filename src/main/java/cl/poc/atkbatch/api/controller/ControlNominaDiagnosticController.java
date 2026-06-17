package cl.poc.atkbatch.api.controller;

import cl.poc.atkbatch.api.dto.ControlNominaResponse;
import cl.poc.atkbatch.api.mapper.ControlNominaMapper;
import cl.poc.atkbatch.domain.ControlNominaEntity;
import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.service.ControlNominaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Control Nomina Diagnostic", description = "Endpoints temporales de diagnostico Oracle; no usar en produccion")
@RestController
@RequestMapping("/api/v1/control-nomina")
@Deprecated(since = "8.1", forRemoval = false)
public class ControlNominaDiagnosticController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlNominaDiagnosticController.class);

    private static final Long DIAGNOSTIC_JOB_EXECUTION_ID = 999999L;
    private static final Long DIAGNOSTIC_NUMERO_NOMINA = 15960L;

    private final ControlNominaService controlNominaService;
    private final ControlNominaMapper mapper;

    public ControlNominaDiagnosticController(
            ControlNominaService controlNominaService,
            ControlNominaMapper mapper) {
        this.controlNominaService = controlNominaService;
        this.mapper = mapper;
    }

    @Operation(summary = "[DIAGNOSTIC - not for production] Ejecuta una prueba temporal de insercion y actualizacion en CONTROL_NOMINA")
    @PostMapping("/test")
    public ControlNominaResponse testControlNominaPersistence() {
        LOGGER.info("Running CONTROL_NOMINA diagnostic test jobExecutionId={} numeroNomina={}",
                DIAGNOSTIC_JOB_EXECUTION_ID, DIAGNOSTIC_NUMERO_NOMINA);
        controlNominaService.markProcessing(DIAGNOSTIC_JOB_EXECUTION_ID, DIAGNOSTIC_NUMERO_NOMINA);

        ResultadoNomina result = new ResultadoNomina(
                DIAGNOSTIC_JOB_EXECUTION_ID,
                DIAGNOSTIC_NUMERO_NOMINA,
                1,
                1,
                0,
                2,
                2,
                List.of(),
                "<NOMFACTRES/>",
                "OK",
                null);

        controlNominaService.markCompleted(result);
        ControlNominaEntity entity = controlNominaService.findByJobExecutionIdAndNumeroNomina(
                        DIAGNOSTIC_JOB_EXECUTION_ID, DIAGNOSTIC_NUMERO_NOMINA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CONTROL_NOMINA no encontrado"));
        LOGGER.info("CONTROL_NOMINA diagnostic test completed status={}", entity.getStatus());
        return mapper.toResponse(entity);
    }

    @Operation(summary = "[DIAGNOSTIC - not for production] Consulta registros CONTROL_NOMINA por ejecucion batch")
    @GetMapping("/jobs/{jobExecutionId}")
    public List<ControlNominaResponse> findByJobExecutionId(@PathVariable Long jobExecutionId) {
        List<ControlNominaResponse> results = controlNominaService.findByJobExecutionId(jobExecutionId).stream()
                .map(mapper::toResponse)
                .toList();
        LOGGER.info("CONTROL_NOMINA query by jobExecutionId={} returned {} records", jobExecutionId, results.size());
        return results;
    }

    @Operation(summary = "[DIAGNOSTIC - not for production] Consulta un registro CONTROL_NOMINA especifico")
    @GetMapping("/jobs/{jobExecutionId}/nominas/{numeroNomina}")
    public ControlNominaResponse findByJobExecutionIdAndNumeroNomina(
            @PathVariable Long jobExecutionId,
            @PathVariable Long numeroNomina) {
        LOGGER.info("CONTROL_NOMINA query by jobExecutionId={} numeroNomina={}", jobExecutionId, numeroNomina);
        return controlNominaService.findByJobExecutionIdAndNumeroNomina(jobExecutionId, numeroNomina)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CONTROL_NOMINA no encontrado"));
    }
}
