package nz.co.pukeko.msginf.infrastructure.exception;

/**
 * This exception is thrown if the XML schema cannot be found to validate some
 * XML.
 * 
 * @author Alisdair Hamblyn
 */

public class XMLSchemaNotFoundException extends MessageException {

   /**
    * Constructs a XMLSchemaNotFoundException object.
    * @param message the exception message.
    */
   public XMLSchemaNotFoundException(String message) {
      super(message);
   }

   /**
    * Constructs a XMLSchemaNotFoundException object.
    * @param e the exception caught.
    */
   public XMLSchemaNotFoundException(Exception e) {
      super(e);
   }
}
