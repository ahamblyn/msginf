package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown when the the QueueChannelPool cannot create a queue
 * channel because the underlying JMS objects are unavailable or do not exist.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueChannelException extends MessageException {

   /**
    * Constructs a QueueChannelException object.
    * @param message the exception message.
    */
   public QueueChannelException(String message) {
      super(message);
   }

   /**
    * Constructs a QueueChannelException object.
    * @param e the exception caught.
    */
   public QueueChannelException(Exception e) {
      super(e);
   }
}
