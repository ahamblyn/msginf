package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This exception is thrown when the DestinationChannel cannot create a queue
 * channel because the underlying JMS objects are unavailable or do not exist.
 * 
 * @author Alisdair Hamblyn
 */

public class DestinationChannelException extends MessageException {

   /**
    * Constructs a DestinationChannelException object.
    * @param message the exception message.
    */
   public DestinationChannelException(String message) {
      super(message);
   }

   /**
    * Constructs a DestinationChannelException object.
    * @param e the exception caught.
    */
   public DestinationChannelException(Exception e) {
      super(e);
   }

   /**
    * Constructs a DestinationChannelException object.
    * @param message the exception message.
    * @param e the exception caught.
    */
   public DestinationChannelException(String message, Exception e) {
      super(message, e);
   }

}
