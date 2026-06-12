package cl.poc.atkbatch.batch.processor;

import cl.poc.atkbatch.domain.DocumentoContable;
import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.SimulatedDocumentoContable;
import cl.poc.atkbatch.domain.SimulatedNomina;
import cl.poc.atkbatch.service.NominaResultXmlService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.ItemProcessor;

public class NominaItemProcessor implements ItemProcessor<SimulatedNomina, ResultadoNomina> {

    private final NominaDocumentoItemProcessor documentoItemProcessor;
    private final NominaResultXmlService nominaResultXmlService;

    public NominaItemProcessor(
            NominaDocumentoItemProcessor documentoItemProcessor,
            NominaResultXmlService nominaResultXmlService) {
        this.documentoItemProcessor = documentoItemProcessor;
        this.nominaResultXmlService = nominaResultXmlService;
    }

    @Override
    public ResultadoNomina process(SimulatedNomina item) throws Exception {
        List<ResultadoDocumento> documentos = new ArrayList<>();
        for (DocumentoContable documento : item.documentos()) {
            documentos.add(documentoItemProcessor.process(new SimulatedDocumentoContable(
                    documento,
                    item.simulationIndex(),
                    simulatedDocumentKey(item, documento),
                    item.simulatedNumeroNomina())));
        }

        int totalOk = (int) documentos.stream().filter(ResultadoDocumento::isOk).count();
        int totalNok = documentos.size() - totalOk;
        int totalConciliaciones = item.documentos().stream()
                .mapToInt(documento -> documento.conciliaciones().size())
                .sum();
        int totalDistribuciones = item.documentos().stream()
                .flatMap(documento -> documento.conciliaciones().stream())
                .mapToInt(conciliacion -> conciliacion.distribuciones().size())
                .sum();
        String status = totalNok == 0 ? "OK" : "NOK";

        ResultadoNomina result = new ResultadoNomina(
                null,
                item.simulatedNumeroNomina(),
                documentos.size(),
                totalOk,
                totalNok,
                totalConciliaciones,
                totalDistribuciones,
                List.copyOf(documentos),
                "",
                status);

        return new ResultadoNomina(
                result.jobExecutionId(),
                result.numeroNomina(),
                result.totalDocuments(),
                result.totalOk(),
                result.totalNok(),
                result.totalConciliaciones(),
                result.totalDistribuciones(),
                result.documentos(),
                nominaResultXmlService.buildNomfactresXml(result),
                result.status());
    }

    private String simulatedDocumentKey(SimulatedNomina nomina, DocumentoContable documento) {
        return "%d-%d-%03d".formatted(
                nomina.simulatedNumeroNomina(),
                documento.idDocumento(),
                nomina.simulationIndex());
    }
}
