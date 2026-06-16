package cl.poc.atkbatch.batch.config;

import cl.poc.atkbatch.batch.processor.NominaItemProcessor;
import cl.poc.atkbatch.batch.processor.NominaDocumentoItemProcessor;
import cl.poc.atkbatch.batch.reader.NominaItemReader;
import cl.poc.atkbatch.batch.reader.NominaDocumentoItemReader;
import cl.poc.atkbatch.batch.writer.NominaResultItemWriter;
import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.SimulatedDocumentoContable;
import cl.poc.atkbatch.domain.SimulatedNomina;
import cl.poc.atkbatch.service.BatchResultStore;
import cl.poc.atkbatch.service.ControlNominaService;
import cl.poc.atkbatch.service.NominaResultXmlService;
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
            ItemReader<SimulatedNomina> nominaItemReader,
            ItemProcessor<SimulatedNomina, ResultadoNomina> nominaItemProcessor,
            ItemWriter<ResultadoNomina> nominaResultItemWriter,
            @Value("${atk.batch.chunk-size}") int chunkSize) {
        return new StepBuilder(PROCESS_STEP_NAME, jobRepository)
                .<SimulatedNomina, ResultadoNomina>chunk(chunkSize, transactionManager)
                .reader(nominaItemReader)
                .processor(nominaItemProcessor)
                .writer(nominaResultItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public NominaItemReader nominaItemReader(
            NominaXmlParserService parserService,
            @Value("${atk.batch.simulation-nominas}") int simulationNominas) {
        return new NominaItemReader(parserService, simulationNominas);
    }

    @Bean
    @StepScope
    public NominaItemProcessor nominaItemProcessor(
            NominaDocumentoItemProcessor nominaDocumentoItemProcessor,
            NominaResultXmlService nominaResultXmlService,
            ControlNominaService controlNominaService,
            @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) {
        return new NominaItemProcessor(
                nominaDocumentoItemProcessor,
                nominaResultXmlService,
                controlNominaService,
                jobExecutionId);
    }

    @Bean
    @StepScope
    public NominaResultItemWriter nominaResultItemWriter(
            BatchResultStore batchResultStore,
            ControlNominaService controlNominaService,
            @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) {
        return new NominaResultItemWriter(batchResultStore, controlNominaService, jobExecutionId);
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
