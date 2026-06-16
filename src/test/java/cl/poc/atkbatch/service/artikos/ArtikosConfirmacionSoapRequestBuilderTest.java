package cl.poc.atkbatch.service.artikos;

import static org.assertj.core.api.Assertions.assertThat;

import cl.poc.atkbatch.domain.artikos.ArtikosProfileConfig;
import org.junit.jupiter.api.Test;

class ArtikosConfirmacionSoapRequestBuilderTest {

    private final ArtikosConfirmacionSoapRequestBuilder builder = new ArtikosConfirmacionSoapRequestBuilder();

    @Test
    void buildsNomfactconfirRequest() {
        String request = builder.buildNomfactconfirRequest(profileConfig(), 15961L, 0);

        assertThat(request).contains("<EjecutaTrx xmlns=\"AtkWs_DocConnectorB2B\">");
        assertThat(request).contains("<msgCode>NOMFACTCONFIR</msgCode>");
        assertThat(request).contains("&lt;MsgCode&gt;NOMFACTCONFIR&lt;/MsgCode&gt;");
        assertThat(request).contains("&lt;NumeroNomina&gt;15961&lt;/NumeroNomina&gt;");
        assertThat(request).contains("&lt;EstadoRespuesta&gt;0&lt;/EstadoRespuesta&gt;");
        assertThat(request).contains("&lt;MsgFromAddress&gt;ZSGRALES&lt;/MsgFromAddress&gt;");
        assertThat(request).doesNotContain("http://tempuri.org/");
        assertThat(builder.maskToken(request)).contains("<token>****</token>");
    }

    private ArtikosProfileConfig profileConfig() {
        ArtikosProfileConfig profileConfig = new ArtikosProfileConfig();
        profileConfig.setToken("TOKEN_GENERALES");
        profileConfig.setMsgFromAddress("ZSGRALES");
        profileConfig.setMsgCodFromAddress("CODGEN");
        profileConfig.setMsgToAddress("ARTIKOS");
        profileConfig.setMsgCodSis("SAF");
        profileConfig.setMsgCodExterno("EXTGEN");
        return profileConfig;
    }
}
