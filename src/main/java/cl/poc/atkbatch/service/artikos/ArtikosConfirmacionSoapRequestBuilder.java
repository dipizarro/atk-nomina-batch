package cl.poc.atkbatch.service.artikos;

import cl.poc.atkbatch.domain.artikos.ArtikosProfileConfig;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ArtikosConfirmacionSoapRequestBuilder {

    private static final DateTimeFormatter ARTIKOS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String ARTIKOS_DOC_CONNECTOR_NAMESPACE = "AtkWs_DocConnectorB2B";

    public String buildNomfactconfirRequest(
            ArtikosProfileConfig profileConfig,
            Long numeroNomina,
            Integer estadoRespuesta) {
        validate(profileConfig, numeroNomina, estadoRespuesta);
        String messageDateTime = LocalDateTime.now().format(ARTIKOS_DATE_FORMAT);
        String msgDocument = buildMsgDocument(profileConfig, numeroNomina, estadoRespuesta, messageDateTime);

        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <EjecutaTrx xmlns="%s">
                      <token>%s</token>
                      <msgCode>NOMFACTCONFIR</msgCode>
                      <msgFromAdress>%s</msgFromAdress>
                      <MsgCodFromAdress>%s</MsgCodFromAdress>
                      <msgToAdress>%s</msgToAdress>
                      <msgDateTime>%s</msgDateTime>
                      <msgCodSis>%s</msgCodSis>
                      <msgCallBack></msgCallBack>
                      <msgDocument>%s</msgDocument>
                    </EjecutaTrx>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(
                ARTIKOS_DOC_CONNECTOR_NAMESPACE,
                escapeXml(profileConfig.getToken()),
                escapeXml(profileConfig.getMsgFromAddress()),
                escapeXml(profileConfig.getMsgCodFromAddress()),
                escapeXml(profileConfig.getMsgToAddress()),
                escapeXml(messageDateTime),
                escapeXml(profileConfig.getMsgCodSis()),
                escapeXml(msgDocument));
    }

    public String maskToken(String rawXml) {
        return rawXml.replaceAll("(?s)<token>.*?</token>", "<token>****</token>");
    }

    private String buildMsgDocument(
            ArtikosProfileConfig profileConfig,
            Long numeroNomina,
            Integer estadoRespuesta,
            String messageDateTime) {
        return """
                <Message>
                  <MessageId>
                    <MsgCode>NOMFACTCONFIR</MsgCode>
                    <MsgDesc>Confirmacion de recibo de nomina</MsgDesc>
                    <MsgVersion>V2.0</MsgVersion>
                    <MsgFromAddress>%s</MsgFromAddress>
                    <MsgToAddress>%s</MsgToAddress>
                    <MsgDateTime>%s</MsgDateTime>
                    <MsgNumber></MsgNumber>
                    <MsgCodSis>%s</MsgCodSis>
                    <DocFileName></DocFileName>
                  </MessageId>
                  <Respuesta>
                    <NumeroNomina>%s</NumeroNomina>
                    <EstadoRespuesta>%s</EstadoRespuesta>
                  </Respuesta>
                </Message>
                """.formatted(
                profileConfig.getMsgFromAddress(),
                profileConfig.getMsgToAddress(),
                messageDateTime,
                profileConfig.getMsgCodSis(),
                numeroNomina,
                estadoRespuesta);
    }

    private void validate(ArtikosProfileConfig profileConfig, Long numeroNomina, Integer estadoRespuesta) {
        requireText(profileConfig.getToken(), "token");
        requireText(profileConfig.getMsgFromAddress(), "msgFromAddress");
        requireText(profileConfig.getMsgCodFromAddress(), "msgCodFromAddress");
        requireText(profileConfig.getMsgToAddress(), "msgToAddress");
        requireText(profileConfig.getMsgCodSis(), "msgCodSis");
        if (numeroNomina == null) {
            throw new IllegalArgumentException("numeroNomina es obligatorio");
        }
        if (estadoRespuesta == null || (estadoRespuesta != 0 && estadoRespuesta != 1)) {
            throw new IllegalArgumentException("estadoRespuesta debe ser 0 o 1");
        }
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Configuracion Artikos incompleta: " + fieldName);
        }
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
