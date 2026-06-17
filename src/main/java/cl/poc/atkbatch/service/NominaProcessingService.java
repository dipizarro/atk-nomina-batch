package cl.poc.atkbatch.service;

import cl.poc.atkbatch.batch.processor.NominaDocumentoItemProcessor;
import cl.poc.atkbatch.domain.DocumentoContable;
import cl.poc.atkbatch.domain.Nomina;
import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.SimulatedDocumentoContable;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationConfig;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NominaProcessingService {

    private final NominaDocumentoItemProcessor documentoItemProcessor;
    private final NominaResultXmlService nominaResultXmlService;

    public NominaProcessingService(
            NominaDocumentoItemProcessor documentoItemProcessor,
            NominaResultXmlService nominaResultXmlService) {
        this.documentoItemProcessor = documentoItemProcessor;
        this.nominaResultXmlService = nominaResultXmlService;
    }

    public ResultadoNomina process(
            Long jobExecutionId,
            Long numeroNomina,
            Nomina nomina,
            ArtikosOperationConfig resultadoOperationConfig) {
        List<ResultadoDocumento> documentos = new ArrayList<>();
        for (DocumentoContable documento : nomina.documentos()) {
            documentos.add(documentoItemProcessor.process(new SimulatedDocumentoContable(
                    documento,
                    1,
                    "%d-%d".formatted(numeroNomina, documento.idDocumento()),
                    numeroNomina)));
        }

        int totalOk = (int) documentos.stream().filter(ResultadoDocumento::isOk).count();
        int totalNok = documentos.size() - totalOk;
        int totalConciliaciones = nomina.documentos().stream()
                .mapToInt(documento -> documento.conciliaciones().size())
                .sum();
        int totalDistribuciones = nomina.documentos().stream()
                .flatMap(documento -> documento.conciliaciones().stream())
                .mapToInt(conciliacion -> conciliacion.distribuciones().size())
                .sum();
        String status = totalNok == 0 ? "OK" : "NOK";

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
                null);

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
