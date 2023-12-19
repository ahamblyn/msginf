package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown when the Queue controller cannot be found in the
 * queue controller factory hashtable.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueControllerNotFoundException extends MessageException {

   /**
    * Constructs a QueueControllerNotFoundException object.
    * @param message the exception message.
    */
   public QueueControllerNotFoundException(String message) {
      super(message);
   }

   /**
    * Constructs a QueueControllerNotFoundException object.
    * @param e the exception caught.
    */
   public QueueControllerNotFoundException(Exception e) {
      super(e);
   }
}
