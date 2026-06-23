package cl.atk.nomina.batch.procurement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.atk.nomina.batch.domain.DocumentoContable;
import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.ResultadoDocumento;
import cl.atk.nomina.batch.domain.SimulatedDocumentoContable;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.domain.error.IntegrationErrorType;
import cl.atk.nomina.batch.service.NominaXmlParserService;
import cl.atk.nomina.batch.shared.exception.ArtikosIntegrationException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ProcurementDocumentProcessingServiceTest {

    private final NominaXmlParserService parser = new NominaXmlParserService(
            new ClassPathResource("samples/ZSVIDA_Nom15960.xml"));

    @Test
    void processDocumentsProcessesEveryDocumentInNomina() {
        Nomina nomina = nominaWithTwoDocuments();
        DocumentoContable first = nomina.documentos().get(0);
        DocumentoContable second = nomina.documentos().get(1);
        ProcurementIntegrationService integrationService = mock(ProcurementIntegrationService.class);
        when(integrationService.processDocument(ArtikosProfileType.VIDA, nomina, first))
                .thenReturn(resultado(first, "OK"));
        when(integrationService.processDocument(ArtikosProfileType.VIDA, nomina, second))
                .thenReturn(resultado(second, "NOK"));

        List<ResultadoDocumento> results = new ProcurementDocumentProcessingService(integrationService)
                .processDocuments(ArtikosProfileType.VIDA, nomina);

        assertThat(results).extracting(ResultadoDocumento::status).containsExactly("OK", "NOK");
        verify(integrationService).processDocument(ArtikosProfileType.VIDA, nomina, first);
        verify(integrationService).processDocument(ArtikosProfileType.VIDA, nomina, second);
    }

    @Test
    void processDocumentsStopsWhenAProcurementTechnicalErrorOccurs() {
        Nomina nomina = nominaWithTwoDocuments();
        DocumentoContable first = nomina.documentos().get(0);
        DocumentoContable second = nomina.documentos().get(1);
        ProcurementIntegrationService integrationService = mock(ProcurementIntegrationService.class);
        when(integrationService.processDocument(ArtikosProfileType.VIDA, nomina, first))
                .thenThrow(new ArtikosIntegrationException(
                        IntegrationErrorType.PROCUREMENT_TECHNICAL_ERROR,
                        ArtikosProfileType.VIDA.name(),
                        15960L,
                        ProcurementIntegrationService.OPERATION,
                        "timeout",
                        null));

        assertThatThrownBy(() -> new ProcurementDocumentProcessingService(integrationService)
                .processDocuments(ArtikosProfileType.VIDA, nomina))
                .isInstanceOf(ArtikosIntegrationException.class)
                .satisfies(exception -> assertThat(((ArtikosIntegrationException) exception).getErrorType())
                        .isEqualTo(IntegrationErrorType.PROCUREMENT_TECHNICAL_ERROR));

        verify(integrationService, never()).processDocument(ArtikosProfileType.VIDA, nomina, second);
    }

    private Nomina nominaWithTwoDocuments() {
        Nomina nomina = parser.parseSampleFile();
        DocumentoContable documento = nomina.documentos().get(0);
        return new Nomina(
                nomina.msgCode(),
                nomina.msgStatus(),
                nomina.msgFromAddress(),
                nomina.cabecera(),
                List.of(documento, secondDocument(documento)));
    }

    private DocumentoContable secondDocument(DocumentoContable source) {
        return new DocumentoContable(
                2,
                source.rutProveedor(),
                source.proveedor(),
                source.nacional(),
                source.idDocumento() + 1,
                source.usuario(),
                "3",
                source.tipoDocumento(),
                source.tipoErp(),
                source.fechaEmision(),
                source.fechaVencimiento(),
                source.fechaRecepcion(),
                source.fechaRecepSii(),
                source.urlDocumento(),
                source.observacion(),
                source.docCurrency(),
                source.usoIva(),
                source.montoNeto(),
                source.montoIva(),
                source.montoExento(),
                source.otrosImpuestos(),
                source.montoTotal(),
                source.referencias(),
                source.conciliaciones());
    }

    private ResultadoDocumento resultado(DocumentoContable documento, String status) {
        return new ResultadoDocumento(
                new SimulatedDocumentoContable(documento, 1, "15960-" + documento.idDocumento(), 15960L),
                status,
                status);
    }
}
