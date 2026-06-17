package cl.poc.atkbatch.batch.writer;

import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosOperation;
import cl.poc.atkbatch.domain.artikos.ArtikosGenericResponse;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import cl.poc.atkbatch.domain.error.IntegrationErrorType;
import cl.poc.atkbatch.service.BatchResultStore;
import cl.poc.atkbatch.service.ControlNominaService;
import cl.poc.atkbatch.service.NominaErrorPolicyService;
import cl.poc.atkbatch.service.artikos.ArtikosGenericSoapResponseParser;
import cl.poc.atkbatch.service.artikos.ArtikosSoapClient;
import cl.poc.atkbatch.shared.exception.ArtikosIntegrationException;
import cl.poc.atkbatch.shared.logging.LoggingContext;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class ArtikosNominaResultItemWriter implements ItemWriter<ResultadoNomina> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikosNominaResultItemWriter.class);

    private final ArtikosSoapClient soapClient;
    private final ArtikosGenericSoapResponseParser genericResponseParser;
    private final ControlNominaService controlNominaService;
    private final NominaErrorPolicyService errorPolicyService;
    private final BatchResultStore batchResultStore;
    private final ArtikosProfileType profile;
    private final boolean dryRun;
    private final Long jobExecutionId;

    public ArtikosNominaResultItemWriter(
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericResponseParser,
            ControlNominaService controlNominaService,
            NominaErrorPolicyService errorPolicyService,
            BatchResultStore batchResultStore,
            String profile,
            String dryRun,
            Long jobExecutionId) {
        this.soapClient = soapClient;
        this.genericResponseParser = genericResponseParser;
        this.controlNominaService = controlNominaService;
        this.errorPolicyService = errorPolicyService;
        this.batchResultStore = batchResultStore;
        this.profile = ArtikosProfileType.from(profile);
        this.dryRun = Boolean.parseBoolean(dryRun);
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public void write(Chunk<? extends ResultadoNomina> chunk) {
        LoggingContext.putJobExecutionId(jobExecutionId);
        LoggingContext.putProfile(profile.name());
        LOGGER.info("Starting Artikos result writer chunk jobExecutionId={} profile={} size={} dryRun={}",
                jobExecutionId, profile, chunk.size(), dryRun);
        List<ResultadoNomina> completedResults = new ArrayList<>();
        try {
            for (ResultadoNomina result : chunk.getItems()) {
                ResultadoNomina resultWithJob = result.withJobExecutionId(jobExecutionId);
                LoggingContext.putNumeroNomina(resultWithJob.numeroNomina());
                if (dryRun) {
                    LOGGER.info("Dry-run storing local result jobExecutionId={} profile={} numeroNomina={} status={}",
                            jobExecutionId, profile, resultWithJob.numeroNomina(), resultWithJob.status());
                    completedResults.add(resultWithJob);
                    continue;
                }

                sendResultToArtikos(resultWithJob);
                completedResults.add(resultWithJob);
            }

            batchResultStore.addNominaResults(jobExecutionId, completedResults);
            LOGGER.info("BatchResultStore updated jobExecutionId={} profile={} results={}",
                    jobExecutionId, profile, completedResults.size());
        } finally {
            LoggingContext.clearAll();
        }
    }

    private void sendResultToArtikos(ResultadoNomina result) {
        LoggingContext.putOperation(ArtikosOperation.NOMFACTRES.name());
        try {
            LOGGER.info("Sending NOMFACTRES to Artikos profile={} jobExecutionId={} numeroNomina={}",
                    profile, jobExecutionId, result.numeroNomina());
            String resultRawXml = soapClient.sendNominaResultRawXml(profile, result);
            ArtikosGenericResponse response = genericResponseParser.parseGenericResponse(resultRawXml);
            if (!response.success()) {
                String message = "NOMFACTRES Artikos rechazado: " + response.messageText();
                LOGGER.warn("Artikos NOMFACTRES error profile={} jobExecutionId={} numeroNomina={} msgStatus={} "
                                + "message={}",
                        profile, jobExecutionId, result.numeroNomina(), response.msgStatus(), response.messageText());
                ArtikosIntegrationException exception = new ArtikosIntegrationException(
                        IntegrationErrorType.NOMINA_RESULT_ERROR,
                        profile.name(),
                        result.numeroNomina(),
                        ArtikosOperation.NOMFACTRES.name(),
                        message,
                        null);
                markControlErrorIfRequired(exception);
                throw exception;
            }

            LOGGER.info("Artikos NOMFACTRES OK profile={} jobExecutionId={} numeroNomina={}",
                    profile, jobExecutionId, result.numeroNomina());
            controlNominaService.markCompleted(result);
            LOGGER.info("CONTROL_NOMINA updated from writer jobExecutionId={} numeroNomina={} status={}",
                    jobExecutionId, result.numeroNomina(), result.status());
        } catch (ArtikosIntegrationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            ArtikosIntegrationException integrationException = new ArtikosIntegrationException(
                    IntegrationErrorType.NOMINA_RESULT_ERROR,
                    profile.name(),
                    result.numeroNomina(),
                    ArtikosOperation.NOMFACTRES.name(),
                    exception.getMessage(),
                    exception);
            markControlErrorIfRequired(integrationException);
            throw integrationException;
        } finally {
            LoggingContext.clearOperation();
        }
    }

    private void markControlErrorIfRequired(ArtikosIntegrationException exception) {
        if (errorPolicyService.shouldMarkControlNominaError(exception.getErrorType(), exception.getNumeroNomina())) {
            try {
                controlNominaService.markError(
                        jobExecutionId,
                        exception.getNumeroNomina(),
                        errorPolicyService.buildControlErrorMessage(exception));
            } catch (RuntimeException controlException) {
                throw new ArtikosIntegrationException(
                        IntegrationErrorType.ORACLE_CONTROL_ERROR,
                        exception.getProfile(),
                        exception.getNumeroNomina(),
                        "CONTROL_NOMINA",
                        controlException.getMessage(),
                        controlException);
            }
        }
    }
}
