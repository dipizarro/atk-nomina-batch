package cl.poc.atkbatch.domain;

public record ResultadoDocumento(
        SimulatedDocumentoContable simulatedDocumento,
        String status,
        String message) {

    public boolean isOk() {
        return "OK".equals(status);
    }
}
