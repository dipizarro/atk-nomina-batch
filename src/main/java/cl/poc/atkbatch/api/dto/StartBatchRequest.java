package cl.poc.atkbatch.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StartBatchRequest(
        @NotBlank String profile,
        @Min(1) Integer maxNominas,
        Boolean dryRun) {

    public int resolvedMaxNominas() {
        return maxNominas == null ? 1 : maxNominas;
    }

    public boolean resolvedDryRun() {
        return dryRun != null && dryRun;
    }
}
