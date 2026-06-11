package cl.poc.atkbatch.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NominaBatchJobConfig {

    public static final String JOB_NAME = "nominaDocumentosContablesJob";
    public static final String INITIAL_STEP_NAME = "initialNominaTaskletStep";

    private static final Logger LOGGER = LoggerFactory.getLogger(NominaBatchJobConfig.class);
    private static final long SIMULATED_WORK_MILLIS = 3_000L;

    @Bean
    public Job nominaDocumentosContablesJob(JobRepository jobRepository, Step initialNominaTaskletStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(initialNominaTaskletStep)
                .build();
    }

    @Bean
    public Step initialNominaTaskletStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder(INITIAL_STEP_NAME, jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LOGGER.info("Iniciando simulacion del batch de nominas");
                    Thread.sleep(SIMULATED_WORK_MILLIS);
                    LOGGER.info("Finalizando simulacion del batch de nominas");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
