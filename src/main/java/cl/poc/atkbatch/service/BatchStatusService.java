package cl.poc.atkbatch.service;

import cl.poc.atkbatch.api.dto.BatchStatusResponse;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BatchStatusService {

    private final JobExplorer jobExplorer;

    public BatchStatusService(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    public BatchStatusResponse getStatus(Long jobExecutionId) {
        JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
        if (execution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejecucion batch no encontrada");
        }

        return new BatchStatusResponse(
                execution.getId(),
                execution.getJobInstance().getJobName(),
                execution.getStatus().name(),
                execution.getExitStatus().getExitCode(),
                execution.getCreateTime(),
                execution.getStartTime(),
                execution.getEndTime(),
                "Estado consultado correctamente");
    }
}
