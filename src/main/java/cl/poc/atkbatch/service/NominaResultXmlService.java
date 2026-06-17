package cl.poc.atkbatch.service;

import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.ResultadoNomina;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class NominaResultXmlService {

    private static final DateTimeFormatter ARTIKOS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public String buildNomfactresXml(ResultadoNomina result) {
        ArtikosOperationConfig defaultConfig = new ArtikosOperationConfig();
        defaultConfig.setMsgCode("NOMFACTRES");
        defaultConfig.setMsgFromAddress("");
        defaultConfig.setMsgToAddress("ARTIKOS");
        defaultConfig.setMsgCodSis("SAF");
        return buildNomfactresXml(result, defaultConfig);
    }

    public String buildNomfactresXml(ResultadoNomina result, ArtikosOperationConfig operationConfig) {
        StringBuilder xml = new StringBuilder();
        xml.append("<Message>");
        xml.append("<MessageId>");
        append(xml, "MsgCode", operationConfig.getMsgCode());
        append(xml, "MsgDesc", "Actualizacion de carga de documentos");
        append(xml, "MsgVersion", "V2.0");
        append(xml, "MsgFromAddress", operationConfig.getMsgFromAddress());
        append(xml, "MsgToAddress", operationConfig.getMsgToAddress());
        append(xml, "MsgDateTime", LocalDateTime.now().format(ARTIKOS_DATE_FORMAT));
        append(xml, "MsgNumber", "");
        append(xml, "MsgCodSis", operationConfig.getMsgCodSis());
        append(xml, "DocFileName", "");
        xml.append("</MessageId>");
        xml.append("<Respuesta>");
        xml.append("<Cabecera>");
        append(xml, "NumeroNomina", result.numeroNomina());
        append(xml, "CantidadOK", result.totalOk());
        append(xml, "CantidadNOK", result.totalNok());
        append(xml, "CantidadInformados", result.totalDocuments());
        xml.append("</Cabecera>");
        xml.append("<Documentos>");
        for (ResultadoDocumento documentoResult : result.documentos()) {
            appendDocumento(xml, documentoResult);
        }
        xml.append("</Documentos>");
        xml.append("</Respuesta>");
        xml.append("</Message>");
        return xml.toString();
    }

    private void appendDocumento(StringBuilder xml, ResultadoDocumento result) {
        xml.append("<Doc>");
        append(xml, "DocFolio", result.resolvedDocFolio());
        append(xml, "DocRutProveedor", result.resolvedDocRutProveedor());
        append(xml, "DocTipoDoc", result.resolvedDocTipoDoc());
        append(xml, "Monto", result.resolvedMonto());
        append(xml, "DocEstado", result.status());
        append(xml, "DocDescEstado", result.message());
        xml.append("</Doc>");
    }

    private void append(StringBuilder xml, String elementName, Object value) {
        xml.append('<').append(elementName).append('>');
        xml.append(escape(toText(value)));
        xml.append("</").append(elementName).append('>');
    }

    private String toText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.toPlainString();
        }
        return value.toString();
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
