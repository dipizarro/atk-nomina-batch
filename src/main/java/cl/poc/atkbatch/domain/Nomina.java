package cl.poc.atkbatch.domain;

import java.util.List;

public record Nomina(
        String msgCode,
        String msgStatus,
        String msgFromAddress,
        NominaHeader cabecera,
        List<DocumentoContable> documentos) {
}
