package cl.poc.atkbatch.service.artikos;

import cl.poc.atkbatch.config.ArtikosProperties;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationConfig;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationType;
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
        ArtikosOperationConfig operationConfig = artikosProperties.requireOperationConfig(
                profileType,
                ArtikosOperationType.CONSUMO_NOMINA);
        String endpoint = artikosProperties.getEndpoints().getNominaUrl();
        String requestXml = requestBuilder.buildNomfacterpRequest(operationConfig);

        try {
            logOperation(profileType, ArtikosOperationType.CONSUMO_NOMINA, endpoint, operationConfig);
            LOGGER.debug("Artikos NOMFACTERP request profile={} xml={}",
                    profileType, requestBuilder.maskToken(requestXml));

            return postSoap(
                    endpoint,
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
        ArtikosOperationConfig operationConfig = artikosProperties.requireOperationConfig(
                profileType,
                ArtikosOperationType.RESPUESTA_NOMINA);
        String endpoint = artikosProperties.getEndpoints().getConnectorUrl();
        String requestXml = confirmacionRequestBuilder.buildNomfactconfirRequest(
                operationConfig,
                numeroNomina,
                estadoRespuesta);

        try {
            logOperation(profileType, ArtikosOperationType.RESPUESTA_NOMINA, endpoint, operationConfig);
            LOGGER.info("Artikos NOMFACTCONFIR request shape profile={} numeroNomina={} {}",
                    profileType, numeroNomina, confirmacionRequestBuilder.describeContractShape(requestXml));
            LOGGER.debug("Artikos NOMFACTCONFIR request profile={} numeroNomina={} xml={}",
                    profileType, numeroNomina, confirmacionRequestBuilder.maskToken(requestXml));

            return postSoap(
                    endpoint,
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

    public ArtikosOperationConfig resultadoNominaConfig(ArtikosProfileType profileType) {
        return artikosProperties.requireOperationConfig(profileType, ArtikosOperationType.RESULTADO_NOMINA);
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

    private void logOperation(
            ArtikosProfileType profileType,
            ArtikosOperationType operationType,
            String endpoint,
            ArtikosOperationConfig operationConfig) {
        LOGGER.info("Calling Artikos QA SOAP endpoint profile={} operation={} endpoint={} msgCode={} "
                        + "msgFromAddress={} msgCodFromAddress={} msgToAddress={} msgCodSis={} "
                        + "tokenPresent={} tokenMasked={}",
                profileType,
                operationType.getPropertyName(),
                endpoint,
                operationConfig.getMsgCode(),
                operationConfig.getMsgFromAddress(),
                operationConfig.getMsgCodFromAddress(),
                operationConfig.getMsgToAddress(),
                operationConfig.getMsgCodSis(),
                ArtikosTokenMasker.isPresent(operationConfig.getToken()),
                ArtikosTokenMasker.mask(operationConfig.getToken()));
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
