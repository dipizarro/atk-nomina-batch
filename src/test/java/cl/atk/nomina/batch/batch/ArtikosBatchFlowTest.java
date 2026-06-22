package cl.atk.nomina.batch.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.atk.nomina.batch.batch.processor.ArtikosNominaItemProcessor;
import cl.atk.nomina.batch.batch.processor.NominaDocumentoItemProcessor;
import cl.atk.nomina.batch.batch.reader.ArtikosNominaItemReader;
import cl.atk.nomina.batch.batch.writer.ArtikosNominaResultItemWriter;
import cl.atk.nomina.batch.domain.Conciliacion;
import cl.atk.nomina.batch.domain.DistribucionContable;
import cl.atk.nomina.batch.domain.DocumentoContable;
import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.NominaHeader;
import cl.atk.nomina.batch.domain.ResultadoNomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosFetchedNomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosGenericResponse;
import cl.atk.nomina.batch.domain.artikos.ArtikosOperationConfig;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.domain.error.IntegrationErrorType;
import cl.atk.nomina.batch.service.BatchResultStore;
import cl.atk.nomina.batch.service.ControlNominaService;
import cl.atk.nomina.batch.service.NominaErrorPolicyService;
import cl.atk.nomina.batch.service.NominaProcessingService;
import cl.atk.nomina.batch.service.NominaResultXmlService;
import cl.atk.nomina.batch.service.NominaXmlParserService;
import cl.atk.nomina.batch.service.SimulatedDocumentProcessingService;
import cl.atk.nomina.batch.service.artikos.ArtikosGenericSoapResponseParser;
import cl.atk.nomina.batch.service.artikos.ArtikosSoapClient;
import cl.atk.nomina.batch.service.artikos.ArtikosSoapResponseParser;
import cl.atk.nomina.batch.shared.exception.ArtikosIntegrationException;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

class ArtikosBatchFlowTest {

    private final NominaXmlParserService nominaXmlParserService = new NominaXmlParserService(
            new ClassPathResource("samples/ZSVIDA_Nom15960.xml"));

    @Test
    void readerReturnsNominaWhenArtikosRespondsWithXml() throws Exception {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        when(soapClient.fetchNominaRawXml(ArtikosProfileType.VIDA)).thenReturn(sampleNominaXml());
        ArtikosNominaItemReader reader = new ArtikosNominaItemReader(
                soapClient,
                new ArtikosSoapResponseParser(nominaXmlParserService),
                "VIDA",
                1L,
                "true");

        ArtikosFetchedNomina item = reader.read();

        assertThat(item).isNotNull();
        assertThat(item.profile()).isEqualTo(ArtikosProfileType.VIDA);
        assertThat(item.numeroNomina()).isEqualTo(15960L);
        assertThat(item.cantidadDocumentos()).isEqualTo(1);
    }

    @Test
    void readerReturnsNullWhenArtikosHasNoNominas() {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        when(soapClient.fetchNominaRawXml(ArtikosProfileType.GENERALES)).thenReturn(noNominasXml());
        ArtikosNominaItemReader reader = new ArtikosNominaItemReader(
                soapClient,
                new ArtikosSoapResponseParser(nominaXmlParserService),
                "GENERALES",
                1L,
                "true");

        assertThat(reader.read()).isNull();
    }

    @Test
    void readerReadsSeveralNominasUntilArtikosHasNoNominas() throws Exception {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        when(soapClient.fetchNominaRawXml(ArtikosProfileType.VIDA))
                .thenReturn(sampleNominaXml(), sampleNominaXml(), noNominasXml());
        ArtikosNominaItemReader reader = new ArtikosNominaItemReader(
                soapClient,
                new ArtikosSoapResponseParser(nominaXmlParserService),
                "VIDA",
                10L,
                "true");

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
        verify(soapClient, times(3)).fetchNominaRawXml(ArtikosProfileType.VIDA);
    }

    @Test
    void readerRespectsMaxNominas() throws Exception {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        when(soapClient.fetchNominaRawXml(ArtikosProfileType.VIDA)).thenReturn(sampleNominaXml());
        ArtikosNominaItemReader reader = new ArtikosNominaItemReader(
                soapClient,
                new ArtikosSoapResponseParser(nominaXmlParserService),
                "VIDA",
                1L,
                "true");

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
        verify(soapClient).fetchNominaRawXml(ArtikosProfileType.VIDA);
    }

    @Test
    void readerStopsAtMaxNominasEvenWhenArtikosStillHasNominas() throws Exception {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        when(soapClient.fetchNominaRawXml(ArtikosProfileType.VIDA)).thenReturn(sampleNominaXml());
        ArtikosNominaItemReader reader = new ArtikosNominaItemReader(
                soapClient,
                new ArtikosSoapResponseParser(nominaXmlParserService),
                "VIDA",
                2L,
                "true");

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
        verify(soapClient, times(2)).fetchNominaRawXml(ArtikosProfileType.VIDA);
    }

    @Test
    void processorMarksProcessingAndConfirmsWhenDryRunIsFalse() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        when(soapClient.confirmNominaRawXml(ArtikosProfileType.VIDA, 15960L, 0)).thenReturn("<ok/>");
        when(soapClient.resultadoNominaConfig(ArtikosProfileType.VIDA)).thenReturn(resultadoOperationConfig());
        when(genericParser.parseGenericResponse("<ok/>"))
                .thenReturn(new ArtikosGenericResponse("NOMFACTCONFIR", "0", "", true));
        ArtikosNominaItemProcessor processor = processor(controlNominaService, soapClient, genericParser, "false");

        ResultadoNomina result = processor.process(fetchedNomina(false));

        assertThat(result.numeroNomina()).isEqualTo(15960L);
        assertThat(result.totalOk()).isEqualTo(1);
        verify(controlNominaService).markProcessing(7L, 15960L);
        verify(soapClient).confirmNominaRawXml(ArtikosProfileType.VIDA, 15960L, 0);
    }

    @Test
    void processorDoesNotConfirmWhenDryRunIsTrue() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        when(soapClient.resultadoNominaConfig(ArtikosProfileType.VIDA)).thenReturn(resultadoOperationConfig());
        ArtikosNominaItemProcessor processor = processor(controlNominaService, soapClient, genericParser, "true");

        ResultadoNomina result = processor.process(fetchedNomina(true));

        assertThat(result.numeroNomina()).isEqualTo(15960L);
        verify(controlNominaService, never()).markProcessing(any(), any());
        verify(soapClient, never()).confirmNominaRawXml(any(), any(), any());
    }

    @Test
    void processorUsesSimulatedProcessingWhenDryRunIsTrue() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        NominaProcessingService nominaProcessingService = mock(NominaProcessingService.class);
        ResultadoNomina expectedResult = resultadoNomina();
        when(soapClient.resultadoNominaConfig(ArtikosProfileType.VIDA)).thenReturn(resultadoOperationConfig());
        when(nominaProcessingService.processSimulated(
                eq(7L),
                eq(15960L),
                eq(ArtikosProfileType.VIDA),
                any(Nomina.class),
                any(ArtikosOperationConfig.class)))
                .thenReturn(expectedResult);
        ArtikosNominaItemProcessor processor = processor(
                controlNominaService, soapClient, genericParser, nominaProcessingService, "true");

        ResultadoNomina result = processor.process(fetchedNomina(true));

        assertThat(result).isSameAs(expectedResult);
        verify(nominaProcessingService).processSimulated(
                eq(7L),
                eq(15960L),
                eq(ArtikosProfileType.VIDA),
                any(Nomina.class),
                any(ArtikosOperationConfig.class));
        verify(nominaProcessingService, never()).process(any(), any(), any(), any(), any());
    }

    @Test
    void processorMarksErrorWhenConfirmationFails() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        NominaProcessingService nominaProcessingService = mock(NominaProcessingService.class);
        when(soapClient.confirmNominaRawXml(ArtikosProfileType.VIDA, 15960L, 0)).thenReturn("<nok/>");
        when(genericParser.parseGenericResponse("<nok/>"))
                .thenReturn(new ArtikosGenericResponse("NOMFACTCONFIR", "1", "rechazada", false));
        ArtikosNominaItemProcessor processor = processor(
                controlNominaService, soapClient, genericParser, nominaProcessingService, "false");

        assertThatThrownBy(() -> processor.process(fetchedNomina(false)))
                .isInstanceOf(ArtikosIntegrationException.class)
                .hasMessageContaining("NOMINA_CONFIRM_ERROR")
                .satisfies(exception -> assertThat(((ArtikosIntegrationException) exception).getErrorType())
                        .isEqualTo(IntegrationErrorType.NOMINA_CONFIRM_ERROR));
        verify(controlNominaService).markError(eq(7L), eq(15960L), any());
        verify(nominaProcessingService, never()).process(any(), any(), any(), any(), any());
        verify(nominaProcessingService, never()).processSimulated(any(), any(), any(), any(), any());
    }

    @Test
    void processorMarksErrorWhenNominaIsAlreadyOutsideIntegrationState() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        when(soapClient.confirmNominaRawXml(ArtikosProfileType.VIDA, 15960L, 0)).thenReturn("<already-confirmed/>");
        when(genericParser.parseGenericResponse("<already-confirmed/>"))
                .thenReturn(new ArtikosGenericResponse(
                        "NOMFACTCONFIR",
                        "1",
                        "Solo se puede confirmar la recepción de una nómina con estado En Integración",
                        false));
        ArtikosNominaItemProcessor processor = processor(controlNominaService, soapClient, genericParser, "false");

        assertThatThrownBy(() -> processor.process(fetchedNomina(false)))
                .isInstanceOf(ArtikosIntegrationException.class)
                .satisfies(exception -> assertThat(((ArtikosIntegrationException) exception).getErrorType())
                        .isEqualTo(IntegrationErrorType.NOMINA_CONFIRM_ERROR));

        verify(controlNominaService).markProcessing(7L, 15960L);
        verify(controlNominaService).markError(eq(7L), eq(15960L), any());
    }

    @Test
    void processorReturnsNokWithoutThrowingForFunctionalDocumentError() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        when(soapClient.confirmNominaRawXml(ArtikosProfileType.VIDA, 15960L, 0)).thenReturn("<ok/>");
        when(soapClient.resultadoNominaConfig(ArtikosProfileType.VIDA)).thenReturn(resultadoOperationConfig());
        when(genericParser.parseGenericResponse("<ok/>"))
                .thenReturn(new ArtikosGenericResponse("NOMFACTCONFIR", "0", "", true));
        ArtikosNominaItemProcessor processor = processor(controlNominaService, soapClient, genericParser, "false");

        ResultadoNomina result = processor.process(fetchedNominaWithZeroTotal(false));

        assertThat(result.status()).isEqualTo("NOK");
        assertThat(result.totalNok()).isEqualTo(1);
        verify(controlNominaService, never()).markError(any(), any(), any());
    }

    @Test
    void processorMarksErrorWhenProcurementTechnicalErrorFailsNominaProcessing() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        NominaProcessingService nominaProcessingService = mock(NominaProcessingService.class);
        ArtikosIntegrationException procurementException = new ArtikosIntegrationException(
                IntegrationErrorType.PROCUREMENT_TECHNICAL_ERROR,
                ArtikosProfileType.VIDA.name(),
                15960L,
                "PROCUREMENT_POST_DOCUMENT",
                "timeout",
                null);
        when(soapClient.confirmNominaRawXml(ArtikosProfileType.VIDA, 15960L, 0)).thenReturn("<ok/>");
        when(soapClient.resultadoNominaConfig(ArtikosProfileType.VIDA)).thenReturn(resultadoOperationConfig());
        when(genericParser.parseGenericResponse("<ok/>"))
                .thenReturn(new ArtikosGenericResponse("NOMFACTCONFIR", "0", "", true));
        when(nominaProcessingService.process(
                eq(7L),
                eq(15960L),
                eq(ArtikosProfileType.VIDA),
                any(Nomina.class),
                any(ArtikosOperationConfig.class)))
                .thenThrow(procurementException);
        ArtikosNominaItemProcessor processor = processor(
                controlNominaService, soapClient, genericParser, nominaProcessingService, "false");

        assertThatThrownBy(() -> processor.process(fetchedNomina(false)))
                .isInstanceOf(ArtikosIntegrationException.class)
                .satisfies(exception -> assertThat(((ArtikosIntegrationException) exception).getErrorType())
                        .isEqualTo(IntegrationErrorType.PROCUREMENT_TECHNICAL_ERROR));

        verify(controlNominaService).markProcessing(7L, 15960L);
        verify(controlNominaService).markError(eq(7L), eq(15960L), any());
        verify(soapClient, never()).sendNominaResultRawXml(any(), any());
    }

    @Test
    void processorCalculatesTotalsFromVariableNominaContent() {
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        when(soapClient.resultadoNominaConfig(ArtikosProfileType.VIDA)).thenReturn(resultadoOperationConfig());
        ArtikosNominaItemProcessor processor = processor(controlNominaService, soapClient, genericParser, "true");

        ResultadoNomina result = processor.process(fetchedVariableNomina(true));

        assertThat(result.totalDocuments()).isEqualTo(2);
        assertThat(result.totalOk()).isEqualTo(1);
        assertThat(result.totalNok()).isEqualTo(1);
        assertThat(result.totalConciliaciones()).isEqualTo(3);
        assertThat(result.totalDistribuciones()).isEqualTo(4);
        assertThat(result.status()).isEqualTo("NOK");
        assertThat(result.documentos()).hasSize(2);
    }


    @Test
    void writerSendsNomfactresWhenDryRunIsFalse() {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        BatchResultStore store = mock(BatchResultStore.class);
        ResultadoNomina result = resultadoNomina();
        when(soapClient.sendNominaResultRawXml(ArtikosProfileType.VIDA, result)).thenReturn("<ok/>");
        when(genericParser.parseGenericResponse("<ok/>"))
                .thenReturn(new ArtikosGenericResponse("NOMFACTRES", "0", "", true));
        ArtikosNominaResultItemWriter writer = writer(
                soapClient, genericParser, controlNominaService, store, "false");

        writer.write(Chunk.of(result));

        verify(soapClient).sendNominaResultRawXml(ArtikosProfileType.VIDA, result);
        verify(controlNominaService).markCompleted(result);
        verify(store).addNominaResults(eq(7L), any());
    }

    @Test
    void writerDoesNotSendNomfactresWhenDryRunIsTrue() {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        BatchResultStore store = mock(BatchResultStore.class);
        ArtikosNominaResultItemWriter writer = writer(
                soapClient, genericParser, controlNominaService, store, "true");

        writer.write(Chunk.of(resultadoNomina()));

        verify(soapClient, never()).sendNominaResultRawXml(any(), any());
        verify(controlNominaService, never()).markCompleted(any());
        verify(store).addNominaResults(eq(7L), any());
    }

    @Test
    void writerMarksErrorWhenNomfactresFails() {
        ArtikosSoapClient soapClient = mock(ArtikosSoapClient.class);
        ArtikosGenericSoapResponseParser genericParser = mock(ArtikosGenericSoapResponseParser.class);
        ControlNominaService controlNominaService = mock(ControlNominaService.class);
        BatchResultStore store = mock(BatchResultStore.class);
        ResultadoNomina result = resultadoNomina();
        when(soapClient.sendNominaResultRawXml(ArtikosProfileType.VIDA, result)).thenReturn("<nok/>");
        when(genericParser.parseGenericResponse("<nok/>"))
                .thenReturn(new ArtikosGenericResponse("NOMFACTRES", "1", "estado invalido", false));
        ArtikosNominaResultItemWriter writer = writer(
                soapClient, genericParser, controlNominaService, store, "false");

        assertThatThrownBy(() -> writer.write(Chunk.of(result)))
                .isInstanceOf(ArtikosIntegrationException.class)
                .hasMessageContaining("NOMINA_RESULT_ERROR")
                .satisfies(exception -> assertThat(((ArtikosIntegrationException) exception).getErrorType())
                        .isEqualTo(IntegrationErrorType.NOMINA_RESULT_ERROR));
        verify(controlNominaService).markError(eq(7L), eq(15960L), any());
    }

    private ArtikosNominaItemProcessor processor(
            ControlNominaService controlNominaService,
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericParser,
            String dryRun) {
        return processor(controlNominaService, soapClient, genericParser, simulatedNominaProcessingService(), dryRun);
    }

    private ArtikosNominaItemProcessor processor(
            ControlNominaService controlNominaService,
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericParser,
            NominaProcessingService nominaProcessingService,
            String dryRun) {
        return new ArtikosNominaItemProcessor(
                controlNominaService,
                soapClient,
                genericParser,
                nominaProcessingService,
                new NominaErrorPolicyService(),
                7L,
                dryRun);
    }

    private ArtikosNominaResultItemWriter writer(
            ArtikosSoapClient soapClient,
            ArtikosGenericSoapResponseParser genericParser,
            ControlNominaService controlNominaService,
            BatchResultStore store,
            String dryRun) {
        return new ArtikosNominaResultItemWriter(
                soapClient,
                genericParser,
                controlNominaService,
                new NominaErrorPolicyService(),
                store,
                "VIDA",
                dryRun,
                7L);
    }

    private ArtikosFetchedNomina fetchedNomina(boolean dryRun) {
        Nomina nomina = nominaXmlParserService.parseSampleFile();
        return new ArtikosFetchedNomina(
                ArtikosProfileType.VIDA,
                nomina,
                nomina.cabecera().numeroNomina(),
                nomina.cabecera().tipoNomina(),
                nomina.cabecera().cantidadDocumentos(),
                "<raw/>",
                dryRun);
    }

    private ArtikosFetchedNomina fetchedNominaWithZeroTotal(boolean dryRun) {
        Nomina nomina = nominaXmlParserService.parseSampleFile();
        var documento = nomina.documentos().get(0);
        var invalidDocument = new cl.atk.nomina.batch.domain.DocumentoContable(
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
                BigDecimal.ZERO,
                documento.referencias(),
                documento.conciliaciones());
        Nomina invalidNomina = new Nomina(
                nomina.msgCode(),
                nomina.msgStatus(),
                nomina.msgFromAddress(),
                nomina.cabecera(),
                List.of(invalidDocument));
        return new ArtikosFetchedNomina(
                ArtikosProfileType.VIDA,
                invalidNomina,
                invalidNomina.cabecera().numeroNomina(),
                invalidNomina.cabecera().tipoNomina(),
                invalidNomina.cabecera().cantidadDocumentos(),
                "<raw/>",
                dryRun);
    }

    private ArtikosFetchedNomina fetchedVariableNomina(boolean dryRun) {
        Nomina nomina = nominaXmlParserService.parseSampleFile();
        DocumentoContable firstDocument = nomina.documentos().get(0);
        DocumentoContable secondDocument = documentWith(
                firstDocument,
                2,
                3151101L,
                BigDecimal.ZERO,
                List.of(conciliacionWithDistributions(2)));
        NominaHeader header = new NominaHeader(
                nomina.cabecera().msgFrom(),
                nomina.cabecera().msgTo(),
                nomina.cabecera().msgDate(),
                nomina.cabecera().msgSystem(),
                nomina.cabecera().msgCode(),
                nomina.cabecera().msgVersion(),
                nomina.cabecera().numeroNomina(),
                nomina.cabecera().tipoNomina(),
                nomina.cabecera().fechaNomina(),
                2);
        Nomina variableNomina = new Nomina(
                nomina.msgCode(),
                nomina.msgStatus(),
                nomina.msgFromAddress(),
                header,
                List.of(firstDocument, secondDocument));
        return new ArtikosFetchedNomina(
                ArtikosProfileType.VIDA,
                variableNomina,
                variableNomina.cabecera().numeroNomina(),
                variableNomina.cabecera().tipoNomina(),
                variableNomina.cabecera().cantidadDocumentos(),
                "<raw/>",
                dryRun);
    }

    private DocumentoContable documentWith(
            DocumentoContable source,
            Integer secuencia,
            Long idDocumento,
            BigDecimal montoTotal,
            List<Conciliacion> conciliaciones) {
        return new DocumentoContable(
                secuencia,
                source.rutProveedor(),
                source.proveedor(),
                source.nacional(),
                idDocumento,
                source.usuario(),
                "%d".formatted(secuencia),
                source.tipoDocumento(),
                source.tipoErp(),
                source.fechaEmision(),
                source.fechaVencimiento(),
                source.fechaRecepcion(),
                source.fechaRecepSii(),
                source.urlDocumento(),
                source.observacion(),
                source.docCurrency(),
                source.montoNeto(),
                source.montoIva(),
                source.montoExento(),
                source.otrosImpuestos(),
                montoTotal,
                source.referencias(),
                conciliaciones);
    }

    private Conciliacion conciliacionWithDistributions(int distributionCount) {
        List<DistribucionContable> distributions = java.util.stream.IntStream.rangeClosed(1, distributionCount)
                .mapToObj(index -> new DistribucionContable(
                        index,
                        "item " + index,
                        "CC",
                        "Centro costo",
                        "CTA",
                        "Cuenta",
                        BigDecimal.TEN,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.TEN))
                .toList();
        return new Conciliacion(
                "NETO",
                "PRODUCTO",
                "CONC",
                "CLP",
                BigDecimal.TEN,
                "RECEP",
                BigDecimal.ONE,
                "comentario",
                1,
                distributions);
    }

    private ResultadoNomina resultadoNomina() {
        return simulatedNominaProcessingService()
                .process(
                        7L,
                        15960L,
                        ArtikosProfileType.VIDA,
                        nominaXmlParserService.parseSampleFile(),
                        resultadoOperationConfig());
    }

    private NominaProcessingService simulatedNominaProcessingService() {
        return new NominaProcessingService(
                new SimulatedDocumentProcessingService(new NominaDocumentoItemProcessor()),
                new SimulatedDocumentProcessingService(new NominaDocumentoItemProcessor()),
                new NominaResultXmlService());
    }

    private ArtikosOperationConfig resultadoOperationConfig() {
        ArtikosOperationConfig operationConfig = new ArtikosOperationConfig();
        operationConfig.setMsgCode("NOMFACTRES");
        operationConfig.setMsgFromAddress("ZSVIDA");
        operationConfig.setMsgToAddress("ARTIKOS");
        operationConfig.setMsgCodSis("SAF");
        return operationConfig;
    }

    private String sampleNominaXml() throws Exception {
        return StreamUtils.copyToString(
                new ClassPathResource("samples/ZSVIDA_Nom15960.xml").getInputStream(),
                StandardCharsets.UTF_8);
    }

    private String noNominasXml() {
        return """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <EjecutaTrxResponse>
                      <EjecutaTrxResult>
                        <Message>
                          <MessageId>
                            <MsgStatus>1</MsgStatus>
                          </MessageId>
                          <MessageOut>
                            <LogMessage>
                              <MessageText>No hay nominas para procesar</MessageText>
                            </LogMessage>
                          </MessageOut>
                        </Message>
                      </EjecutaTrxResult>
                    </EjecutaTrxResponse>
                  </soap:Body>
                </soap:Envelope>
                """;
    }
}
