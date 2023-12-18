package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown when the underlying JMS objects are unavailable
 * or do not exist.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueUnavailableException extends MessageException {

   /**
    * Constructs a QueueUnavailableException object.
    * @param message the exception message.
    */
   public QueueUnavailableException(String message) {
      super(message);
   }

   /**
    * Constructs a QueueUnavailableException object.
    * @param e the exception caught.
    */
   public QueueUnavailableException(Exception e) {
      super(e);
   }
}
