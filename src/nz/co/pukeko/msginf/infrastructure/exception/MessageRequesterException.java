package nz.co.pukeko.msginf.infrastructure.exception;

/**
 * This exception is thrown if the message requester has an exception.
 * 
 * @author Alisdair Hamblyn
 */

public class MessageRequesterException extends MessageException {

    /**
     * Constructs a MessageRequesterException object.
     * @param message the exception message.
     */
    public MessageRequesterException(String message) {
        super(message);
    }
    
    /**
     * Constructs a MessageRequesterException object.
     * @param e the exception caught.
     */
    public MessageRequesterException(Exception e) {
    	super(e);
    }
}
