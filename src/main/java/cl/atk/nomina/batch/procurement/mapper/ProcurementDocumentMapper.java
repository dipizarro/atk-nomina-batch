package cl.atk.nomina.batch.procurement.mapper;

import cl.atk.nomina.batch.domain.Conciliacion;
import cl.atk.nomina.batch.domain.DistribucionContable;
import cl.atk.nomina.batch.domain.DocumentoContable;
import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.procurement.config.ProcurementMappingProperties;
import cl.atk.nomina.batch.procurement.dto.ProcurementCmpDocumtDetRequest;
import cl.atk.nomina.batch.procurement.dto.ProcurementCmpDocumtDetRutRequest;
import cl.atk.nomina.batch.procurement.dto.ProcurementCmpDocumtRequest;
import cl.atk.nomina.batch.procurement.dto.ProcurementCmpRequest;
import cl.atk.nomina.batch.procurement.dto.ProcurementDocumentRequest;
import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProcurementDocumentMapper {

    private final ProcurementMappingProperties properties;
    private final ProcurementMappingValidator validator;
    private final ProcurementDateMapper dateMapper;

    public ProcurementDocumentMapper(
            ProcurementMappingProperties properties,
            ProcurementMappingValidator validator,
            ProcurementDateMapper dateMapper) {
        this.properties = properties;
        this.validator = validator;
        this.dateMapper = dateMapper;
    }

    public ProcurementDocumentRequest toCmpDocumentRequest(
            ArtikosProfileType profile,
            Nomina nomina,
            DocumentoContable documento) {
        if (profile == null) {
            throw new ProcurementMappingException("Artikos profile is required for Procurement mapping");
        }
        if (nomina == null) {
            throw new ProcurementMappingException("Nomina is required for Procurement mapping");
        }
        if (documento == null) {
            throw new ProcurementMappingException("DocumentoContable is required for Procurement mapping");
        }

        validator.validate(profile, properties);

        String documentType = properties.getDocumentType();
        Long rutProveedor = RutUtils.extractRutNumber(documento.rutProveedor());
        ProcurementCmpRequest cmp = new ProcurementCmpRequest(
                toCmpDocumt(profile, documento, rutProveedor),
                toDetails(documento),
                new ProcurementCmpDocumtDetRutRequest(rutProveedor, rutProveedor, "V"));

        return new ProcurementDocumentRequest(documentType, cmp, null);
    }

    private ProcurementCmpDocumtRequest toCmpDocumt(
            ArtikosProfileType profile,
            DocumentoContable documento,
            Long rutProveedor) {
        String fechaEmision = dateMapper.toProcurementDate(documento.fechaEmision());
        String fechaRecepcion = dateMapper.toProcurementDate(documento.fechaRecepcion());
        String fechaVencimiento = dateMapper.toProcurementDate(
                isBlank(documento.fechaVencimiento()) ? documento.fechaEmision() : documento.fechaVencimiento());

        return new ProcurementCmpDocumtRequest(
                properties.getCmpDocumentType(),
                validator.company(profile, properties),
                properties.getNumPeriodo(),
                rutProveedor,
                requiredText(documento.numeroDocumento(), "DocumentoContable.numeroDocumento"),
                properties.getCodSistem(),
                firstCodCuenta(documento),
                properties.getCodTipCuenta(),
                properties.getCodContbl(),
                properties.getDefaultCurrency(),
                fechaEmision,
                documentGloss(documento),
                fechaEmision,
                zeroIfNull(documento.montoNeto()),
                zeroIfNull(documento.montoExento()),
                zeroIfNull(documento.montoIva()),
                zeroIfNull(documento.montoTotal()),
                requiredLong(documento.numeroDocumento(), "DocumentoContable.numeroDocumento"),
                fechaVencimiento,
                properties.getCodigoRecIva(),
                fechaRecepcion);
    }

    private List<ProcurementCmpDocumtDetRequest> toDetails(DocumentoContable documento) {
        List<DistribucionContable> distribuciones = allDistribuciones(documento);
        if (distribuciones.isEmpty()) {
            throw new ProcurementMappingException("DocumentoContable must contain at least one distribucion");
        }

        List<ProcurementCmpDocumtDetRequest> details = new ArrayList<>();
        for (int index = 0; index < distribuciones.size(); index++) {
            DistribucionContable distribucion = distribuciones.get(index);
            BigDecimal unitValue = firstNonNull(distribucion.montoNeto(), distribucion.montoExento(), distribucion.montoTotal());
            details.add(new ProcurementCmpDocumtDetRequest(
                    index + 1,
                    properties.getCodTipUnid(),
                    properties.getGrlCodItem(),
                    distribucion.codCentroCosto(),
                    requiredText(distribucion.codCuentaContable(), "DistribucionContable.codCuentaContable"),
                    properties.getCodTipCuenta(),
                    properties.getLineGloss(),
                    properties.getDefaultCantidad(),
                    zeroIfNull(unitValue),
                    zeroIfNull(unitValue),
                    properties.getValTipCambio(),
                    properties.getPctDscnto(),
                    properties.getMtoDscnto(),
                    zeroIfNull(distribucion.montoExento()),
                    zeroIfNull(distribucion.montoNeto()),
                    properties.getPctIva(),
                    zeroIfNull(distribucion.montoIva()),
                    zeroIfNull(distribucion.montoTotal())));
        }
        return List.copyOf(details);
    }

    private List<DistribucionContable> allDistribuciones(DocumentoContable documento) {
        List<DistribucionContable> distribuciones = new ArrayList<>();
        if (documento.conciliaciones() == null) {
            return List.of();
        }
        for (Conciliacion conciliacion : documento.conciliaciones()) {
            if (conciliacion.distribuciones() != null) {
                distribuciones.addAll(conciliacion.distribuciones());
            }
        }
        return distribuciones;
    }

    private String firstCodCuenta(DocumentoContable documento) {
        return allDistribuciones(documento).stream()
                .map(DistribucionContable::codCuentaContable)
                .filter(value -> !isBlank(value))
                .findFirst()
                .orElse(null);
    }

    private String documentGloss(DocumentoContable documento) {
        if (!isBlank(documento.observacion())) {
            return documento.observacion();
        }
        return "Artikos " + safe(documento.proveedor()) + " doc " + safe(documento.numeroDocumento()).trim();
    }

    private String requiredText(String value, String fieldName) {
        if (isBlank(value)) {
            throw new ProcurementMappingException("Missing Artikos field for Procurement mapping: " + fieldName);
        }
        return value;
    }

    private Long requiredLong(String value, String fieldName) {
        String text = requiredText(value, fieldName);
        try {
            return Long.valueOf(text.trim());
        } catch (NumberFormatException exception) {
            throw new ProcurementMappingException(
                    "Invalid numeric Artikos field for Procurement mapping: " + fieldName + "=" + value);
        }
    }

    private BigDecimal firstNonNull(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
