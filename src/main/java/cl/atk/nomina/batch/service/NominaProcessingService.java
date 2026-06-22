package cl.atk.nomina.batch.service;

import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.ResultadoDocumento;
import cl.atk.nomina.batch.domain.ResultadoNomina;
import cl.atk.nomina.batch.domain.SimulatedDocumentoContable;
import cl.atk.nomina.batch.domain.artikos.ArtikosOperationConfig;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NominaProcessingService {

    private final DocumentProcessingService documentProcessingService;
    private final SimulatedDocumentProcessingService simulatedDocumentProcessingService;
    private final NominaResultXmlService nominaResultXmlService;

    public NominaProcessingService(
            DocumentProcessingService documentProcessingService,
            SimulatedDocumentProcessingService simulatedDocumentProcessingService,
            NominaResultXmlService nominaResultXmlService) {
        this.documentProcessingService = documentProcessingService;
        this.simulatedDocumentProcessingService = simulatedDocumentProcessingService;
        this.nominaResultXmlService = nominaResultXmlService;
    }

    public ResultadoNomina process(
            Long jobExecutionId,
            Long numeroNomina,
            ArtikosProfileType profile,
            Nomina nomina,
            ArtikosOperationConfig resultadoOperationConfig) {
        return processWithDocumentService(
                jobExecutionId,
                numeroNomina,
                profile,
                nomina,
                resultadoOperationConfig,
                documentProcessingService);
    }

    public ResultadoNomina processSimulated(
            Long jobExecutionId,
            Long numeroNomina,
            ArtikosProfileType profile,
            Nomina nomina,
            ArtikosOperationConfig resultadoOperationConfig) {
        return processWithDocumentService(
                jobExecutionId,
                numeroNomina,
                profile,
                nomina,
                resultadoOperationConfig,
                simulatedDocumentProcessingService);
    }

    public ResultadoNomina processAlreadyOk(
            Long jobExecutionId,
            Long numeroNomina,
            ArtikosProfileType profile,
            Nomina nomina,
            ArtikosOperationConfig resultadoOperationConfig) {
        List<ResultadoDocumento> documentos = new ArrayList<>();
        nomina.documentos().forEach(documento -> documentos.add(new ResultadoDocumento(
                new SimulatedDocumentoContable(
                        documento,
                        1,
                        "%d-%d".formatted(numeroNomina, documento.idDocumento()),
                        numeroNomina),
                "OK",
                "Documento omitido: nomina ya procesada OK",
                documento.numeroDocumento(),
                documento.rutProveedor(),
                documento.tipoDocumento(),
                documento.montoTotal())));

        return buildResultadoNomina(
                jobExecutionId,
                numeroNomina,
                nomina,
                resultadoOperationConfig,
                List.copyOf(documentos),
                null);
    }

    private ResultadoNomina processWithDocumentService(
            Long jobExecutionId,
            Long numeroNomina,
            ArtikosProfileType profile,
            Nomina nomina,
            ArtikosOperationConfig resultadoOperationConfig,
            DocumentProcessingService processingService) {
        List<ResultadoDocumento> documentos = processingService.processDocuments(profile, nomina);

        return buildResultadoNomina(
                jobExecutionId,
                numeroNomina,
                nomina,
                resultadoOperationConfig,
                documentos,
                documentos.isEmpty() ? "Nomina sin documentos para informar" : null);
    }

    private ResultadoNomina buildResultadoNomina(
            Long jobExecutionId,
            Long numeroNomina,
            Nomina nomina,
            ArtikosOperationConfig resultadoOperationConfig,
            List<ResultadoDocumento> documentos,
            String errorMessage) {
        int totalOk = (int) documentos.stream().filter(ResultadoDocumento::isOk).count();
        int totalNok = documentos.size() - totalOk;
        int totalConciliaciones = nomina.documentos().stream()
                .mapToInt(documento -> documento.conciliaciones().size())
                .sum();
        int totalDistribuciones = nomina.documentos().stream()
                .flatMap(documento -> documento.conciliaciones().stream())
                .mapToInt(conciliacion -> conciliacion.distribuciones().size())
                .sum();
        String status = totalNok == 0 && !documentos.isEmpty() ? "OK" : "NOK";

        ResultadoNomina result = new ResultadoNomina(
                jobExecutionId,
                numeroNomina,
                documentos.size(),
                totalOk,
                totalNok,
                totalConciliaciones,
                totalDistribuciones,
                List.copyOf(documentos),
                "",
                status,
                errorMessage);

        return new ResultadoNomina(
                result.jobExecutionId(),
                result.numeroNomina(),
                result.totalDocuments(),
                result.totalOk(),
                result.totalNok(),
                result.totalConciliaciones(),
                result.totalDistribuciones(),
                result.documentos(),
                nominaResultXmlService.buildNomfactresXml(result, resultadoOperationConfig),
                result.status(),
                result.errorMessage());
    }
}
