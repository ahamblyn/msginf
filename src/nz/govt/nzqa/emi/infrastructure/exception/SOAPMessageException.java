package nz.govt.nzqa.emi.infrastructure.exception;

/**
 * This exception is thrown if the XML is unable to be converted to a SOAP message.
 * 
 * @author Alisdair Hamblyn
 */

public class SOAPMessageException extends MessageException {

   /**
    * Constructs a SOAPMessageException object.
    * @param message the exception message.
    */
   public SOAPMessageException(String message) {
      super(message);
   }

   /**
    * Constructs a SOAPMessageException object.
    * @param e the exception caught.
    */
   public SOAPMessageException(Exception e) {
      super(e);
   }
}
