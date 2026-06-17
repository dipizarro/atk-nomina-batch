package cl.poc.atkbatch.api.controller;

import cl.poc.atkbatch.api.dto.ArtikosMaskedProfileConfigResponse;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import cl.poc.atkbatch.service.artikos.ArtikosMaskedConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/artikos/qa/config")
@Tag(name = "Artikos QA", description = "Diagnostico de configuracion SOAP QA")
public class ArtikosConfigController {

    private final ArtikosMaskedConfigService maskedConfigService;

    public ArtikosConfigController(ArtikosMaskedConfigService maskedConfigService) {
        this.maskedConfigService = maskedConfigService;
    }

    @GetMapping("/{profile}")
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

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleConfigurationException(IllegalStateException exception) {
        return exception.getMessage();
    }
}
