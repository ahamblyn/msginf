package nz.govt.nzqa.emi.infrastructure.exception;

/**
 * This exception is thrown if the message controller has an exception.
 * 
 * @author Alisdair Hamblyn
 */

public class MessageControllerException extends MessageException {

    /**
     * Constructs a MessageControllerException object.
     * @param message the exception message.
     */
    public MessageControllerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a MessageControllerException object.
     * @param e the exception caught.
     */
    public MessageControllerException(Exception e) {
    	super(e);
    }
}
