package cl.atk.nomina.batch.api.controller;

import cl.atk.nomina.batch.api.dto.BatchStatusResponse;
import cl.atk.nomina.batch.api.dto.BatchSummaryResponse;
import cl.atk.nomina.batch.api.dto.NominaResultResponse;
import cl.atk.nomina.batch.api.dto.StartBatchRequest;
import cl.atk.nomina.batch.api.dto.StartBatchResponse;
import cl.atk.nomina.batch.service.BatchLauncherService;
import cl.atk.nomina.batch.service.BatchStatusService;
import cl.atk.nomina.batch.service.BatchSummaryService;
import cl.atk.nomina.batch.shared.exception.BatchConcurrencyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Nomina Batch", description = "Operaciones para iniciar y consultar el batch de nominas")
@RestController
@RequestMapping("/api/v1/nominas/batch")
public class NominaBatchController {

    private final BatchLauncherService batchLauncherService;
    private final BatchStatusService batchStatusService;
    private final BatchSummaryService batchSummaryService;

    public NominaBatchController(
            BatchLauncherService batchLauncherService,
            BatchStatusService batchStatusService,
            BatchSummaryService batchSummaryService) {
        this.batchLauncherService = batchLauncherService;
        this.batchStatusService = batchStatusService;
        this.batchSummaryService = batchSummaryService;
    }

    @Operation(summary = "Inicia asincronicamente el batch de nominas")
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public StartBatchResponse startBatch(@Valid @RequestBody(required = false) StartBatchRequest request) {
        return batchLauncherService.startNominaBatch(request);
    }

    @Operation(summary = "Consulta el estado de una ejecucion batch")
    @GetMapping("/{jobExecutionId}")
    public BatchStatusResponse getBatchStatus(@PathVariable Long jobExecutionId) {
        return batchStatusService.getStatus(jobExecutionId);
    }

    @Operation(summary = "Consulta el resumen de resultados de una ejecucion batch")
    @GetMapping("/{jobExecutionId}/summary")
    public BatchSummaryResponse getBatchSummary(@PathVariable Long jobExecutionId) {
        return batchSummaryService.getSummary(jobExecutionId);
    }

    @Operation(summary = "Consulta el resultado de una nomina procesada")
    @GetMapping("/{jobExecutionId}/results/{numeroNomina}")
    public NominaResultResponse getNominaResult(
            @PathVariable Long jobExecutionId,
            @PathVariable Long numeroNomina) {
        return batchSummaryService.getNominaResult(jobExecutionId, numeroNomina);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(BatchConcurrencyException.class)
    public ResponseEntity<String> handleConflict(BatchConcurrencyException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }
}
