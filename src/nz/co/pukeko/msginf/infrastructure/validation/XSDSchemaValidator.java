package nz.co.pukeko.msginf.infrastructure.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import nz.co.pukeko.msginf.infrastructure.exception.InvalidXMLSchemaException;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.XMLSchemaNotFoundException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class validates the XML schema against an XSD file. Each MessageController
 * has its own XSD schema validator.
 * 
 * @author Alisdair Hamblyn
 */

public class XSDSchemaValidator {

   /**
    * The DOM document builder.
    */
   private DocumentBuilder documentBuilder;

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
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
/*
          dbf.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", true);
          dbf.setFeature("http://xml.org/sax/features/validation", true);
          dbf.setFeature("http://xml.org/sax/features/namespaces", true);
          dbf.setFeature("http://apache.org/xml/features/validation/schema", true);
          dbf.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
          if (useNamespace) {
             dbf.setNamespaceAware(true);
          } else {
             dbf.setNamespaceAware(false);
          }
*/
          documentBuilder = dbf.newDocumentBuilder();
          documentBuilder.setErrorHandler(new SAXParserErrorHandler());
       } catch (ParserConfigurationException pce) {
          throw new InvalidXMLSchemaException(pce);
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
         documentBuilder.parse(new InputSource(byteStream));
      } catch (SAXException se) {
         throw new InvalidXMLSchemaException(se);
      } catch (IOException ioe) {
         throw new XMLSchemaNotFoundException(ioe);
      }
   }
}
