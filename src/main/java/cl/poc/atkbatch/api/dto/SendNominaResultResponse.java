package cl.poc.atkbatch.api.dto;

public record SendNominaResultResponse(
        String profile,
        Long numeroNomina,
        boolean sent,
        String msgStatus,
        String message) {
}
