package cl.poc.atkbatch.batch.config;

import cl.poc.atkbatch.batch.processor.NominaDocumentoItemProcessor;
import cl.poc.atkbatch.batch.reader.NominaDocumentoItemReader;
import cl.poc.atkbatch.batch.writer.NominaDocumentoItemWriter;
import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.SimulatedDocumentoContable;
import cl.poc.atkbatch.service.BatchResultStore;
import cl.poc.atkbatch.service.NominaXmlParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NominaBatchJobConfig {

    public static final String JOB_NAME = "nominaDocumentosContablesJob";
    public static final String PROCESS_STEP_NAME = "processNominaDocumentosStep";

    private static final Logger LOGGER = LoggerFactory.getLogger(NominaBatchJobConfig.class);

    @Bean
    public Job nominaDocumentosContablesJob(
            JobRepository jobRepository,
            Step processNominaDocumentosStep,
            BatchResultStore batchResultStore) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(clearResultStoreListener(batchResultStore))
                .start(processNominaDocumentosStep)
                .build();
    }

    @Bean
    public Step processNominaDocumentosStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<SimulatedDocumentoContable> nominaDocumentoItemReader,
            ItemProcessor<SimulatedDocumentoContable, ResultadoDocumento> nominaDocumentoItemProcessor,
            ItemWriter<ResultadoDocumento> nominaDocumentoItemWriter,
            @Value("${atk.batch.chunk-size}") int chunkSize) {
        return new StepBuilder(PROCESS_STEP_NAME, jobRepository)
                .<SimulatedDocumentoContable, ResultadoDocumento>chunk(chunkSize, transactionManager)
                .reader(nominaDocumentoItemReader)
                .processor(nominaDocumentoItemProcessor)
                .writer(nominaDocumentoItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public NominaDocumentoItemReader nominaDocumentoItemReader(
            NominaXmlParserService parserService,
            @Value("${atk.batch.simulation-iterations}") int simulationIterations) {
        return new NominaDocumentoItemReader(parserService, simulationIterations);
    }

    @Bean
    public NominaDocumentoItemProcessor nominaDocumentoItemProcessor() {
        return new NominaDocumentoItemProcessor();
    }

    @Bean
    @StepScope
    public NominaDocumentoItemWriter nominaDocumentoItemWriter(
            BatchResultStore batchResultStore,
            @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) {
        return new NominaDocumentoItemWriter(batchResultStore, jobExecutionId);
    }

    private JobExecutionListener clearResultStoreListener(BatchResultStore batchResultStore) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                LOGGER.info("Limpiando resultados en memoria para jobExecutionId={}", jobExecution.getId());
                batchResultStore.clearResults(jobExecution.getId());
            }
        };
    }
}
