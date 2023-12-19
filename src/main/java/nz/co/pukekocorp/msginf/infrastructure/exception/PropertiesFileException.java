package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown if the properties file is misconfigured.
 * 
 * @author Alisdair Hamblyn
 */

public class PropertiesFileException extends MessageException {

    /**
     * Constructs a PropertiesFileException object.
     * @param message the exception message.
     */
    public PropertiesFileException(String message) {
        super(message);
    }

    /**
     * Constructs a PropertiesFileException object.
     * @param e the exception caught.
     */
    public PropertiesFileException(Exception e) {
        super(e);
    }
}
