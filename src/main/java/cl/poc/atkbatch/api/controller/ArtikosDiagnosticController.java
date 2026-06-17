package cl.poc.atkbatch.api.controller;

import cl.poc.atkbatch.api.dto.ArtikosMaskedProfileConfigResponse;
import cl.poc.atkbatch.api.dto.ConfirmNominaRequest;
import cl.poc.atkbatch.api.dto.ConfirmNominaResponse;
import cl.poc.atkbatch.api.dto.FetchNominaRequest;
import cl.poc.atkbatch.api.dto.FetchNominaResponse;
import cl.poc.atkbatch.domain.Nomina;
import cl.poc.atkbatch.domain.artikos.ArtikosGenericResponse;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import cl.poc.atkbatch.service.artikos.ArtikosGenericSoapResponseParser;
import cl.poc.atkbatch.service.artikos.ArtikosMaskedConfigService;
import cl.poc.atkbatch.service.artikos.ArtikosSoapClient;
import cl.poc.atkbatch.service.artikos.ArtikosSoapClientException;
import cl.poc.atkbatch.service.artikos.ArtikosSoapResponseParser;
import cl.poc.atkbatch.shared.exception.NominaXmlParsingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/artikos/qa/nominas")
@Tag(name = "Artikos QA", description = "Diagnostico de conectividad SOAP QA para consulta NOMFACTERP")
public class ArtikosDiagnosticController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikosDiagnosticController.class);

    private final ArtikosSoapClient soapClient;
    private final ArtikosSoapResponseParser responseParser;
    private final ArtikosGenericSoapResponseParser genericResponseParser;
    private final ArtikosMaskedConfigService maskedConfigService;

    public ArtikosDiagnosticController(
            ArtikosSoapClient soapClient,
            ArtikosSoapResponseParser responseParser,
            ArtikosGenericSoapResponseParser genericResponseParser,
            ArtikosMaskedConfigService maskedConfigService) {
        this.soapClient = soapClient;
        this.responseParser = responseParser;
        this.genericResponseParser = genericResponseParser;
        this.maskedConfigService = maskedConfigService;
    }

    @PostMapping("/fetch")
    @Operation(summary = "Consulta nominas disponibles en Artikos QA con NOMFACTERP")
    public FetchNominaResponse fetchNomina(@Valid @RequestBody FetchNominaRequest request) {
        ArtikosProfileType profileType = parseProfile(request.profile());
        String rawXml = soapClient.fetchNominaRawXml(profileType);

        if (responseParser.isNoNominasResponse(rawXml)) {
            LOGGER.info("Artikos QA returned no nominas for profile={}", profileType);
            return new FetchNominaResponse(
                    profileType.name(),
                    false,
                    null,
                    null,
                    0,
                    responseParser.extractNoNominasMessage(rawXml));
        }

        Optional<Nomina> nomina = responseParser.extractNomina(rawXml);
        if (nomina.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Artikos QA no retorno una nomina ni un mensaje de no disponibilidad");
        }

        Nomina parsedNomina = nomina.get();
        return new FetchNominaResponse(
                profileType.name(),
                true,
                parsedNomina.cabecera().numeroNomina(),
                parsedNomina.cabecera().tipoNomina(),
                parsedNomina.cabecera().cantidadDocumentos(),
                "Nomina recibida correctamente desde Artikos");
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirma recepcion de nomina en Artikos QA con NOMFACTCONFIR")
    public ConfirmNominaResponse confirmNomina(@Valid @RequestBody ConfirmNominaRequest request) {
        ArtikosProfileType profileType = parseProfile(request.profile());
        String rawXml = soapClient.confirmNominaRawXml(
                profileType,
                request.numeroNomina(),
                request.estadoRespuesta());
        ArtikosGenericResponse response = genericResponseParser.parseGenericResponse(rawXml);

        return new ConfirmNominaResponse(
                profileType.name(),
                request.numeroNomina(),
                response.success(),
                response.msgStatus(),
                response.success()
                        ? "Confirmacion enviada correctamente a Artikos"
                        : response.messageText());
    }

    @GetMapping("/config/{profile}")
    @Operation(summary = "Muestra configuracion Artikos QA enmascarada por perfil")
    public ArtikosMaskedProfileConfigResponse getMaskedConfig(@PathVariable String profile) {
        return maskedConfigService.getMaskedConfig(parseProfile(profile));
    }

    private ArtikosProfileType parseProfile(String profile) {
        try {
            return ArtikosProfileType.from(profile);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @ExceptionHandler(ArtikosSoapClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String handleSoapClientException(ArtikosSoapClientException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(NominaXmlParsingException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String handleParsingException(NominaXmlParsingException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleConfigurationException(IllegalStateException exception) {
        return exception.getMessage();
    }
}
