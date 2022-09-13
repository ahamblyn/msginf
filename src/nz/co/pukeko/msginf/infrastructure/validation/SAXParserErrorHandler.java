package nz.co.pukeko.msginf.infrastructure.validation;

import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class handles any SAX parser exceptions from the XML schema validation.
 * 
 * @author Alisdair Hamblyn
 */

public class SAXParserErrorHandler extends DefaultHandler {

    /**
     * The log4j logger.
     */
   private static Logger logger = LogManager.getLogger(SAXParserErrorHandler.class);

    /**
     * The SAXParserErrorHandler constructor.
     */
   public SAXParserErrorHandler() {
      MessagingLoggerConfiguration.configure();
   }

    /**
     * This method handles a SAX parser warning.
     * @param ex the SAX parser exception
     * @throws SAXException
     */
   public void warning(SAXParseException ex) throws SAXException {
      logger.warn("WARNING: " + ex.getMessage());
   }

   /**
    * This method handles a SAX parser error.
    * @param ex the SAX parser exception
    * @throws SAXException
    */
   public void error(SAXParseException ex) throws SAXException {
      logger.warn("ERROR: " + ex.getMessage());
      throw new SAXException(ex);
   }

   /**
    * This method handles a SAX parser fatal error.
    * @param ex the SAX parser exception
    * @throws SAXException
    */
   public void fatalError(SAXParseException ex) throws SAXException {
      logger.warn("FATAL ERROR: " + ex.getMessage());
      throw new SAXException(ex);
   }
}
