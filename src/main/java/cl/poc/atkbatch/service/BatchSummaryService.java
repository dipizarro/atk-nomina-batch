package cl.poc.atkbatch.service;

import cl.poc.atkbatch.api.dto.BatchSummaryResponse;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BatchSummaryService {

    private final JobExplorer jobExplorer;
    private final BatchResultStore batchResultStore;

    public BatchSummaryService(JobExplorer jobExplorer, BatchResultStore batchResultStore) {
        this.jobExplorer = jobExplorer;
        this.batchResultStore = batchResultStore;
    }

    public BatchSummaryResponse getSummary(Long jobExecutionId) {
        JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
        if (execution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejecucion batch no encontrada");
        }

        BatchResultStore.BatchResultSummary summary = batchResultStore.getSummary(jobExecutionId);
        return new BatchSummaryResponse(
                jobExecutionId,
                execution.getStatus().name(),
                summary.numeroNomina(),
                summary.totalProcessed(),
                summary.totalOk(),
                summary.totalNok(),
                summary.totalConciliaciones(),
                summary.totalDistribuciones());
    }
}
