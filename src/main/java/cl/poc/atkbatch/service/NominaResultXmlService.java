package cl.poc.atkbatch.service;

import cl.poc.atkbatch.domain.DocumentoContable;
import cl.poc.atkbatch.domain.ResultadoDocumento;
import cl.poc.atkbatch.domain.ResultadoNomina;
import org.springframework.stereotype.Service;

@Service
public class NominaResultXmlService {

    public String buildNomfactresXml(ResultadoNomina result) {
        StringBuilder xml = new StringBuilder();
        xml.append("<Message>");
        xml.append("<MessageId>");
        append(xml, "MsgCode", "NOMFACTRES");
        append(xml, "MsgCodSis", "SAF");
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
        DocumentoContable documento = result.simulatedDocumento().documentoOriginal();
        xml.append("<Doc>");
        append(xml, "DocFolio", documento.numeroDocumento());
        append(xml, "DocRutProveedor", documento.rutProveedor());
        append(xml, "DocTipoDoc", documento.tipoDocumento());
        append(xml, "Monto", documento.montoTotal());
        append(xml, "DocEstado", result.status());
        append(xml, "DocDescEstado", result.message());
        xml.append("</Doc>");
    }

    private void append(StringBuilder xml, String elementName, Object value) {
        xml.append('<').append(elementName).append('>');
        xml.append(escape(value == null ? "" : value.toString()));
        xml.append("</").append(elementName).append('>');
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
