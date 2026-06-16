package cl.poc.atkbatch.service.artikos;

import cl.poc.atkbatch.domain.Nomina;
import cl.poc.atkbatch.service.NominaXmlParserService;
import cl.poc.atkbatch.shared.exception.NominaXmlParsingException;
import java.text.Normalizer;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import java.io.StringReader;

@Component
public class ArtikosSoapResponseParser {

    private static final String NO_NOMINAS_MESSAGE = "No hay nominas para procesar";

    private final NominaXmlParserService nominaXmlParserService;

    public ArtikosSoapResponseParser(NominaXmlParserService nominaXmlParserService) {
        this.nominaXmlParserService = nominaXmlParserService;
    }

    public boolean isNoNominasResponse(String rawXml) {
        return normalized(rawXml).contains(normalized(NO_NOMINAS_MESSAGE));
    }

    public String extractNoNominasMessage(String rawXml) {
        String messageText = textByLocalName(rawXml, "MessageText");
        return messageText.isBlank() ? "No hay nominas para procesar" : messageText;
    }

    public Optional<Nomina> extractNomina(String rawXml) {
        if (isNoNominasResponse(rawXml)) {
            return Optional.empty();
        }
        if (!hasNode(rawXml, "Nomina")) {
            return Optional.empty();
        }
        return Optional.of(nominaXmlParserService.parseFromString(rawXml));
    }

    private boolean hasNode(String rawXml, String localName) {
        return !textByXPath(rawXml, "name(//*[local-name()='" + localName + "'][1])").isBlank();
    }

    private String textByLocalName(String rawXml, String localName) {
        return textByXPath(rawXml, "string(//*[local-name()='" + localName + "'][1])");
    }

    private String textByXPath(String rawXml, String expression) {
        try {
            Document document = parse(rawXml);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate("//*[local-name()='MessageId']", document, XPathConstants.NODE);
            String msgStatus = node == null ? "" : xpath.evaluate("string(./*[local-name()='MsgStatus'])", node);
            if (!msgStatus.isBlank() && !"0".equals(msgStatus)) {
                String messageText = xpath.evaluate("string(//*[local-name()='MessageText'][1])", document);
                throw new NominaXmlParsingException("Artikos respondio con MsgStatus " + msgStatus + ": " + messageText);
            }
            return xpath.evaluate(expression, document).trim();
        } catch (NominaXmlParsingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new NominaXmlParsingException("No fue posible parsear la respuesta SOAP de Artikos", exception);
        }
    }

    private Document parse(String rawXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
    }

    private String normalized(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
