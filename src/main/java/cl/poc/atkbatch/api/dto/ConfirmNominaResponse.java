package cl.poc.atkbatch.api.dto;

public record ConfirmNominaResponse(
        String profile,
        Long numeroNomina,
        boolean confirmed,
        String msgStatus,
        String message) {
}
