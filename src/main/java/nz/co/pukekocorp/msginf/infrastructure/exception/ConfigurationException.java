package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown if the messaging infrastructure has been misconfigured.
 * 
 * @author Alisdair Hamblyn
 */

public class ConfigurationException extends MessageException {

    /**
     * Constructs a ConfigurationException object.
     * @param message the exception message.
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a ConfigurationException object.
     * @param e the exception caught.
     */
    public ConfigurationException(Exception e) {
    	super(e);
    }

    /**
     * Constructs a ConfigurationException object.
     * @param message the exception message.
     * @param e the exception caught.
     */
    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }

}
