package nz.co.pukekocorp.msginf.infrastructure.exception;

/**
 * This class is the super class of all the messaging infrastructure exceptions.
 * 
 * @author Alisdair Hamblyn
 */

public class MessageException extends Exception {

   /**
    * Constructs a MessageException object.
    * @param message the exception message.
    */
   public MessageException(String message) {
      super(message, new Exception(message));
   }

   /**
    * Constructs a MessageException object.
    * @param e the exception caught.
    */
   public MessageException(Exception e) {
      super(e);
   }
   
   /**
    * Constructs a MessageException object.
    * @param message the exception message.
    * @param e the exception caught.
    */
   public MessageException(String message, Exception e) {
	   super(message, e);
   }

}
