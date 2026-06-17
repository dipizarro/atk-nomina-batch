package cl.poc.atkbatch.batch.writer;

import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosGenericResponse;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import cl.poc.atkbatch.service.BatchResultStore;
import cl.poc.atkbatch.service.ControlNominaService;
import cl.poc.atkbatch.service.artikos.ArtikosGenericSoapResponseParser;
import cl.poc.atkbatch.service.artikos.ArtikosSoapClient;
import cl.poc.atkbatch.shared.exception.ArtikosIntegrationException;
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
    private final BatchResultStore batchResultStore;
    private final ArtikosProfileType profile;
    private final boolean dryRun;
    private final Long jobExecutionId;

    public ArtikosNominaResultItemWriter(
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericResponseParser,
            ControlNominaService controlNominaService,
            BatchResultStore batchResultStore,
            String profile,
            String dryRun,
            Long jobExecutionId) {
        this.soapClient = soapClient;
        this.genericResponseParser = genericResponseParser;
        this.controlNominaService = controlNominaService;
        this.batchResultStore = batchResultStore;
        this.profile = ArtikosProfileType.from(profile);
        this.dryRun = Boolean.parseBoolean(dryRun);
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public void write(Chunk<? extends ResultadoNomina> chunk) {
        List<ResultadoNomina> completedResults = new ArrayList<>();
        for (ResultadoNomina result : chunk.getItems()) {
            ResultadoNomina resultWithJob = result.withJobExecutionId(jobExecutionId);
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
    }

    private void sendResultToArtikos(ResultadoNomina result) {
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
                controlNominaService.markError(jobExecutionId, result.numeroNomina(), message);
                throw new ArtikosIntegrationException(message);
            }

            LOGGER.info("Artikos NOMFACTRES OK profile={} jobExecutionId={} numeroNomina={}",
                    profile, jobExecutionId, result.numeroNomina());
            controlNominaService.markCompleted(result);
        } catch (ArtikosIntegrationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            controlNominaService.markError(jobExecutionId, result.numeroNomina(), exception.getMessage());
            throw new ArtikosIntegrationException("Fallo el envio NOMFACTRES a Artikos", exception);
        }
    }
}
