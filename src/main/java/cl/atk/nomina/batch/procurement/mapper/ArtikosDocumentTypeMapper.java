package cl.atk.nomina.batch.procurement.mapper;

import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ArtikosDocumentTypeMapper {

    private static final Map<String, String> DOCUMENT_TYPES = Map.of(
            "33", "FEC",
            "34", "FCE",
            "56", "NDC",
            "61", "ECC",
            "FEC", "FEC",
            "FCE", "FCE",
            "NDC", "NDC",
            "ECC", "ECC");

    public String toProcurementDocumentType(String tipoErp) {
        if (tipoErp == null || tipoErp.isBlank()) {
            throw new ProcurementMappingException("Missing Artikos field for Procurement mapping: DocumentoContable.tipoErp");
        }

        String normalized = tipoErp.trim().toUpperCase();
        String mapped = DOCUMENT_TYPES.get(normalized);
        if (mapped == null) {
            throw new ProcurementMappingException("Unsupported Artikos Tipo_ERP for Procurement mapping: " + tipoErp);
        }
        return mapped;
    }
}
