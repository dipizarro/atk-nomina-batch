package cl.atk.nomina.batch.procurement.lookup;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProcurementTaxTypeResolverTest {

    private final ProcurementTaxTypeResolver resolver = new ProcurementTaxTypeResolver();

    @Test
    void resolvesIvaWhenNetAmountIsPositive() {
        assertThat(resolver.resolve(new BigDecimal("10"))).isEqualTo("IVA");
    }

    @Test
    void resolvesExentoWhenNetAmountIsZeroOrNull() {
        assertThat(resolver.resolve(BigDecimal.ZERO)).isEqualTo("EXE");
        assertThat(resolver.resolve(null)).isEqualTo("EXE");
    }
}
