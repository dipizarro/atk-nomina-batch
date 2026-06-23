package cl.atk.nomina.batch.procurement.lookup;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ProcurementTaxTypeResolver {

    public String resolve(BigDecimal montoNeto) {
        if (isPositive(montoNeto)) {
            return "IVA";
        }
        return "EXE";
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
