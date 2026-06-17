package cl.atk.nomina.batch.service;

import static org.assertj.core.api.Assertions.assertThat;

import cl.atk.nomina.batch.domain.ResultadoDocumento;
import cl.atk.nomina.batch.domain.ResultadoNomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosOperationConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class NominaResultXmlServiceTest {

    private final NominaResultXmlService service = new NominaResultXmlService();

    @Test
    void buildsNomfactresXmlUsingResultadoNominaConfig() {
        ResultadoDocumento documento = new ResultadoDocumento(
                null,
                "OK",
                "Documento procesado correctamente",
                "3151100",
                "96670840-9",
                "33",
                new BigDecimal("21850"));
        ResultadoNomina result = new ResultadoNomina(
                null,
                15961L,
                1,
                1,
                0,
                0,
                0,
                List.of(documento),
                "",
                "OK",
                null);

        String xml = service.buildNomfactresXml(result, operationConfig());

        assertThat(xml).contains("<MsgCode>NOMFACTRES</MsgCode>");
        assertThat(xml).contains("<MsgDesc>Actualizacion de carga de documentos</MsgDesc>");
        assertThat(xml).contains("<MsgVersion>V2.0</MsgVersion>");
        assertThat(xml).contains("<MsgFromAddress>ZSGRALES</MsgFromAddress>");
        assertThat(xml).contains("<MsgToAddress>ARTIKOS</MsgToAddress>");
        assertThat(xml).contains("<MsgCodSis>SAF</MsgCodSis>");
        assertThat(xml).contains("<NumeroNomina>15961</NumeroNomina>");
        assertThat(xml).contains("<CantidadOK>1</CantidadOK>");
        assertThat(xml).contains("<CantidadNOK>0</CantidadNOK>");
        assertThat(xml).contains("<CantidadInformados>1</CantidadInformados>");
        assertThat(xml).contains("<DocFolio>3151100</DocFolio>");
        assertThat(xml).contains("<DocRutProveedor>96670840-9</DocRutProveedor>");
        assertThat(xml).contains("<DocTipoDoc>33</DocTipoDoc>");
        assertThat(xml).contains("<Monto>21850</Monto>");
        assertThat(xml).contains("<DocEstado>OK</DocEstado>");
    }

    private ArtikosOperationConfig operationConfig() {
        ArtikosOperationConfig operationConfig = new ArtikosOperationConfig();
        operationConfig.setMsgCode("NOMFACTRES");
        operationConfig.setMsgFromAddress("ZSGRALES");
        operationConfig.setMsgToAddress("ARTIKOS");
        operationConfig.setMsgCodSis("SAF");
        return operationConfig;
    }
}
