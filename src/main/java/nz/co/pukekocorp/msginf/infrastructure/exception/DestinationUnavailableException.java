package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown when the underlying JMS objects are unavailable
 * or do not exist.
 * 
 * @author Alisdair Hamblyn
 */

public class DestinationUnavailableException extends MessageException {

   /**
    * Constructs a DestinationUnavailableException object.
    * @param message the exception message.
    */
   public DestinationUnavailableException(String message) {
      super(message);
   }

   /**
    * Constructs a DestinationUnavailableException object.
    * @param e the exception caught.
    */
   public DestinationUnavailableException(Exception e) {
      super(e);
   }
}
