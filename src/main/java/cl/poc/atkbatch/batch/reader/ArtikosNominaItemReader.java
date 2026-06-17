package cl.poc.atkbatch.batch.reader;

import cl.poc.atkbatch.domain.Nomina;
import cl.poc.atkbatch.domain.artikos.ArtikosOperation;
import cl.poc.atkbatch.domain.artikos.ArtikosFetchedNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import cl.poc.atkbatch.service.artikos.ArtikosSoapClient;
import cl.poc.atkbatch.service.artikos.ArtikosSoapResponseParser;
import cl.poc.atkbatch.shared.exception.ArtikosIntegrationException;
import cl.poc.atkbatch.shared.logging.LoggingContext;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

public class ArtikosNominaItemReader implements ItemReader<ArtikosFetchedNomina> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikosNominaItemReader.class);

    private final ArtikosSoapClient soapClient;
    private final ArtikosSoapResponseParser responseParser;
    private final ArtikosProfileType profile;
    private final long maxNominas;
    private final boolean dryRun;
    private long fetchedCount;

    public ArtikosNominaItemReader(
            ArtikosSoapClient soapClient,
            ArtikosSoapResponseParser responseParser,
            String profile,
            Long maxNominas,
            String dryRun) {
        this.soapClient = soapClient;
        this.responseParser = responseParser;
        this.profile = ArtikosProfileType.from(profile);
        this.maxNominas = maxNominas == null ? 1L : maxNominas;
        this.dryRun = Boolean.parseBoolean(dryRun);
    }

    @Override
    public ArtikosFetchedNomina read() {
        LoggingContext.putProfile(profile.name());
        LoggingContext.putOperation(ArtikosOperation.NOMFACTERP.name());
        if (fetchedCount >= maxNominas) {
            LOGGER.info("Artikos reader reached maxNominas={} profile={} dryRun={}", maxNominas, profile, dryRun);
            LoggingContext.clearOperation();
            LoggingContext.clearNomina();
            return null;
        }

        try {
            LOGGER.info("Fetching Artikos nomina profile={} currentCount={} maxNominas={} dryRun={}",
                    profile, fetchedCount, maxNominas, dryRun);
            String rawXml = soapClient.fetchNominaRawXml(profile);
            if (responseParser.isNoNominasResponse(rawXml)) {
                LOGGER.info("Artikos returned no nominas profile={} message={}",
                        profile, responseParser.extractNoNominasMessage(rawXml));
                return null;
            }

            Optional<Nomina> parsedNomina = responseParser.extractNomina(rawXml);
            if (parsedNomina.isEmpty()) {
                LOGGER.info("Artikos response did not contain a nomina profile={}", profile);
                return null;
            }

            Nomina nomina = parsedNomina.get();
            fetchedCount++;
            LoggingContext.putNumeroNomina(nomina.cabecera().numeroNomina());
            LOGGER.info("Artikos nomina received profile={} numeroNomina={} tipoNomina={} cantidadDocumentos={}",
                    profile,
                    nomina.cabecera().numeroNomina(),
                    nomina.cabecera().tipoNomina(),
                    nomina.cabecera().cantidadDocumentos());
            return new ArtikosFetchedNomina(
                    profile,
                    nomina,
                    nomina.cabecera().numeroNomina(),
                    nomina.cabecera().tipoNomina(),
                    nomina.cabecera().cantidadDocumentos(),
                    rawXml,
                    dryRun);
        } catch (RuntimeException exception) {
            throw new ArtikosIntegrationException("No fue posible consultar nomina en Artikos QA", exception);
        } finally {
            LoggingContext.clearOperation();
            LoggingContext.clearNomina();
        }
    }
}
