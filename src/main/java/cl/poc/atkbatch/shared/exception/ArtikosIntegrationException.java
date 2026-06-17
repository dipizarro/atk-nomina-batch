package cl.poc.atkbatch.shared.exception;

public class ArtikosIntegrationException extends RuntimeException {

    public ArtikosIntegrationException(String message) {
        super(message);
    }

    public ArtikosIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
