package cl.poc.atkbatch.api.dto;

import java.time.OffsetDateTime;

public record HealthResponse(String status, String application, OffsetDateTime timestamp) {
}
