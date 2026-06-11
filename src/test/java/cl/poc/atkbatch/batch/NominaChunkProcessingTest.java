package cl.poc.atkbatch.batch;

import static org.assertj.core.api.Assertions.assertThat;

import cl.poc.atkbatch.batch.processor.NominaDocumentoItemProcessor;
import cl.poc.atkbatch.batch.reader.NominaDocumentoItemReader;
import cl.poc.atkbatch.domain.DocumentoContable;
import cl.poc.atkbatch.domain.Nomina;
import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.SimulatedDocumentoContable;
import cl.poc.atkbatch.service.BatchResultStore;
import cl.poc.atkbatch.service.NominaXmlParserService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class NominaChunkProcessingTest {

    private final NominaXmlParserService parserService = new NominaXmlParserService(
            new ClassPathResource("samples/ZSVIDA_Nom15960.xml"));
    private final NominaDocumentoItemProcessor processor = new NominaDocumentoItemProcessor();

    @Test
    void readerGeneratesOneHundredSimulatedItemsFromCurrentXml() throws Exception {
        NominaDocumentoItemReader reader = new NominaDocumentoItemReader(parserService, 100);

        List<SimulatedDocumentoContable> items = readAll(reader);

        assertThat(items).hasSize(100);
        assertThat(items.get(0).simulatedDocumentKey()).isEqualTo("15960-3151100-001");
        assertThat(items.get(1).simulatedDocumentKey()).isEqualTo("15960-3151100-002");
        assertThat(items.get(99).simulatedDocumentKey()).isEqualTo("15960-3151100-100");
    }

    @Test
    void processorMarksValidDocumentAsOk() throws Exception {
        ResultadoDocumento result = processor.process(simulatedItem(validDocument()));

        assertThat(result.status()).isEqualTo("OK");
        assertThat(result.isOk()).isTrue();
    }

    @Test
    void processorMarksDocumentAsNokWhenTotalAmountIsZeroOrNull() throws Exception {
        ResultadoDocumento zeroAmountResult = processor.process(simulatedItem(documentWithMontoTotal(BigDecimal.ZERO)));
        ResultadoDocumento nullAmountResult = processor.process(simulatedItem(documentWithMontoTotal(null)));

        assertThat(zeroAmountResult.status()).isEqualTo("NOK");
        assertThat(nullAmountResult.status()).isEqualTo("NOK");
    }

    @Test
    void summaryCalculatesProcessedAndNestedTotals() throws Exception {
        NominaDocumentoItemReader reader = new NominaDocumentoItemReader(parserService, 100);
        BatchResultStore store = new BatchResultStore();
        Long jobExecutionId = 7L;

        List<ResultadoDocumento> results = new ArrayList<>();
        for (SimulatedDocumentoContable item : readAll(reader)) {
            results.add(processor.process(item));
        }
        store.addResults(jobExecutionId, results);

        BatchResultStore.BatchResultSummary summary = store.getSummary(jobExecutionId);

        assertThat(summary.totalProcessed()).isEqualTo(100);
        assertThat(summary.totalOk()).isEqualTo(100);
        assertThat(summary.totalNok()).isZero();
        assertThat(summary.totalConciliaciones()).isEqualTo(200);
        assertThat(summary.totalDistribuciones()).isEqualTo(200);
        assertThat(summary.numeroNomina()).isEqualTo(15960L);
    }

    private List<SimulatedDocumentoContable> readAll(NominaDocumentoItemReader reader) throws Exception {
        List<SimulatedDocumentoContable> items = new ArrayList<>();
        SimulatedDocumentoContable item;
        while ((item = reader.read()) != null) {
            items.add(item);
        }
        return items;
    }

    private SimulatedDocumentoContable simulatedItem(DocumentoContable documento) {
        return new SimulatedDocumentoContable(documento, 1, "15960-3151100-001", 15960L);
    }

    private DocumentoContable validDocument() {
        Nomina nomina = parserService.parseSampleFile();
        return nomina.documentos().get(0);
    }

    private DocumentoContable documentWithMontoTotal(BigDecimal montoTotal) {
        DocumentoContable documento = validDocument();
        return new DocumentoContable(
                documento.secuencia(),
                documento.rutProveedor(),
                documento.proveedor(),
                documento.nacional(),
                documento.idDocumento(),
                documento.usuario(),
                documento.numeroDocumento(),
                documento.tipoDocumento(),
                documento.tipoErp(),
                documento.fechaEmision(),
                documento.fechaVencimiento(),
                documento.fechaRecepcion(),
                documento.fechaRecepSii(),
                documento.urlDocumento(),
                documento.observacion(),
                documento.docCurrency(),
                documento.montoNeto(),
                documento.montoIva(),
                documento.montoExento(),
                documento.otrosImpuestos(),
                montoTotal,
                documento.referencias(),
                documento.conciliaciones());
    }
}
