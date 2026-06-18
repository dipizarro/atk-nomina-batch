package cl.atk.nomina.batch.service.artikos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cl.atk.nomina.batch.config.ArtikosProperties;
import cl.atk.nomina.batch.config.ArtikosRetryProperties;
import cl.atk.nomina.batch.domain.artikos.ArtikosOperationConfig;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileConfig;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.service.NominaResultXmlService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ArtikosSoapClientRetryTest {

    @Test
    void retriesTechnicalHttp5xxAndReturnsSuccessfulBody() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://artikos.test/nominas"))
                .andRespond(withServerError());
        server.expect(once(), requestTo("https://artikos.test/nominas"))
                .andRespond(withSuccess(successSoap(), MediaType.TEXT_XML));
        ArtikosSoapClient client = client(builder.build(), retryProperties(true, 2));

        String response = client.fetchNominaRawXml(ArtikosProfileType.VIDA);

        assertThat(response).contains("<MsgStatus>0</MsgStatus>");
        server.verify();
    }

    @Test
    void doesNotRetryFunctionalSoapErrorWithHttp200() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://artikos.test/nominas"))
                .andRespond(withSuccess(functionalErrorSoap(), MediaType.TEXT_XML));
        ArtikosSoapClient client = client(builder.build(), retryProperties(true, 3));

        String response = client.fetchNominaRawXml(ArtikosProfileType.VIDA);

        assertThat(response).contains("<MsgStatus>1</MsgStatus>");
        server.verify();
    }

    private ArtikosSoapClient client(RestClient restClient, ArtikosRetryProperties retryProperties) {
        return new ArtikosSoapClient(
                properties(),
                new ArtikosNominaSoapRequestBuilder(),
                new ArtikosConfirmacionSoapRequestBuilder(),
                new ArtikosResultadoSoapRequestBuilder(),
                new NominaResultXmlService(),
                retryProperties,
                restClient);
    }

    private ArtikosRetryProperties retryProperties(boolean enabled, int maxAttempts) {
        ArtikosRetryProperties properties = new ArtikosRetryProperties();
        properties.setEnabled(enabled);
        properties.setMaxAttempts(maxAttempts);
        properties.setBackoffMs(0);
        return properties;
    }

    private ArtikosProperties properties() {
        ArtikosProperties properties = new ArtikosProperties();
        ArtikosProperties.Endpoints endpoints = new ArtikosProperties.Endpoints();
        endpoints.setNominaUrl("https://artikos.test/nominas");
        endpoints.setConnectorUrl("https://artikos.test/connector");
        properties.setEndpoints(endpoints);
        properties.setNominaSoapAction("\"AtkWs_DocExtractor/EjecutaTrx\"");
        properties.setConnectorSoapAction("\"AtkWs_DocConnectorB2B/EjecutaTrx\"");
        properties.setProfiles(Map.of(
                ArtikosProfileType.VIDA, profileConfig(),
                ArtikosProfileType.GENERALES, profileConfig()));
        return properties;
    }

    private ArtikosProfileConfig profileConfig() {
        ArtikosProfileConfig profileConfig = new ArtikosProfileConfig();
        profileConfig.setConsumoNomina(operationConfig("NOMFACTERP"));
        profileConfig.setRespuestaNomina(operationConfig("NOMFACTCONFIR"));
        profileConfig.setResultadoNomina(operationConfig("NOMFACTRES"));
        return profileConfig;
    }

    private ArtikosOperationConfig operationConfig(String msgCode) {
        ArtikosOperationConfig operationConfig = new ArtikosOperationConfig();
        operationConfig.setToken("TOKEN");
        operationConfig.setMsgCode(msgCode);
        operationConfig.setMsgFromAddress("ZSVIDA");
        operationConfig.setMsgCodFromAddress("96819630-8");
        operationConfig.setMsgToAddress("ARTIKOS");
        operationConfig.setMsgCodSis("SAF");
        operationConfig.setMsgCodExterno("EXT");
        return operationConfig;
    }

    private String successSoap() {
        return "<Message><MessageId><MsgStatus>0</MsgStatus></MessageId></Message>";
    }

    private String functionalErrorSoap() {
        return "<Message><MessageId><MsgStatus>1</MsgStatus></MessageId>"
                + "<MessageOut><LogMessage><MessageText>Error funcional</MessageText></LogMessage></MessageOut>"
                + "</Message>";
    }
}
