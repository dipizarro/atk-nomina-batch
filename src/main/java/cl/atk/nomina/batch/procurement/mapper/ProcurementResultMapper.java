package cl.atk.nomina.batch.procurement.mapper;

import cl.atk.nomina.batch.domain.DocumentoContable;
import cl.atk.nomina.batch.domain.ResultadoDocumento;
import cl.atk.nomina.batch.domain.SimulatedDocumentoContable;
import cl.atk.nomina.batch.procurement.dto.ProcurementDocumentPostResult;
import cl.atk.nomina.batch.shared.util.StringSanitizer;
import org.springframework.stereotype.Component;

@Component
public class ProcurementResultMapper {

    private static final int MESSAGE_MAX_LENGTH = 300;
    private static final String OK_MESSAGE = "Documento procesado correctamente en Procurement";
    private static final String NOK_DEFAULT_MESSAGE = "Documento rechazado por Procurement";

    public ResultadoDocumento toResultadoDocumento(
            Long numeroNomina,
            DocumentoContable documento,
            ProcurementDocumentPostResult procurementResult) {
        boolean successful = procurementResult.successful();
        return new ResultadoDocumento(
                new SimulatedDocumentoContable(
                        documento,
                        1,
                        "%d-%d".formatted(numeroNomina, documento.idDocumento()),
                        numeroNomina),
                successful ? "OK" : "NOK",
                successful ? OK_MESSAGE : nokMessage(procurementResult),
                documento.numeroDocumento(),
                documento.rutProveedor(),
                documento.tipoDocumento(),
                documento.montoTotal());
    }

    private String nokMessage(ProcurementDocumentPostResult procurementResult) {
        String message = procurementResult.errorMessage();
        if (message == null || message.isBlank()) {
            message = procurementResult.message();
        }
        if (message == null || message.isBlank()) {
            message = NOK_DEFAULT_MESSAGE;
        }
        return StringSanitizer.compactAndTruncate(message, MESSAGE_MAX_LENGTH);
    }
}
