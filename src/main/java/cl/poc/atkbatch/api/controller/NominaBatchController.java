package cl.poc.atkbatch.api.controller;

import cl.poc.atkbatch.api.dto.BatchStatusResponse;
import cl.poc.atkbatch.api.dto.StartBatchResponse;
import cl.poc.atkbatch.service.BatchLauncherService;
import cl.poc.atkbatch.service.BatchStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Nomina Batch", description = "Operaciones para iniciar y consultar el batch de nominas")
@RestController
@RequestMapping("/api/v1/nominas/batch")
public class NominaBatchController {

    private final BatchLauncherService batchLauncherService;
    private final BatchStatusService batchStatusService;

    public NominaBatchController(BatchLauncherService batchLauncherService, BatchStatusService batchStatusService) {
        this.batchLauncherService = batchLauncherService;
        this.batchStatusService = batchStatusService;
    }

    @Operation(summary = "Inicia asincronicamente el batch de nominas")
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public StartBatchResponse startBatch() {
        return batchLauncherService.startNominaBatch();
    }

    @Operation(summary = "Consulta el estado de una ejecucion batch")
    @GetMapping("/{jobExecutionId}")
    public BatchStatusResponse getBatchStatus(@PathVariable Long jobExecutionId) {
        return batchStatusService.getStatus(jobExecutionId);
    }
}
