package cl.atk.nomina.batch.procurement.mapper;

import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.procurement.config.ProcurementMappingProperties;
import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import org.springframework.stereotype.Component;

@Component
public class ProcurementMappingValidator {

    public void validate(ArtikosProfileType profile, ProcurementMappingProperties properties) {
        requireText(properties.getDocumentType(), "procurement.mapping.document-type");
        requireText(properties.getCodSistem(), "procurement.mapping.cod-sistem");
        requireText(company(profile, properties), "procurement.mapping.company-by-profile." + profile.name());
        requireValue(properties.getNumPeriodo(), "procurement.mapping.num-periodo");
        requireText(properties.getCodContbl(), "procurement.mapping.cod-contbl");
        requireText(properties.getCodTipUnid(), "procurement.mapping.cod-tip-unid");
        requireText(properties.getGrlCodItem(), "procurement.mapping.grl-cod-item");
        requireValue(properties.getValTipCambio(), "procurement.mapping.val-tip-cambio");
        requireValue(properties.getPctDscnto(), "procurement.mapping.pct-dscnto");
        requireValue(properties.getMtoDscnto(), "procurement.mapping.mto-dscnto");
        requireValue(properties.getPctIva(), "procurement.mapping.pct-iva");
        requireText(properties.getCodigoRecIva(), "procurement.mapping.codigo-rec-iva");
        requireValue(properties.getDefaultCantidad(), "procurement.mapping.default-cantidad");
    }

    public String company(ArtikosProfileType profile, ProcurementMappingProperties properties) {
        if (properties.getCompanyByProfile() == null) {
            return null;
        }
        return properties.getCompanyByProfile().get(profile.name());
    }

    private void requireText(String value, String propertyName) {
        if (value == null || value.isBlank() || "REPLACE_ME".equalsIgnoreCase(value.trim())) {
            throw new ProcurementMappingException("Missing procurement mapping property: " + propertyName);
        }
    }

    private void requireValue(Object value, String propertyName) {
        if (value == null) {
            throw new ProcurementMappingException("Missing procurement mapping property: " + propertyName);
        }
    }
}
