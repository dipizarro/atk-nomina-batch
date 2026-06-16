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
    private static final String DEFAULT_SOAP_ACTION = "\"AtkWs_DocExtractor/EjecutaTrx\"";

    private final ArtikosProperties artikosProperties;
    private final ArtikosNominaSoapRequestBuilder requestBuilder;
    private final RestClient restClient;

    public ArtikosSoapClient(
            ArtikosProperties artikosProperties,
            ArtikosNominaSoapRequestBuilder requestBuilder,
            RestClient.Builder restClientBuilder) {
        this.artikosProperties = artikosProperties;
        this.requestBuilder = requestBuilder;
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

            return restClient.post()
                    .uri(URI.create(artikosProperties.getNominaUrl()))
                    .contentType(MediaType.parseMediaType("text/xml; charset=utf-8"))
                    .headers(headers -> applySoapAction(headers, artikosProperties.getSoapAction()))
                    .body(requestXml)
                    .exchange((request, response) -> {
                        HttpStatusCode statusCode = response.getStatusCode();
                        String responseBody = readBody(response.getBody());
                        LOGGER.info("Artikos QA nomina SOAP response profile={} status={}", profileType, statusCode);
                        if (statusCode.isError()) {
                            String safeBody = compact(responseBody);
                            LOGGER.warn("Artikos QA nomina SOAP error profile={} status={} body={}",
                                    profileType, statusCode, safeBody);
                            throw new ArtikosSoapClientException(
                                    "Artikos QA respondio HTTP " + statusCode.value() + ": " + safeBody);
                        }
                        return responseBody;
                    });
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

    private void applySoapAction(HttpHeaders headers, String soapAction) {
        String resolvedSoapAction = StringUtils.hasText(soapAction) ? soapAction : DEFAULT_SOAP_ACTION;
        headers.add("SOAPAction", resolvedSoapAction);
    }

    private String readBody(java.io.InputStream body) throws IOException {
        return StreamUtils.copyToString(body, StandardCharsets.UTF_8);
    }

    private String compact(String body) {
        String compactBody = body == null ? "" : body.replaceAll("\\s+", " ").trim();
        return compactBody.length() <= 2000 ? compactBody : compactBody.substring(0, 2000) + "...";
    }
}
