package nz.govt.nzqa.emi.infrastructure.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import nz.govt.nzqa.emi.infrastructure.exception.InvalidXMLSchemaException;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.exception.XMLSchemaNotFoundException;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class validates the XML schema against an XSD file. Each MessageController
 * has its own XSD schema validator.
 * 
 * @author Alisdair Hamblyn
 */

public class XSDSchemaValidator {

   /**
    * The DOM document parser.
    */
   private DOMParser parser;

   /**
    * An xml header used to turn the xml fragment into a proper xml document.
    */
   private String xmlHeader = "<?xml version=\"1.0\"?>";

   /**
    * The XSDSchemaValidator constructor. Sets up the DOM parser to use the
    * specified schema and the SAX parser error handler. Sets document
    * validation to true.
    * @param xsdFile the XSD url
    * @throws MessageException
    */
   public XSDSchemaValidator(String xsdFile) throws MessageException {
	   this(xsdFile, false);
   }

    /**
     * The XSDSchemaValidator constructor. Sets up the DOM parser to use the
     * specified schema and the SAX parser error handler. Sets document
     * validation to true.
     * @param xsdFile the XSD url
     * @param useNamespace whether to use namespaces or not
     * @throws MessageException
     */
    public XSDSchemaValidator(String xsdFile, boolean useNamespace) throws MessageException {
       try {
          parser = new DOMParser();
          parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", true);
          parser.setFeature("http://xml.org/sax/features/validation", true);
          parser.setFeature("http://xml.org/sax/features/namespaces", true);
          parser.setFeature("http://apache.org/xml/features/validation/schema", true);
          parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
          if (useNamespace) {
              parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", xsdFile);
          } else {
              parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", xsdFile);
          }
          parser.setErrorHandler(new SAXParserErrorHandler());
       }
       catch (SAXNotSupportedException snse) {
    	   throw new InvalidXMLSchemaException(snse);
       }
       catch (SAXNotRecognizedException snre) {
    	   throw new InvalidXMLSchemaException(snre);
       }
    }

   /**
    * This method validates the XSD schema by parsing the xml fragment with
    * the DOM parser. If this fails due to an invalid or non-existent schema,
    * a MessageException is thrown.
    * @param xmlData the xml fragment
    * @throws MessageException
    */
   public void validateXML(String xmlData) throws MessageException {
       // if the <?xml header is already present, don't add it
       if (!xmlData.startsWith("<?xml")) {
           xmlData = xmlHeader + xmlData;
       }
      parseXml(xmlData);
   }

   private void parseXml(String xml) throws MessageException {
      ByteArrayInputStream byteStream;
      byteStream = new ByteArrayInputStream(xml.getBytes());
      try {
         parser.parse(new InputSource(byteStream));
      } catch (SAXException se) {
         throw new InvalidXMLSchemaException(se);
      } catch (IOException ioe) {
         throw new XMLSchemaNotFoundException(ioe);
      }
   }
}
