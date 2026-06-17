package cl.poc.atkbatch.batch.processor;

import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosOperation;
import cl.poc.atkbatch.domain.artikos.ArtikosFetchedNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosGenericResponse;
import cl.poc.atkbatch.domain.error.IntegrationErrorType;
import cl.poc.atkbatch.service.ControlNominaService;
import cl.poc.atkbatch.service.NominaErrorPolicyService;
import cl.poc.atkbatch.service.NominaProcessingService;
import cl.poc.atkbatch.service.artikos.ArtikosGenericSoapResponseParser;
import cl.poc.atkbatch.service.artikos.ArtikosSoapClient;
import cl.poc.atkbatch.shared.exception.ArtikosIntegrationException;
import cl.poc.atkbatch.shared.logging.LoggingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class ArtikosNominaItemProcessor implements ItemProcessor<ArtikosFetchedNomina, ResultadoNomina> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikosNominaItemProcessor.class);

    private final ControlNominaService controlNominaService;
    private final ArtikosSoapClient soapClient;
    private final ArtikosGenericSoapResponseParser genericResponseParser;
    private final NominaProcessingService nominaProcessingService;
    private final NominaErrorPolicyService errorPolicyService;
    private final Long jobExecutionId;
    private final boolean dryRun;

    public ArtikosNominaItemProcessor(
            ControlNominaService controlNominaService,
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericResponseParser,
            NominaProcessingService nominaProcessingService,
            NominaErrorPolicyService errorPolicyService,
            Long jobExecutionId,
            String dryRun) {
        this.controlNominaService = controlNominaService;
        this.soapClient = soapClient;
        this.genericResponseParser = genericResponseParser;
        this.nominaProcessingService = nominaProcessingService;
        this.errorPolicyService = errorPolicyService;
        this.jobExecutionId = jobExecutionId;
        this.dryRun = Boolean.parseBoolean(dryRun);
    }

    @Override
    public ResultadoNomina process(ArtikosFetchedNomina item) {
        Long numeroNomina = item.numeroNomina();
        LoggingContext.putJobExecutionId(jobExecutionId);
        LoggingContext.putProfile(item.profile().name());
        LoggingContext.putNumeroNomina(numeroNomina);
        if (dryRun) {
            try {
                LOGGER.info("Dry-run processing Artikos nomina jobExecutionId={} profile={} numeroNomina={}",
                        jobExecutionId, item.profile(), numeroNomina);
                return processLocally(item);
            } finally {
                LoggingContext.clearAll();
            }
        }

        try {
            LOGGER.info("[CONTROL_NOMINA] PROCESSING jobExecutionId={} numeroNomina={} profile={}",
                    jobExecutionId, numeroNomina, item.profile());
            markProcessing(item);

            LoggingContext.putOperation(ArtikosOperation.NOMFACTCONFIR.name());
            LOGGER.info("Sending Artikos confirmation profile={} numeroNomina={}", item.profile(), numeroNomina);
            String confirmationRawXml = soapClient.confirmNominaRawXml(item.profile(), numeroNomina, 0);
            ArtikosGenericResponse confirmationResponse = genericResponseParser.parseGenericResponse(confirmationRawXml);
            if (!confirmationResponse.success()) {
                String message = "Confirmacion Artikos rechazada: " + confirmationResponse.messageText();
                LOGGER.warn("Artikos confirmation error profile={} numeroNomina={} msgStatus={} message={}",
                        item.profile(), numeroNomina, confirmationResponse.msgStatus(), confirmationResponse.messageText());
                ArtikosIntegrationException exception = new ArtikosIntegrationException(
                        IntegrationErrorType.NOMINA_CONFIRM_ERROR,
                        item.profile().name(),
                        numeroNomina,
                        ArtikosOperation.NOMFACTCONFIR.name(),
                        message,
                        null);
                markControlErrorIfRequired(exception);
                throw exception;
            }
            LOGGER.info("Artikos confirmation OK profile={} numeroNomina={}", item.profile(), numeroNomina);
            LoggingContext.clearOperation();

            return processLocally(item);
        } catch (ArtikosIntegrationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            ArtikosIntegrationException integrationException = new ArtikosIntegrationException(
                    IntegrationErrorType.NOMINA_PROCESSING_ERROR,
                    item.profile().name(),
                    numeroNomina,
                    currentOperation(),
                    exception.getMessage(),
                    exception);
            markControlErrorIfRequired(integrationException);
            throw integrationException;
        } finally {
            LoggingContext.clearAll();
        }
    }

    private void markProcessing(ArtikosFetchedNomina item) {
        try {
            controlNominaService.markProcessing(jobExecutionId, item.numeroNomina());
        } catch (RuntimeException exception) {
            throw new ArtikosIntegrationException(
                    IntegrationErrorType.ORACLE_CONTROL_ERROR,
                    item.profile().name(),
                    item.numeroNomina(),
                    "CONTROL_NOMINA",
                    exception.getMessage(),
                    exception);
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

    private String currentOperation() {
        return LoggingContext.snapshot().get("operation");
    }

    private ResultadoNomina processLocally(ArtikosFetchedNomina item) {
        LoggingContext.clearOperation();
        LOGGER.info("Generating NOMFACTRES locally profile={} numeroNomina={}", item.profile(), item.numeroNomina());
        ResultadoNomina result = nominaProcessingService.process(
                jobExecutionId,
                item.numeroNomina(),
                item.nomina(),
                soapClient.resultadoNominaConfig(item.profile()));
        LOGGER.info("Nomina processed locally jobExecutionId={} profile={} numeroNomina={} totalDocuments={} "
                        + "totalOk={} totalNok={}",
                jobExecutionId,
                item.profile(),
                item.numeroNomina(),
                result.totalDocuments(),
                result.totalOk(),
                result.totalNok());
        return result;
    }
}
