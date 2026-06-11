package cl.poc.atkbatch.api.dto;

public record BatchSummaryResponse(
        Long jobExecutionId,
        String status,
        Long numeroNomina,
        long totalProcessed,
        long totalOk,
        long totalNok,
        long totalConciliaciones,
        long totalDistribuciones) {
}
