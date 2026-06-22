package cl.atk.nomina.batch.procurement.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProcurementDuplicateDetectorTest {

    private final ProcurementDuplicateDetector detector = new ProcurementDuplicateDetector();

    @Test
    void detectsConfirmedSpanishDuplicateMessage() {
        assertThat(detector.isDuplicate(
                "El registro que intenta crear ya existe en la base de datos",
                null))
                .isTrue();
    }

    @Test
    void detectsShortSpanishDuplicateMessage() {
        assertThat(detector.isDuplicate("registro ya existe", null)).isTrue();
    }

    @Test
    void detectsOracleUniqueConstraintMessage() {
        assertThat(detector.isDuplicate(null, "ORA-00001: unique constraint violated")).isTrue();
    }

    @Test
    void doesNotDetectFunctionalNonDuplicateMessage() {
        assertThat(detector.isDuplicate("Proveedor invalido", null)).isFalse();
    }

    @Test
    void doesNotDetectNullMessage() {
        assertThat(detector.isDuplicate(null, null)).isFalse();
    }
}
