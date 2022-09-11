package nz.govt.nzqa.emi.infrastructure.exception;

/**
 * This exception is thrown when the XML data fails the dtd validation.
 * 
 * @author Alisdair Hamblyn
 */

public class InvalidXMLSchemaException extends MessageException {

   /**
    * Constructs a InvalidXMLSchemaException object.
    * @param message the exception message.
    */
   public InvalidXMLSchemaException(String message) {
      super(message);
   }

   /**
    * Constructs a InvalidXMLSchemaException object.
    * The InvalidXMLSchemaException constructor.
    * @param e the exception caught.
    */
   public InvalidXMLSchemaException(Exception e) {
      super(e);
   }
}
