package nz.govt.nzqa.emi.infrastructure.exception;

/**
 * This exception is thrown if the queue manager has an exception.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueManagerException extends MessageException {

    /**
     * Constructs a QueueManagerException object.
     * @param message the exception message.
     */
    public QueueManagerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a QueueManagerException object.
     * @param e the exception caught.
     */
    public QueueManagerException(Exception e) {
    	super(e);
    }
}
