package cl.poc.atkbatch.domain;

public record ReferenciaDocumento(
        Integer secuencia,
        String tipoDocumento,
        String folio,
        String comentario) {
}
