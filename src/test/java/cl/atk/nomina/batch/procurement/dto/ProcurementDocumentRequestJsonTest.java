package cl.atk.nomina.batch.procurement.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProcurementDocumentRequestJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesWithExactProcurementJsonNames() throws Exception {
        ProcurementDocumentRequest request = new ProcurementDocumentRequest(
                "CMP",
                new ProcurementCmpRequest(
                        new ProcurementCmpDocumtRequest(
                                "CMP",
                                "002",
                                202606,
                                96670840L,
                                "2",
                                "CM",
                                "21040010",
                                "CONTBL",
                                "CLP",
                                "2026-06-03",
                                "Documento",
                                "2026-06-03",
                                new BigDecimal("19000"),
                                BigDecimal.ZERO,
                                new BigDecimal("2850"),
                                "2026-06-03",
                                "REC",
                                "2026-06-03"),
                        List.of(new ProcurementCmpDocumtDetRequest(
                                1,
                                "UN",
                                "SERVICIO",
                                "13102",
                                "Linea",
                                BigDecimal.ONE,
                                new BigDecimal("15000"),
                                new BigDecimal("15000"),
                                BigDecimal.ONE,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                new BigDecimal("15000"),
                                new BigDecimal("19"),
                                new BigDecimal("2090"),
                                new BigDecimal("17090"))),
                        new ProcurementCmpDocumtDetRutRequest()),
                null);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(request));

        assertThat(json.get("COD_TIP_DOCUMT").asText()).isEqualTo("CMP");
        assertThat(json.has("CMP")).isTrue();
        assertThat(json.has("HNR")).isTrue();
        assertThat(json.get("HNR").isNull()).isTrue();
        assertThat(json.get("CMP").has("CMP_DOCUMT")).isTrue();
        assertThat(json.get("CMP").has("CMP_DOCUMT_DET")).isTrue();
        assertThat(json.get("CMP").has("CMP_DOCUMT_DET_RUT")).isTrue();
        assertThat(json.get("CMP").get("CMP_DOCUMT").get("COD_EMPRES").asText()).isEqualTo("002");
        assertThat(json.get("CMP").get("CMP_DOCUMT").get("NUM_DOCCMP").asText()).isEqualTo("2");
        assertThat(json.get("CMP").get("CMP_DOCUMT_DET").get(0).get("NUM_LIN_DOCCMP").asInt()).isEqualTo(1);
        assertThat(json.get("CMP").get("CMP_DOCUMT_DET_RUT").isObject()).isTrue();
    }
}
