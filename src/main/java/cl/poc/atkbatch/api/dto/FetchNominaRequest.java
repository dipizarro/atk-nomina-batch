package cl.poc.atkbatch.api.dto;

import jakarta.validation.constraints.NotBlank;

public record FetchNominaRequest(
        @NotBlank String profile) {
}
