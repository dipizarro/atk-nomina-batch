package cl.atk.nomina.batch.procurement.lookup;

public record ProcurementItemLookupResult(
        String grlCodItem,
        String codTipUnid,
        String codContbl,
        String codImpsto,
        String codMoneda,
        Long codCuenta) {
}
