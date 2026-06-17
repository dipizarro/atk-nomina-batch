package cl.poc.atkbatch.batch.processor;

import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosOperation;
import cl.poc.atkbatch.domain.artikos.ArtikosFetchedNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosGenericResponse;
import cl.poc.atkbatch.service.ControlNominaService;
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
    private final Long jobExecutionId;
    private final boolean dryRun;

    public ArtikosNominaItemProcessor(
            ControlNominaService controlNominaService,
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericResponseParser,
            NominaProcessingService nominaProcessingService,
            Long jobExecutionId,
            String dryRun) {
        this.controlNominaService = controlNominaService;
        this.soapClient = soapClient;
        this.genericResponseParser = genericResponseParser;
        this.nominaProcessingService = nominaProcessingService;
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
            controlNominaService.markProcessing(jobExecutionId, numeroNomina);

            LoggingContext.putOperation(ArtikosOperation.NOMFACTCONFIR.name());
            LOGGER.info("Sending Artikos confirmation profile={} numeroNomina={}", item.profile(), numeroNomina);
            String confirmationRawXml = soapClient.confirmNominaRawXml(item.profile(), numeroNomina, 0);
            ArtikosGenericResponse confirmationResponse = genericResponseParser.parseGenericResponse(confirmationRawXml);
            if (!confirmationResponse.success()) {
                if (isAlreadyOutsideIntegrationState(confirmationResponse.messageText())) {
                    LOGGER.warn("Artikos confirmation skipped as non-blocking state condition profile={} "
                                    + "numeroNomina={} msgStatus={} message={}",
                            item.profile(),
                            numeroNomina,
                            confirmationResponse.msgStatus(),
                            confirmationResponse.messageText());
                    return processLocally(item);
                }
                String message = "Confirmacion Artikos rechazada: " + confirmationResponse.messageText();
                LOGGER.warn("Artikos confirmation error profile={} numeroNomina={} msgStatus={} message={}",
                        item.profile(), numeroNomina, confirmationResponse.msgStatus(), confirmationResponse.messageText());
                controlNominaService.markError(jobExecutionId, numeroNomina, message);
                throw new ArtikosIntegrationException(message);
            }
            LOGGER.info("Artikos confirmation OK profile={} numeroNomina={}", item.profile(), numeroNomina);
            LoggingContext.clearOperation();

            return processLocally(item);
        } catch (ArtikosIntegrationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            controlNominaService.markError(jobExecutionId, numeroNomina, exception.getMessage());
            throw new ArtikosIntegrationException("Fallo la confirmacion de nomina Artikos", exception);
        } finally {
            LoggingContext.clearAll();
        }
    }

    private boolean isAlreadyOutsideIntegrationState(String messageText) {
        return messageText != null
                && messageText.contains("Solo se puede confirmar la recepci")
                && messageText.contains("estado En Integraci");
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
