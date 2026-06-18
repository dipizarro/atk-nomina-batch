package cl.atk.nomina.batch.service;

import cl.atk.nomina.batch.api.dto.StartBatchResponse;
import cl.atk.nomina.batch.api.dto.StartBatchRequest;
import cl.atk.nomina.batch.batch.config.NominaBatchJobConfig;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BatchLauncherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchLauncherService.class);

    private final JobLauncher jobLauncher;
    private final Job nominaDocumentosContablesJob;
    private final int defaultMaxNominas;
    private final AtomicLong runIdSequence = new AtomicLong(System.currentTimeMillis());

    public BatchLauncherService(
            @Qualifier("asyncJobLauncher") JobLauncher jobLauncher,
            Job nominaDocumentosContablesJob,
            @Value("${atk.batch.max-nominas:1000}") int defaultMaxNominas) {
        this.jobLauncher = jobLauncher;
        this.nominaDocumentosContablesJob = nominaDocumentosContablesJob;
        this.defaultMaxNominas = defaultMaxNominas;
    }

    public StartBatchResponse startNominaBatch(StartBatchRequest request) {
        try {
            StartBatchRequest effectiveRequest = request == null
                    ? new StartBatchRequest("GENERALES", null, true)
                    : request;
            ArtikosProfileType profileType = ArtikosProfileType.from(effectiveRequest.profile());
            int maxNominas = effectiveRequest.resolvedMaxNominas(defaultMaxNominas);
            boolean dryRun = effectiveRequest.resolvedDryRun();
            Long runId = nextRunId();
            JobParameters parameters = new JobParametersBuilder()
                    .addString("profile", profileType.name())
                    .addLong("maxNominas", (long) maxNominas)
                    .addString("dryRun", Boolean.toString(dryRun))
                    .addLong("run.id", runId)
                    .toJobParameters();
            LOGGER.info("Launching nomina batch job={} runId={} profile={} maxNominas={} dryRun={}",
                    NominaBatchJobConfig.JOB_NAME, runId, profileType, maxNominas, dryRun);
            JobExecution execution = jobLauncher.run(nominaDocumentosContablesJob, parameters);
            LOGGER.info("Nomina batch accepted jobExecutionId={} status={} profile={} maxNominas={} dryRun={}",
                    execution.getId(), execution.getStatus(), profileType, maxNominas, dryRun);

            return new StartBatchResponse(
                    execution.getId(),
                    NominaBatchJobConfig.JOB_NAME,
                    execution.getStatus().name(),
                    "Batch iniciado correctamente",
                    profileType.name(),
                    maxNominas,
                    dryRun);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error("Failed to launch nomina batch job={}", NominaBatchJobConfig.JOB_NAME, exception);
            throw new IllegalStateException("No fue posible iniciar el batch de nominas", exception);
        }
    }

    private Long nextRunId() {
        return runIdSequence.updateAndGet(previous -> Math.max(System.currentTimeMillis(), previous + 1));
    }
}
