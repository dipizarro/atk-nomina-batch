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
import cl.atk.nomina.batch.procurement.lookup.ProcurementItemLookupResult;
import cl.atk.nomina.batch.procurement.lookup.ProcurementMappingLookupService;
import cl.atk.nomina.batch.procurement.lookup.ProcurementTaxTypeResolver;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ProcurementDocumentMapper {

    private final ProcurementMappingProperties properties;
    private final ProcurementMappingValidator validator;
    private final ProcurementDateMapper dateMapper;
    private final ArtikosDocumentTypeMapper documentTypeMapper;
    private final ArtikosCompanyMapper companyMapper;
    private final ProcurementUsoIvaMapper usoIvaMapper;
    private final ProcurementTaxTypeResolver taxTypeResolver;
    private final ProcurementMappingLookupService lookupService;

    public ProcurementDocumentMapper(
            ProcurementMappingProperties properties,
            ProcurementMappingValidator validator,
            ProcurementDateMapper dateMapper,
            ArtikosDocumentTypeMapper documentTypeMapper,
            ArtikosCompanyMapper companyMapper,
            ProcurementUsoIvaMapper usoIvaMapper,
            ProcurementTaxTypeResolver taxTypeResolver,
            ProcurementMappingLookupService lookupService) {
        this.properties = properties;
        this.validator = validator;
        this.dateMapper = dateMapper;
        this.documentTypeMapper = documentTypeMapper;
        this.companyMapper = companyMapper;
        this.usoIvaMapper = usoIvaMapper;
        this.taxTypeResolver = taxTypeResolver;
        this.lookupService = lookupService;
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
        String codEmpres = companyMapper.resolveCodEmpres(profile, nomina, properties);
        String codMoneda = resolveCurrency(documento.docCurrency());
        List<MappedDetail> details = toDetails(documento, codEmpres, codMoneda);
        ProcurementCmpRequest cmp = new ProcurementCmpRequest(
                toCmpDocumt(nomina, documento, rutProveedor, codEmpres, codMoneda, details),
                details.stream().map(MappedDetail::request).toList(),
                new ProcurementCmpDocumtDetRutRequest(rutProveedor, rutProveedor, "V"));

        return new ProcurementDocumentRequest(documentType, cmp, null);
    }

    private ProcurementCmpDocumtRequest toCmpDocumt(
            Nomina nomina,
            DocumentoContable documento,
            Long rutProveedor,
            String codEmpres,
            String codMoneda,
            List<MappedDetail> details) {
        String fechaEmision = dateMapper.toProcurementDate(documento.fechaEmision());
        String fechaRecepcion = dateMapper.toProcurementDate(documento.fechaRecepcion());
        String fechaVencimiento = dateMapper.toProcurementDate(
                isBlank(documento.fechaVencimiento()) ? documento.fechaEmision() : documento.fechaVencimiento());
        String codContbl = singleCodContbl(details);

        return new ProcurementCmpDocumtRequest(
                documentTypeMapper.toProcurementDocumentType(documento.tipoErp()),
                codEmpres,
                properties.getNumPeriodo(),
                rutProveedor,
                requiredText(documento.numeroDocumento(), "DocumentoContable.numeroDocumento"),
                properties.getCodSistem(),
                firstCodCuenta(documento),
                properties.getCodTipCuenta(),
                codContbl,
                codMoneda,
                fechaEmision,
                documentGloss(documento),
                fechaEmision,
                zeroIfNull(documento.montoNeto()),
                zeroIfNull(documento.montoExento()),
                zeroIfNull(documento.montoIva()),
                zeroIfNull(documento.montoTotal()),
                requiredLong(documento.numeroDocumento(), "DocumentoContable.numeroDocumento"),
                fechaVencimiento,
                usoIvaMapper.normalize(documento.usoIva()),
                fechaRecepcion);
    }

    private List<MappedDetail> toDetails(DocumentoContable documento, String codEmpres, String codMoneda) {
        List<DetailSource> sources = allDetailSources(documento);
        if (sources.isEmpty()) {
            throw new ProcurementMappingException("DocumentoContable must contain at least one distribucion");
        }

        List<MappedDetail> details = new ArrayList<>();
        for (int index = 0; index < sources.size(); index++) {
            DetailSource source = sources.get(index);
            DistribucionContable distribucion = source.distribucion();
            BigDecimal unitValue = firstNonNull(distribucion.montoNeto(), distribucion.montoExento(), distribucion.montoTotal());
            Long codCuenta = requiredLong(distribucion.codCuentaContable(), "DistribucionContable.codCuentaContable");
            String codImpsto = taxTypeResolver.resolve(distribucion.montoNeto());
            ProcurementItemLookupResult lookup = lookupService.resolveItemForDistribution(
                    codEmpres,
                    properties.getNumPeriodo(),
                    properties.getCodSistem(),
                    codMoneda,
                    codCuenta,
                    codImpsto);
            ProcurementCmpDocumtDetRequest request = new ProcurementCmpDocumtDetRequest(
                    index + 1,
                    lookup.codTipUnid(),
                    lookup.grlCodItem(),
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
                    zeroIfNull(distribucion.montoTotal()));
            details.add(new MappedDetail(request, lookup));
        }
        return List.copyOf(details);
    }

    private String singleCodContbl(List<MappedDetail> details) {
        return details.stream()
                .map(detail -> detail.lookup().codContbl())
                .filter(value -> !isBlank(value))
                .distinct()
                .reduce((left, right) -> {
                    throw new ProcurementMappingException("Ambiguous Procurement COD_CONTBL from ASI lookup: " + left + ", " + right);
                })
                .orElseThrow(() -> new ProcurementMappingException("ASI lookup did not return COD_CONTBL"));
    }

    private List<DistribucionContable> allDistribuciones(DocumentoContable documento) {
        return allDetailSources(documento).stream().map(DetailSource::distribucion).toList();
    }

    private List<DetailSource> allDetailSources(DocumentoContable documento) {
        List<DistribucionContable> distribuciones = new ArrayList<>();
        List<DetailSource> sources = new ArrayList<>();
        if (documento.conciliaciones() == null) {
            return List.of();
        }
        for (Conciliacion conciliacion : documento.conciliaciones()) {
            if (conciliacion.distribuciones() != null) {
                for (DistribucionContable distribucion : conciliacion.distribuciones()) {
                    sources.add(new DetailSource(conciliacion, distribucion));
                }
            }
        }
        return sources;
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

    private String resolveCurrency(String docCurrency) {
        if (isBlank(docCurrency)) {
            return properties.getDefaultCurrency();
        }
        String normalized = docCurrency.trim().toUpperCase();
        if ("CLP".equals(normalized) || "$".equals(normalized)) {
            return "$";
        }
        return properties.getDefaultCurrency();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record DetailSource(Conciliacion conciliacion, DistribucionContable distribucion) {
    }

    private record MappedDetail(ProcurementCmpDocumtDetRequest request, ProcurementItemLookupResult lookup) {
    }
}
