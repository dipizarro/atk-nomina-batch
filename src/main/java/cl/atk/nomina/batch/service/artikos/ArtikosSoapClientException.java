package cl.atk.nomina.batch.service.artikos;

public class ArtikosSoapClientException extends RuntimeException {

    public ArtikosSoapClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtikosSoapClientException(String message) {
        super(message);
    }
}
