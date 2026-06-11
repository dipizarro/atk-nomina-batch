package cl.poc.atkbatch.service;

import cl.poc.atkbatch.api.dto.StartBatchResponse;
import cl.poc.atkbatch.batch.config.NominaBatchJobConfig;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchLauncherService {

    private final JobLauncher jobLauncher;
    private final Job nominaDocumentosContablesJob;
    private final AtomicLong runIdSequence = new AtomicLong(System.currentTimeMillis());

    public BatchLauncherService(@Qualifier("asyncJobLauncher") JobLauncher jobLauncher, Job nominaDocumentosContablesJob) {
        this.jobLauncher = jobLauncher;
        this.nominaDocumentosContablesJob = nominaDocumentosContablesJob;
    }

    public StartBatchResponse startNominaBatch() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("run.id", nextRunId())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(nominaDocumentosContablesJob, parameters);

            return new StartBatchResponse(
                    execution.getId(),
                    NominaBatchJobConfig.JOB_NAME,
                    execution.getStatus().name(),
                    "Batch iniciado correctamente");
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible iniciar el batch de nominas", exception);
        }
    }

    private Long nextRunId() {
        return runIdSequence.updateAndGet(previous -> Math.max(System.currentTimeMillis(), previous + 1));
    }
}
