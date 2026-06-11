package cl.poc.atkbatch.api.dto;

public record StartBatchResponse(
        Long jobExecutionId,
        String jobName,
        String status,
        String message) {
}
