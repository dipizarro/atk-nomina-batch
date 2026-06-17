package cl.poc.atkbatch.domain.artikos;

import cl.poc.atkbatch.domain.Nomina;

public record ArtikosFetchedNomina(
        ArtikosProfileType profile,
        Nomina nomina,
        Long numeroNomina,
        String tipoNomina,
        Integer cantidadDocumentos,
        String rawXml,
        Boolean dryRun) {
}
