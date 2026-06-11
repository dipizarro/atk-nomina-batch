package cl.poc.atkbatch.domain;

public record SimulatedDocumentoContable(
        DocumentoContable documentoOriginal,
        Integer simulationIteration,
        String simulatedDocumentKey,
        Long numeroNomina) {
}
