package nz.co.pukeko.msginf.infrastructure.exception;

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

   /**
    * Gets the exception message.
    * @return the exception message.
    */
   public String getMessage() {
      return super.getMessage();
   }

   /**
    * Prints the exception stack trace.
    */
   public void printStackTrace() {
	   super.printStackTrace();
   }
}
