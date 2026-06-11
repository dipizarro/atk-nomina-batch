package cl.poc.atkbatch.service;

import cl.poc.atkbatch.domain.ResultadoDocumento;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class BatchResultStore {

    private final Map<Long, List<ResultadoDocumento>> resultsByJobExecutionId = new ConcurrentHashMap<>();

    public void clearResults(Long jobExecutionId) {
        resultsByJobExecutionId.remove(jobExecutionId);
    }

    public void addResults(Long jobExecutionId, List<? extends ResultadoDocumento> results) {
        resultsByJobExecutionId.compute(jobExecutionId, (key, currentResults) -> {
            List<ResultadoDocumento> updatedResults = currentResults == null ? new ArrayList<>() : new ArrayList<>(currentResults);
            updatedResults.addAll(results);
            return updatedResults;
        });
    }

    public List<ResultadoDocumento> getResults(Long jobExecutionId) {
        return List.copyOf(resultsByJobExecutionId.getOrDefault(jobExecutionId, List.of()));
    }

    public BatchResultSummary getSummary(Long jobExecutionId) {
        List<ResultadoDocumento> results = getResults(jobExecutionId);

        long totalOk = results.stream().filter(ResultadoDocumento::isOk).count();
        long totalNok = results.size() - totalOk;
        long totalConciliaciones = results.stream()
                .map(ResultadoDocumento::simulatedDocumento)
                .mapToLong(item -> item.documentoOriginal().conciliaciones().size())
                .sum();
        long totalDistribuciones = results.stream()
                .map(ResultadoDocumento::simulatedDocumento)
                .flatMap(item -> item.documentoOriginal().conciliaciones().stream())
                .mapToLong(conciliacion -> conciliacion.distribuciones().size())
                .sum();
        Long numeroNomina = results.stream()
                .findFirst()
                .map(ResultadoDocumento::simulatedDocumento)
                .map(item -> item.numeroNomina())
                .orElse(null);

        return new BatchResultSummary(
                jobExecutionId,
                numeroNomina,
                results.size(),
                totalOk,
                totalNok,
                totalConciliaciones,
                totalDistribuciones);
    }

    public record BatchResultSummary(
            Long jobExecutionId,
            Long numeroNomina,
            long totalProcessed,
            long totalOk,
            long totalNok,
            long totalConciliaciones,
            long totalDistribuciones) {
    }
}
