package cl.atk.nomina.batch.procurement.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "procurement.mapping")
public class ProcurementMappingProperties {

    private String documentType = "CMP";
    private String cmpDocumentType = "FEC";
    private String codSistem = "CM";
    private Map<String, String> companyByProfile = new HashMap<>(Map.of(
            "GENERALES", "002",
            "VIDA", "001"));
    private Integer numPeriodo;
    private String codContbl;
    private String codTipCuenta = "2";
    private String codTipUnid;
    private String grlCodItem;
    private String lineGloss = "BENEFICIOS AL PERSONAL";
    private String defaultCurrency = "CLP";
    private BigDecimal valTipCambio = BigDecimal.ONE;
    private BigDecimal pctDscnto = BigDecimal.ZERO;
    private BigDecimal mtoDscnto = BigDecimal.ZERO;
    private BigDecimal pctIva = new BigDecimal("19");
    private String codigoRecIva;
    private BigDecimal defaultCantidad = BigDecimal.ONE;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCmpDocumentType() {
        return cmpDocumentType;
    }

    public void setCmpDocumentType(String cmpDocumentType) {
        this.cmpDocumentType = cmpDocumentType;
    }

    public String getCodSistem() {
        return codSistem;
    }

    public void setCodSistem(String codSistem) {
        this.codSistem = codSistem;
    }

    public Map<String, String> getCompanyByProfile() {
        return companyByProfile;
    }

    public void setCompanyByProfile(Map<String, String> companyByProfile) {
        this.companyByProfile = companyByProfile;
    }

    public Integer getNumPeriodo() {
        return numPeriodo;
    }

    public void setNumPeriodo(Integer numPeriodo) {
        this.numPeriodo = numPeriodo;
    }

    public String getCodContbl() {
        return codContbl;
    }

    public void setCodContbl(String codContbl) {
        this.codContbl = codContbl;
    }

    public String getCodTipCuenta() {
        return codTipCuenta;
    }

    public void setCodTipCuenta(String codTipCuenta) {
        this.codTipCuenta = codTipCuenta;
    }

    public String getCodTipUnid() {
        return codTipUnid;
    }

    public void setCodTipUnid(String codTipUnid) {
        this.codTipUnid = codTipUnid;
    }

    public String getGrlCodItem() {
        return grlCodItem;
    }

    public void setGrlCodItem(String grlCodItem) {
        this.grlCodItem = grlCodItem;
    }

    public String getLineGloss() {
        return lineGloss;
    }

    public void setLineGloss(String lineGloss) {
        this.lineGloss = lineGloss;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public BigDecimal getValTipCambio() {
        return valTipCambio;
    }

    public void setValTipCambio(BigDecimal valTipCambio) {
        this.valTipCambio = valTipCambio;
    }

    public BigDecimal getPctDscnto() {
        return pctDscnto;
    }

    public void setPctDscnto(BigDecimal pctDscnto) {
        this.pctDscnto = pctDscnto;
    }

    public BigDecimal getMtoDscnto() {
        return mtoDscnto;
    }

    public void setMtoDscnto(BigDecimal mtoDscnto) {
        this.mtoDscnto = mtoDscnto;
    }

    public BigDecimal getPctIva() {
        return pctIva;
    }

    public void setPctIva(BigDecimal pctIva) {
        this.pctIva = pctIva;
    }

    public String getCodigoRecIva() {
        return codigoRecIva;
    }

    public void setCodigoRecIva(String codigoRecIva) {
        this.codigoRecIva = codigoRecIva;
    }

    public BigDecimal getDefaultCantidad() {
        return defaultCantidad;
    }

    public void setDefaultCantidad(BigDecimal defaultCantidad) {
        this.defaultCantidad = defaultCantidad;
    }
}
