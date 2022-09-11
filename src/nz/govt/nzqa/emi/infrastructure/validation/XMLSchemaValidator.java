package nz.govt.nzqa.emi.infrastructure.validation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nz.govt.nzqa.emi.infrastructure.exception.InvalidXMLSchemaException;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.exception.XMLSchemaNotFoundException;
import nz.govt.nzqa.emi.infrastructure.logging.MessagingLoggerConfiguration;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class validates the XML schema against a DTD file.
 * 
 * @author Alisdair Hamblyn
 */

public class XMLSchemaValidator {

   /**
    * The xml document builder factory.
    */
   private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

   /**
    * The xml document builder.
    */
	private DocumentBuilder db;

    /**
     * An xml header used to turn the xml fragment into a proper xml document.
     */
   private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Claim PUBLIC \"-//Claim//EN\"";

    /**
     * The XMLSchemaValidator constructor. Sets up the document builder and
     * SAX parser error handler. Sets document validation to true.
     * @throws MessageException
     */
   public XMLSchemaValidator() throws MessageException {
      try {
            dbf.setValidating(true);
 		    db = dbf.newDocumentBuilder();
            db.setErrorHandler(new SAXParserErrorHandler());
  	        MessagingLoggerConfiguration.configure();
		} catch(ParserConfigurationException pce) {
			throw new InvalidXMLSchemaException(pce);
		}
   }

    /**
     * This method validates the DTD schema by creating an xml document with
     * the header and creating a DOM document. If this fails due to an invalid
     * or non-existent schema, a MessageException is thrown.
     * @param xmlData the xml fragment
     * @param schema the DTD url
     * @return boolean true if validation OK.
     * @throws MessageException
     */
   public boolean validateXML(String xmlData, String schema) throws MessageException {
      xmlData = xmlHeader + " \""+ schema + "\">" + xmlData;
      createDOMDocument(xmlData);
      return true;
   }

   private Document createDOMDocument(String data) throws MessageException {
		BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(data.getBytes()));
		try {
			Document doc = db.parse(is);
			return doc;
		} catch(SAXException se) {
            throw new InvalidXMLSchemaException(se);
		} catch(IOException ioe) {
            throw new XMLSchemaNotFoundException(ioe);
		}
	}
}
