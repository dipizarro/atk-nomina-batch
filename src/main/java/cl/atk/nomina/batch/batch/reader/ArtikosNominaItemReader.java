package cl.atk.nomina.batch.batch.reader;

import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosOperation;
import cl.atk.nomina.batch.domain.artikos.ArtikosFetchedNomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.domain.error.IntegrationErrorType;
import cl.atk.nomina.batch.service.artikos.ArtikosSoapClient;
import cl.atk.nomina.batch.service.artikos.ArtikosSoapClientException;
import cl.atk.nomina.batch.service.artikos.ArtikosSoapResponseParser;
import cl.atk.nomina.batch.shared.exception.ArtikosIntegrationException;
import cl.atk.nomina.batch.shared.exception.NominaXmlParsingException;
import cl.atk.nomina.batch.shared.logging.LoggingContext;
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
        this.maxNominas = maxNominas == null ? Long.MAX_VALUE : maxNominas;
        this.dryRun = Boolean.parseBoolean(dryRun);
    }

    @Override
    public ArtikosFetchedNomina read() {
        LoggingContext.putProfile(profile.name());
        LoggingContext.putOperation(ArtikosOperation.NOMFACTERP.name());
        if (fetchedCount >= maxNominas) {
            LOGGER.info("Artikos reader reached operational safety limit maxNominas={} profile={} dryRun={} fetchedCount={}",
                    maxNominas, profile, dryRun, fetchedCount);
            LoggingContext.clearOperation();
            LoggingContext.clearNomina();
            return null;
        }

        try {
            LOGGER.info("Fetching Artikos nomina profile={} currentCount={} maxNominas={} dryRun={}",
                    profile, fetchedCount, maxNominas, dryRun);
            String rawXml = soapClient.fetchNominaRawXml(profile);
            if (responseParser.isNoNominasResponse(rawXml)) {
                LOGGER.info("Artikos returned no nominas profile={} fetchedCount={} message={}",
                        profile, fetchedCount, responseParser.extractNoNominasMessage(rawXml));
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
        } catch (ArtikosSoapClientException exception) {
            throw new ArtikosIntegrationException(
                    IntegrationErrorType.ARTIKOS_FETCH_ERROR,
                    profile.name(),
                    null,
                    ArtikosOperation.NOMFACTERP.name(),
                    exception.getMessage(),
                    exception);
        } catch (NominaXmlParsingException exception) {
            throw new ArtikosIntegrationException(
                    IntegrationErrorType.XML_PARSING_ERROR,
                    profile.name(),
                    null,
                    ArtikosOperation.NOMFACTERP.name(),
                    exception.getMessage(),
                    exception);
        } catch (RuntimeException exception) {
            throw new ArtikosIntegrationException(
                    IntegrationErrorType.ARTIKOS_FETCH_ERROR,
                    profile.name(),
                    null,
                    ArtikosOperation.NOMFACTERP.name(),
                    "No fue posible consultar nomina en Artikos QA",
                    exception);
        } finally {
            LoggingContext.clearOperation();
            LoggingContext.clearNomina();
        }
    }
}
