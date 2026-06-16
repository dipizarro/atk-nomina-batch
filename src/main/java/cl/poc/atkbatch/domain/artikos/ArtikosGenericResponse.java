package cl.poc.atkbatch.domain.artikos;

public record ArtikosGenericResponse(
        String msgCode,
        String msgStatus,
        String messageText,
        boolean success) {
}
