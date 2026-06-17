package cl.poc.atkbatch.api.dto;

public record ArtikosMaskedOperationConfigResponse(
        String operation,
        String endpoint,
        String msgCode,
        String msgFromAddress,
        String msgCodFromAddress,
        String msgToAddress,
        String msgCodSis,
        boolean tokenPresent,
        String tokenMasked) {
}
