package nz.govt.nzqa.emi.infrastructure.exception;

/**
 * This exception is thrown if the XML properties file is misconfigured.
 * 
 * @author Alisdair Hamblyn
 */

public class XMLPropertiesFileException extends MessageException {

    /**
     * Constructs a XMLPropertiesFileException object.
     * @param message the exception message.
     */
    public XMLPropertiesFileException(String message) {
        super(message);
    }

    /**
     * Constructs a XMLPropertiesFileException object.
     * @param e the exception caught.
     */
    public XMLPropertiesFileException(Exception e) {
        super(e);
    }
}
