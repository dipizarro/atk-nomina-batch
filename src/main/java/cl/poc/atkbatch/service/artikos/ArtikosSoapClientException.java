package cl.poc.atkbatch.service.artikos;

public class ArtikosSoapClientException extends RuntimeException {

    public ArtikosSoapClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtikosSoapClientException(String message) {
        super(message);
    }
}
