package cl.poc.atkbatch.service.artikos;

import cl.poc.atkbatch.config.ArtikosProperties;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileConfig;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ArtikosSoapClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikosSoapClient.class);
    private static final String DEFAULT_EXTRACTOR_SOAP_ACTION = "\"AtkWs_DocExtractor/EjecutaTrx\"";
    private static final String DEFAULT_CONNECTOR_SOAP_ACTION = "\"AtkWs_DocConnectorB2B/EjecutaTrx\"";

    private final ArtikosProperties artikosProperties;
    private final ArtikosNominaSoapRequestBuilder requestBuilder;
    private final ArtikosConfirmacionSoapRequestBuilder confirmacionRequestBuilder;
    private final RestClient restClient;

    public ArtikosSoapClient(
            ArtikosProperties artikosProperties,
            ArtikosNominaSoapRequestBuilder requestBuilder,
            ArtikosConfirmacionSoapRequestBuilder confirmacionRequestBuilder,
            RestClient.Builder restClientBuilder) {
        this.artikosProperties = artikosProperties;
        this.requestBuilder = requestBuilder;
        this.confirmacionRequestBuilder = confirmacionRequestBuilder;
        this.restClient = restClientBuilder.build();
    }

    public String fetchNominaRawXml(ArtikosProfileType profileType) {
        ArtikosProfileConfig profileConfig = artikosProperties.requireProfile(profileType);
        String requestXml = requestBuilder.buildNomfacterpRequest(profileConfig);

        try {
            LOGGER.info("Calling Artikos QA nomina SOAP endpoint profile={} endpoint={}",
                    profileType, artikosProperties.getNominaUrl());
            LOGGER.debug("Artikos NOMFACTERP request profile={} xml={}",
                    profileType, requestBuilder.maskToken(requestXml));

            return postSoap(
                    artikosProperties.getNominaUrl(),
                    requestXml,
                    resolveSoapAction(
                            artikosProperties.getNominaSoapAction(),
                            artikosProperties.getSoapAction(),
                            DEFAULT_EXTRACTOR_SOAP_ACTION),
                    "nomina",
                    profileType,
                    null);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (ArtikosSoapClientException exception) {
            throw exception;
        } catch (RestClientException exception) {
            LOGGER.warn("Artikos QA nomina SOAP connection error profile={} cause={}",
                    profileType, exception.getMessage(), exception);
            throw new ArtikosSoapClientException("No fue posible consultar nominas en Artikos QA", exception);
        }
    }

    public String confirmNominaRawXml(
            ArtikosProfileType profileType,
            Long numeroNomina,
            Integer estadoRespuesta) {
        ArtikosProfileConfig profileConfig = artikosProperties.requireProfile(profileType);
        String requestXml = confirmacionRequestBuilder.buildNomfactconfirRequest(
                profileConfig,
                numeroNomina,
                estadoRespuesta);

        try {
            LOGGER.info("Calling Artikos QA confirmation SOAP endpoint profile={} numeroNomina={} endpoint={}",
                    profileType, numeroNomina, artikosProperties.getConnectorUrl());
            LOGGER.debug("Artikos NOMFACTCONFIR request profile={} numeroNomina={} xml={}",
                    profileType, numeroNomina, confirmacionRequestBuilder.maskToken(requestXml));

            return postSoap(
                    artikosProperties.getConnectorUrl(),
                    requestXml,
                    resolveSoapAction(
                            artikosProperties.getConnectorSoapAction(),
                            artikosProperties.getSoapAction(),
                            DEFAULT_CONNECTOR_SOAP_ACTION),
                    "confirmation",
                    profileType,
                    numeroNomina);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (ArtikosSoapClientException exception) {
            throw exception;
        } catch (RestClientException exception) {
            LOGGER.warn("Artikos QA confirmation SOAP connection error profile={} numeroNomina={} cause={}",
                    profileType, numeroNomina, exception.getMessage(), exception);
            throw new ArtikosSoapClientException("No fue posible confirmar recepcion de nomina en Artikos QA",
                    exception);
        }
    }

    private String postSoap(
            String endpoint,
            String requestXml,
            String soapAction,
            String operation,
            ArtikosProfileType profileType,
            Long numeroNomina) {
        return restClient.post()
                .uri(URI.create(endpoint))
                .contentType(MediaType.parseMediaType("text/xml; charset=utf-8"))
                .headers(headers -> applySoapAction(headers, soapAction))
                .body(requestXml)
                .exchange((request, response) -> {
                    HttpStatusCode statusCode = response.getStatusCode();
                    String responseBody = readBody(response.getBody());
                    LOGGER.info("Artikos QA {} SOAP response profile={} numeroNomina={} status={}",
                            operation, profileType, numeroNomina, statusCode);
                    if (statusCode.isError()) {
                        String safeBody = compact(responseBody);
                        LOGGER.warn("Artikos QA {} SOAP error profile={} numeroNomina={} status={} body={}",
                                operation, profileType, numeroNomina, statusCode, safeBody);
                        throw new ArtikosSoapClientException(
                                "Artikos QA respondio HTTP " + statusCode.value() + ": " + safeBody);
                    }
                    return responseBody;
                });
    }

    private void applySoapAction(HttpHeaders headers, String soapAction) {
        headers.add("SOAPAction", soapAction);
    }

    private String resolveSoapAction(
            String configuredOperationSoapAction,
            String configuredLegacySoapAction,
            String defaultSoapAction) {
        if (StringUtils.hasText(configuredOperationSoapAction)) {
            return configuredOperationSoapAction;
        }
        if (StringUtils.hasText(configuredLegacySoapAction)) {
            return configuredLegacySoapAction;
        }
        return defaultSoapAction;
    }

    private String readBody(java.io.InputStream body) throws IOException {
        return StreamUtils.copyToString(body, StandardCharsets.UTF_8);
    }

    private String compact(String body) {
        String compactBody = body == null ? "" : body.replaceAll("\\s+", " ").trim();
        return compactBody.length() <= 2000 ? compactBody : compactBody.substring(0, 2000) + "...";
    }
}
