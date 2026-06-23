package cl.atk.nomina.batch.procurement.mapper;

import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.procurement.config.ProcurementMappingProperties;
import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import org.springframework.stereotype.Component;

@Component
public class ArtikosCompanyMapper {

    private final ProcurementMappingValidator validator;

    public ArtikosCompanyMapper(ProcurementMappingValidator validator) {
        this.validator = validator;
    }

    public String resolveCodEmpres(ArtikosProfileType profile, Nomina nomina, ProcurementMappingProperties properties) {
        String msgTo = nomina != null && nomina.cabecera() != null ? nomina.cabecera().msgTo() : null;
        if (!isBlank(msgTo)) {
            String normalized = msgTo.trim().toUpperCase();
            if ("001".equals(normalized) || "ZSGVIDA".equals(normalized) || "ZSVIDA".equals(normalized)) {
                return "001";
            }
            if ("002".equals(normalized) || "ZSGRALES".equals(normalized)) {
                return "002";
            }
            throw new ProcurementMappingException("Unsupported Artikos Msg_To for Procurement company mapping: " + msgTo);
        }

        String fallback = validator.company(profile, properties);
        if (isBlank(fallback)) {
            throw new ProcurementMappingException("Unable to resolve Procurement COD_EMPRES from Artikos Msg_To or profile");
        }
        return fallback;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
