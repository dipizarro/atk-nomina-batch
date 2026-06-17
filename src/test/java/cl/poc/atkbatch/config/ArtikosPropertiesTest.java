package cl.poc.atkbatch.config;

import static org.assertj.core.api.Assertions.assertThat;

import cl.poc.atkbatch.domain.artikos.ArtikosOperationConfig;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationType;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import cl.poc.atkbatch.service.artikos.ArtikosTokenMasker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArtikosPropertiesTest {

    @Autowired
    private ArtikosProperties properties;

    @Test
    void bindsOperationSpecificProperties() {
        assertThat(properties.getEndpoints().getNominaUrl()).contains("AtkWS_DocExtractorB2B.asmx");
        assertThat(properties.getEndpoints().getConnectorUrl()).contains("AtkWS_DocConnectorB2B.asmx");

        ArtikosOperationConfig consumo = properties.requireOperationConfig(
                ArtikosProfileType.GENERALES,
                ArtikosOperationType.CONSUMO_NOMINA);
        ArtikosOperationConfig respuesta = properties.requireOperationConfig(
                ArtikosProfileType.GENERALES,
                ArtikosOperationType.RESPUESTA_NOMINA);
        ArtikosOperationConfig resultado = properties.requireOperationConfig(
                ArtikosProfileType.GENERALES,
                ArtikosOperationType.RESULTADO_NOMINA);

        assertThat(consumo.getMsgCode()).isEqualTo("NOMFACTERP");
        assertThat(consumo.getToken()).isEqualTo("TEST_TOKEN_GENERALES_CONSUMO");
        assertThat(respuesta.getMsgCode()).isEqualTo("NOMFACTCONFIR");
        assertThat(respuesta.getToken()).isEqualTo("TEST_TOKEN_GENERALES_RESPUESTA");
        assertThat(resultado.getMsgCode()).isEqualTo("NOMFACTRES");
        assertThat(resultado.getToken()).isEqualTo("TEST_TOKEN_GENERALES_RESULTADO");
    }

    @Test
    void masksTokenWithoutExposingFullValue() {
        String token = "ABCD12345678WXYZ";

        assertThat(ArtikosTokenMasker.isPresent(token)).isTrue();
        assertThat(ArtikosTokenMasker.mask(token)).isEqualTo("ABCD****WXYZ");
        assertThat(ArtikosTokenMasker.mask(token)).doesNotContain("12345678");
    }
}
