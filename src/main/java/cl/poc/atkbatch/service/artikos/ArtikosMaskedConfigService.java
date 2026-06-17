package cl.poc.atkbatch.service.artikos;

import cl.poc.atkbatch.api.dto.ArtikosMaskedOperationConfigResponse;
import cl.poc.atkbatch.api.dto.ArtikosMaskedProfileConfigResponse;
import cl.poc.atkbatch.config.ArtikosProperties;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationConfig;
import cl.poc.atkbatch.domain.artikos.ArtikosOperationType;
import cl.poc.atkbatch.domain.artikos.ArtikosProfileType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArtikosMaskedConfigService {

    private final ArtikosProperties artikosProperties;

    public ArtikosMaskedConfigService(ArtikosProperties artikosProperties) {
        this.artikosProperties = artikosProperties;
    }

    public ArtikosMaskedProfileConfigResponse getMaskedConfig(ArtikosProfileType profileType) {
        return new ArtikosMaskedProfileConfigResponse(
                profileType.name(),
                List.of(
                        maskedOperation(profileType, ArtikosOperationType.CONSUMO_NOMINA),
                        maskedOperation(profileType, ArtikosOperationType.RESPUESTA_NOMINA),
                        maskedOperation(profileType, ArtikosOperationType.RESULTADO_NOMINA)));
    }

    private ArtikosMaskedOperationConfigResponse maskedOperation(
            ArtikosProfileType profileType,
            ArtikosOperationType operationType) {
        ArtikosOperationConfig operationConfig = artikosProperties.requireOperationConfig(profileType, operationType);
        String endpoint = operationType == ArtikosOperationType.CONSUMO_NOMINA
                ? artikosProperties.getEndpoints().getNominaUrl()
                : artikosProperties.getEndpoints().getConnectorUrl();
        return new ArtikosMaskedOperationConfigResponse(
                operationType.getPropertyName(),
                endpoint,
                operationConfig.getMsgCode(),
                operationConfig.getMsgFromAddress(),
                operationConfig.getMsgCodFromAddress(),
                operationConfig.getMsgToAddress(),
                operationConfig.getMsgCodSis(),
                ArtikosTokenMasker.isPresent(operationConfig.getToken()),
                ArtikosTokenMasker.mask(operationConfig.getToken()));
    }
}
