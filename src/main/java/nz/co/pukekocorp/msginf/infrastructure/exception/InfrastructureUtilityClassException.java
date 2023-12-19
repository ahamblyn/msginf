package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown if an infrastructure utility class has an exception.
 * 
 * @author Alisdair Hamblyn
 */

public class InfrastructureUtilityClassException extends MessageException {

    /**
     * Constructs a InfrastructureUtilityClassException object.
     * @param message the exception message.
     */
    public InfrastructureUtilityClassException(String message) {
        super(message);
    }
    
    /**
     * Constructs a InfrastructureUtilityClassException object.
     * @param e the exception caught.
     */
    public InfrastructureUtilityClassException(Exception e) {
    	super(e);
    }
}
