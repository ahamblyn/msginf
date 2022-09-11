package nz.co.pukeko.msginf.infrastructure.exception;

/**
 * This exception is thrown if the ByteArrayWrapper has an exception.
 * 
 * @author Alisdair Hamblyn
 */

public class ByteArrayWrapperException extends MessageException {

    /**
     * Constructs a ByteArrayWrapperException object.
     * @param message the exception message.
     */
    public ByteArrayWrapperException(String message) {
        super(message);
    }
    
    /**
     * Constructs a ByteArrayWrapperException object.
     * @param e the exception caught.
     */
    public ByteArrayWrapperException(Exception e) {
    	super(e);
    }
}
