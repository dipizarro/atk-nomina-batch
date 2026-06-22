package cl.atk.nomina.batch.procurement.service;

import cl.atk.nomina.batch.domain.DocumentoContable;
import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.ResultadoDocumento;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.service.DocumentProcessingService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(name = "procurement.integration.enabled", havingValue = "true")
public class ProcurementDocumentProcessingService implements DocumentProcessingService {

    private final ProcurementIntegrationService integrationService;

    public ProcurementDocumentProcessingService(ProcurementIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @Override
    public List<ResultadoDocumento> processDocuments(ArtikosProfileType profile, Nomina nomina) {
        List<ResultadoDocumento> resultados = new ArrayList<>();
        for (DocumentoContable documento : nomina.documentos()) {
            resultados.add(integrationService.processDocument(profile, nomina, documento));
        }
        return List.copyOf(resultados);
    }
}
