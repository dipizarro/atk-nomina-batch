package cl.atk.nomina.batch.procurement.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import org.junit.jupiter.api.Test;

class ArtikosDocumentTypeMapperTest {

    private final ArtikosDocumentTypeMapper mapper = new ArtikosDocumentTypeMapper();

    @Test
    void mapsNumericAndCanonicalDocumentTypes() {
        assertThat(mapper.toProcurementDocumentType("33")).isEqualTo("FEC");
        assertThat(mapper.toProcurementDocumentType("34")).isEqualTo("FCE");
        assertThat(mapper.toProcurementDocumentType("56")).isEqualTo("NDC");
        assertThat(mapper.toProcurementDocumentType("61")).isEqualTo("ECC");
        assertThat(mapper.toProcurementDocumentType("FEC")).isEqualTo("FEC");
        assertThat(mapper.toProcurementDocumentType("FCE")).isEqualTo("FCE");
        assertThat(mapper.toProcurementDocumentType("NDC")).isEqualTo("NDC");
        assertThat(mapper.toProcurementDocumentType("ECC")).isEqualTo("ECC");
    }

    @Test
    void rejectsUnsupportedDocumentType() {
        assertThatThrownBy(() -> mapper.toProcurementDocumentType("XX"))
                .isInstanceOf(ProcurementMappingException.class)
                .hasMessageContaining("Unsupported Artikos Tipo_ERP");
    }
}
