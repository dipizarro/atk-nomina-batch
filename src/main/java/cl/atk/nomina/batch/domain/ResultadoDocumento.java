package cl.atk.nomina.batch.domain;

import java.math.BigDecimal;

public record ResultadoDocumento(
        SimulatedDocumentoContable simulatedDocumento,
        String status,
        String message,
        String docFolio,
        String docRutProveedor,
        String docTipoDoc,
        BigDecimal monto) {

    public ResultadoDocumento(
            SimulatedDocumentoContable simulatedDocumento,
            String status,
            String message) {
        this(simulatedDocumento, status, message, null, null, null, null);
    }

    public boolean isOk() {
        return "OK".equals(status);
    }

    public String resolvedDocFolio() {
        if (hasText(docFolio)) {
            return docFolio;
        }
        return originalDocumento() == null ? "" : originalDocumento().numeroDocumento();
    }

    public String resolvedDocRutProveedor() {
        if (hasText(docRutProveedor)) {
            return docRutProveedor;
        }
        return originalDocumento() == null ? "" : originalDocumento().rutProveedor();
    }

    public String resolvedDocTipoDoc() {
        if (hasText(docTipoDoc)) {
            return docTipoDoc;
        }
        return originalDocumento() == null ? "" : originalDocumento().tipoDocumento();
    }

    public BigDecimal resolvedMonto() {
        if (monto != null) {
            return monto;
        }
        return originalDocumento() == null ? null : originalDocumento().montoTotal();
    }

    private DocumentoContable originalDocumento() {
        return simulatedDocumento == null ? null : simulatedDocumento.documentoOriginal();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
