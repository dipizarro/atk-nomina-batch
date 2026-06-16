package cl.poc.atkbatch.service.artikos;

import static org.assertj.core.api.Assertions.assertThat;

import cl.poc.atkbatch.domain.artikos.ArtikosProfileConfig;
import org.junit.jupiter.api.Test;

class ArtikosNominaSoapRequestBuilderTest {

    private final ArtikosNominaSoapRequestBuilder builder = new ArtikosNominaSoapRequestBuilder();

    @Test
    void buildsNomfacterpRequest() {
        String request = builder.buildNomfacterpRequest(profileConfig());

        assertThat(request).contains("<EjecutaTrx xmlns=\"AtkWs_DocExtractor\">");
        assertThat(request).doesNotContain("http://tempuri.org/");
        assertThat(request).contains("<msgCode>NOMFACTERP</msgCode>");
        assertThat(request).contains("<msgFromAdress>ZSVIDA</msgFromAdress>");
        assertThat(request).contains("<MsgCodFromAdress>CODVIDA</MsgCodFromAdress>");
        assertThat(request).contains("<msgToAdress>ARTIKOS</msgToAdress>");
        assertThat(request).contains("<msgCodSis>SAF</msgCodSis>");
        assertThat(request).contains("<msgCodExterno>EXTVIDA</msgCodExterno>");
        assertThat(request).doesNotContain("<token></token>");
        assertThat(builder.maskToken(request)).contains("<token>****</token>");
    }

    private ArtikosProfileConfig profileConfig() {
        ArtikosProfileConfig profileConfig = new ArtikosProfileConfig();
        profileConfig.setToken("TOKEN_VIDA");
        profileConfig.setMsgFromAddress("ZSVIDA");
        profileConfig.setMsgCodFromAddress("CODVIDA");
        profileConfig.setMsgToAddress("ARTIKOS");
        profileConfig.setMsgCodSis("SAF");
        profileConfig.setMsgCodExterno("EXTVIDA");
        return profileConfig;
    }
}
