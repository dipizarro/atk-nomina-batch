package cl.atk.nomina.batch.domain.error;

public enum IntegrationErrorType {
    ARTIKOS_FETCH_ERROR,
    ARTIKOS_NO_NOMINAS,
    XML_PARSING_ERROR,
    NOMINA_CONFIRM_ERROR,
    NOMINA_PROCESSING_ERROR,
    NOMINA_RESULT_ERROR,
    ORACLE_CONTROL_ERROR,
    UNKNOWN_ERROR
}
