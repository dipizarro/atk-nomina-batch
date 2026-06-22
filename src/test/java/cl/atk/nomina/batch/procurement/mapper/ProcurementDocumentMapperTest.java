package cl.atk.nomina.batch.procurement.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.atk.nomina.batch.domain.DocumentoContable;
import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.procurement.config.ProcurementMappingProperties;
import cl.atk.nomina.batch.procurement.dto.ProcurementDocumentRequest;
import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import cl.atk.nomina.batch.service.NominaXmlParserService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ProcurementDocumentMapperTest {

    private final NominaXmlParserService parser = new NominaXmlParserService(
            new ClassPathResource("samples/ZSVIDA_Nom15960.xml"));

    @Test
    void mapsArtikosDocumentToProcurementCmpRequestForGenerales() {
        Nomina nomina = parser.parseSampleFile();
        DocumentoContable documento = nomina.documentos().get(0);

        ProcurementDocumentRequest request = mapper(defaultProperties())
                .toCmpDocumentRequest(ArtikosProfileType.GENERALES, nomina, documento);

        assertThat(request.codTipDocumt()).isEqualTo("CMP");
        assertThat(request.hnr()).isNull();
        assertThat(request.cmp()).isNotNull();
        assertThat(request.cmp().cmpDocumt().codTipDocumt()).isEqualTo("FEC");
        assertThat(request.cmp().cmpDocumt().codEmpres()).isEqualTo("002");
        assertThat(request.cmp().cmpDocumt().codSistem()).isEqualTo("CM");
        assertThat(request.cmp().cmpDocumt().numRut()).isEqualTo(96670840L);
        assertThat(request.cmp().cmpDocumt().numDoccmp()).isEqualTo("2");
        assertThat(request.cmp().cmpDocumt().codCuenta()).isEqualTo("6130401000");
        assertThat(request.cmp().cmpDocumt().codTipCuenta()).isEqualTo("2");
        assertThat(request.cmp().cmpDocumt().codContbl()).isEqualTo("CONTBL");
        assertThat(request.cmp().cmpDocumt().codMoneda()).isEqualTo("CLP");
        assertThat(request.cmp().cmpDocumt().fecEmidcm()).isEqualTo("2026-06-03");
        assertThat(request.cmp().cmpDocumt().fechaRecFe()).isEqualTo("2026-06-03");
        assertThat(request.cmp().cmpDocumt().mtoTotNtodig()).isEqualByComparingTo("19000");
        assertThat(request.cmp().cmpDocumt().mtoTotExndig()).isEqualByComparingTo("0");
        assertThat(request.cmp().cmpDocumt().mtoTotIvadig()).isEqualByComparingTo("2850");
        assertThat(request.cmp().cmpDocumt().mtoTotDocdig()).isEqualByComparingTo("21850");
        assertThat(request.cmp().cmpDocumt().numFolDocumt()).isEqualTo(2L);

        assertThat(request.cmp().cmpDocumtDet()).hasSize(2);
        assertThat(request.cmp().cmpDocumtDet().get(0).numLinDoccmp()).isEqualTo(1);
        assertThat(request.cmp().cmpDocumtDet().get(1).numLinDoccmp()).isEqualTo(2);
        assertThat(request.cmp().cmpDocumtDet().get(0).codCcosto()).isEqualTo("20001");
        assertThat(request.cmp().cmpDocumtDet().get(0).codCuenta()).isEqualTo("6130401000");
        assertThat(request.cmp().cmpDocumtDet().get(0).codTipCuenta()).isEqualTo("2");
        assertThat(request.cmp().cmpDocumtDet().get(0).glsLinea()).isEqualTo("BENEFICIOS AL PERSONAL");
        assertThat(request.cmp().cmpDocumtDet().get(0).mtoNeto()).isEqualByComparingTo("15000");
        assertThat(request.cmp().cmpDocumtDet().get(0).mtoIvaclc()).isEqualByComparingTo("2090");
        assertThat(request.cmp().cmpDocumtDet().get(0).mtoTotItem()).isEqualByComparingTo("17090");
        assertThat(request.cmp().cmpDocumtDet().get(1).mtoNeto()).isEqualByComparingTo("4000");
        assertThat(request.cmp().cmpDocumtDet().get(1).mtoTotItem()).isEqualByComparingTo("4760");
        assertThat(request.cmp().cmpDocumtDetRut().cmpNumRut()).isEqualTo(96670840L);
        assertThat(request.cmp().cmpDocumtDetRut().numRut()).isEqualTo(96670840L);
        assertThat(request.cmp().cmpDocumtDetRut().aIndVige()).isEqualTo("V");
    }

    @Test
    void mapsCompanyByProfileVida() {
        Nomina nomina = parser.parseSampleFile();

        ProcurementDocumentRequest request = mapper(defaultProperties())
                .toCmpDocumentRequest(ArtikosProfileType.VIDA, nomina, nomina.documentos().get(0));

        assertThat(request.cmp().cmpDocumt().codEmpres()).isEqualTo("001");
    }

    @Test
    void alwaysMapsCurrencyFromProperties() {
        Nomina nomina = parser.parseSampleFile();
        DocumentoContable source = nomina.documentos().get(0);
        DocumentoContable documento = new DocumentoContable(
                source.secuencia(),
                source.rutProveedor(),
                source.proveedor(),
                source.nacional(),
                source.idDocumento(),
                source.usuario(),
                source.numeroDocumento(),
                source.tipoDocumento(),
                source.tipoErp(),
                source.fechaEmision(),
                source.fechaVencimiento(),
                source.fechaRecepcion(),
                source.fechaRecepSii(),
                source.urlDocumento(),
                source.observacion(),
                "USD",
                source.montoNeto(),
                source.montoIva(),
                source.montoExento(),
                source.otrosImpuestos(),
                source.montoTotal(),
                source.referencias(),
                source.conciliaciones());

        ProcurementMappingProperties properties = defaultProperties();
        properties.setDefaultCurrency("CLP");

        ProcurementDocumentRequest request = mapper(properties)
                .toCmpDocumentRequest(ArtikosProfileType.VIDA, nomina, documento);

        assertThat(request.cmp().cmpDocumt().codMoneda()).isEqualTo("CLP");
    }

    @Test
    void throwsClearExceptionWhenRequiredPropertyIsMissing() {
        ProcurementMappingProperties properties = defaultProperties();
        properties.setCodContbl(null);

        Nomina nomina = parser.parseSampleFile();

        assertThatThrownBy(() -> mapper(properties)
                .toCmpDocumentRequest(ArtikosProfileType.VIDA, nomina, nomina.documentos().get(0)))
                .isInstanceOf(ProcurementMappingException.class)
                .hasMessage("Missing procurement mapping property: procurement.mapping.cod-contbl");
    }

    private ProcurementDocumentMapper mapper(ProcurementMappingProperties properties) {
        return new ProcurementDocumentMapper(
                properties,
                new ProcurementMappingValidator(),
                new ProcurementDateMapper());
    }

    private ProcurementMappingProperties defaultProperties() {
        ProcurementMappingProperties properties = new ProcurementMappingProperties();
        properties.setNumPeriodo(202606);
        properties.setCodContbl("CONTBL");
        properties.setCodTipUnid("UN");
        properties.setGrlCodItem("SERVICIO");
        properties.setCodigoRecIva("REC");
        properties.setDefaultCantidad(BigDecimal.ONE);
        return properties;
    }
}
