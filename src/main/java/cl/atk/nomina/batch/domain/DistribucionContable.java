package cl.atk.nomina.batch.domain;

import java.math.BigDecimal;

public record DistribucionContable(
        Integer secuencia,
        String itemDescription,
        String codCentroCosto,
        String centroCosto,
        String codCuentaContable,
        String cuentaContable,
        BigDecimal montoNeto,
        BigDecimal montoExento,
        BigDecimal montoIva,
        BigDecimal montoTotal) {
}
