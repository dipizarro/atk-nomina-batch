package cl.atk.nomina.batch.procurement.service;

import java.text.Normalizer;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProcurementDuplicateDetector {

    private static final List<String> DUPLICATE_PATTERNS = List.of(
            "el registro que intenta crear ya existe en la base de datos",
            "registro ya existe",
            "ya existe",
            "duplicate",
            "duplicado",
            "unique constraint",
            "ora-00001");

    public boolean isDuplicate(String message, Object error) {
        String combined = normalized("%s %s".formatted(value(message), value(error)));
        if (combined.isBlank()) {
            return false;
        }
        return DUPLICATE_PATTERNS.stream().anyMatch(combined::contains);
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private String normalized(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
