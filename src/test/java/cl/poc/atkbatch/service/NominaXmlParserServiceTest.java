package cl.poc.atkbatch.service;

import static org.assertj.core.api.Assertions.assertThat;

import cl.poc.atkbatch.domain.Conciliacion;
import cl.poc.atkbatch.domain.DocumentoContable;
import cl.poc.atkbatch.domain.Nomina;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class NominaXmlParserServiceTest {

    private final NominaXmlParserService parserService = new NominaXmlParserService(
            new ClassPathResource("samples/ZSVIDA_Nom15960.xml"));

    @Test
    void parsesArtikosSoapNominaSample() {
        Nomina nomina = parserService.parseSampleFile();
        DocumentoContable documento = nomina.documentos().get(0);

        assertThat(nomina.msgCode()).isEqualTo("NOMFACTERP");
        assertThat(nomina.msgStatus()).isEqualTo("0");
        assertThat(nomina.msgFromAddress()).isEqualTo("ZSVIDA");
        assertThat(nomina.cabecera().numeroNomina()).isEqualTo(15960L);
        assertThat(nomina.cabecera().tipoNomina()).isEqualTo("ZSVAYP");
        assertThat(nomina.cabecera().cantidadDocumentos()).isEqualTo(1);
        assertThat(nomina.documentos()).hasSize(1);

        assertThat(documento.secuencia()).isEqualTo(1);
        assertThat(documento.rutProveedor()).isEqualTo("96670840-9");
        assertThat(documento.proveedor()).isEqualTo("DIMERC S.A.");
        assertThat(documento.idDocumento()).isEqualTo(3151100L);
        assertThat(documento.usuario()).isEqualTo("eolivares");
        assertThat(documento.numeroDocumento()).isEqualTo("2");
        assertThat(documento.tipoDocumento()).isEqualTo("33");
        assertThat(documento.tipoErp()).isEqualTo("33");
        assertThat(documento.montoNeto()).isEqualByComparingTo(new BigDecimal("19000"));
        assertThat(documento.montoIva()).isEqualByComparingTo(new BigDecimal("2850"));
        assertThat(documento.montoExento()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(documento.montoTotal()).isEqualByComparingTo(new BigDecimal("21850"));
        assertThat(documento.conciliaciones()).hasSize(2);
        assertThat(totalDistribuciones(documento)).isEqualTo(2);
    }

    private int totalDistribuciones(DocumentoContable documento) {
        return documento.conciliaciones().stream()
                .map(Conciliacion::distribuciones)
                .mapToInt(java.util.List::size)
                .sum();
    }
}
