package cl.poc.atkbatch.domain;

import java.util.List;

public record SimulatedNomina(
        Long baseNumeroNomina,
        Long simulatedNumeroNomina,
        Integer simulationIndex,
        Nomina nominaOriginal,
        List<DocumentoContable> documentos,
        String simulatedNominaKey) {
}
