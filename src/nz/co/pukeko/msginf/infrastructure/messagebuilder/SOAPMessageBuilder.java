package nz.co.pukeko.msginf.infrastructure.messagebuilder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nz.co.pukeko.msginf.infrastructure.exception.SOAPMessageException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class creates the SOAP message from the XML data provided.
 * 
 * @author Alisdair Hamblyn
 */

public class SOAPMessageBuilder {

    /**
     * The xml document builder factory.
     */
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

   /**
    * The xml document builder.
    */
	private DocumentBuilder db;

    /**
     * The SOAPMessageBuilder constructor. Sets up the document builder.
     * @throws SOAPMessageException
     */
	public SOAPMessageBuilder() throws SOAPMessageException {
		try {
			db = dbf.newDocumentBuilder();
		} catch(ParserConfigurationException pce) {
			throw new SOAPMessageException(pce);
		}
	}
	
    /**
     * This method creates the SOAP xml message by adding the source and destination
     * to the SOAP header, and the xml data to the SOAP body.
     * @param source the source application
     * @param destination the destination application
     * @param bodyData the xml body data
     * @return the SOAP xml message
     * @throws SOAPMessageException
     */
	public String createMessage(String source, String destination, String bodyData) throws SOAPMessageException {
		String result = "";
		Element element;
	    try {
			//Create a SOAP message
			MessageFactory mf = MessageFactory.newInstance();
			SOAPMessage msg = mf.createMessage();

			//Create objects for the message parts
			SOAPPart soap = msg.getSOAPPart();
			SOAPEnvelope envelope = soap.getEnvelope();
			SOAPHeader header = envelope.getHeader();

	    	element = createDOMDocument(bodyData).getDocumentElement();

			// add the header data
            SOAPElement headerElement = header.addHeaderElement(envelope.createName("MessageHeader"));

            headerElement.addChildElement("MessageSource").addTextNode(source);
            headerElement.addChildElement("MessageDestination").addTextNode(destination);

			// 1. Turn the blank SOAP message into a DOM document
			Document soapDoc = db.newDocument();
			DOMResult target = new DOMResult(soapDoc);
			// (with a copying transformer)
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
          // this line doesn't like namespaces
			transformer.transform(soap.getContent(), target);

			// 2. Find the SOAP body in the DOM
			NodeList bodyList = soapDoc.getElementsByTagNameNS(SOAPConstants.URI_NS_SOAP_ENVELOPE,"Body");
			Element bodyElement = (Element)bodyList.item(0);

			// maybe delete its existing children using DOM methods, then:

			// 3. Add 'element' to the bodyElement
			bodyElement.appendChild(soapDoc.importNode(element,true));

			// 4. Load the DOM back into the SOAP message
			soap.setContent(new DOMSource(soapDoc));
			
			//Save the message
			msg.saveChanges();

			//Check the input
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			result = out.toString();
		} catch(SOAPException se) {
            throw new SOAPMessageException(se);
		} catch(TransformerConfigurationException tce) {
            throw new SOAPMessageException(tce);
		} catch(TransformerException te) {
            throw new SOAPMessageException(te);
		} catch(IOException ioe) {
            throw new SOAPMessageException(ioe);
		}
		return result;
	}

    /**
     * Extracts the text message from a SOAP message.
     * @param soapMessage the SOAP message.
     * @param element the XML element
     * @return the text message.
     * @throws SOAPMessageException
     */
	public String extractTextFromSOAPMessage(String soapMessage, String element) throws SOAPMessageException {
        Document doc = createDOMDocument(soapMessage);
        NodeList bodyList = doc.getElementsByTagName(element);
        Element bodyElement = (Element)bodyList.item(0);
        Text destination = (Text)bodyElement.getChildNodes().item(0);
        return destination.getNodeValue();
    }

    /**
     * Checks if the message is a SOAP message.
     * @param soapMessage the SOAP message.
     * @return true if it is a SOAP message.
     * @throws SOAPMessageException
     */
	public boolean checkIfSOAPMessage(String soapMessage) throws SOAPMessageException {
        boolean result = false;
        Document doc = createDOMDocument(soapMessage);
        NodeList bodyList = doc.getElementsByTagName("soap-env:Body");
        Element bodyElement = (Element)bodyList.item(0);
        if (bodyElement == null) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Extracts the payload message from a SOAP message.
     * @param soapMessage the SOAP message.
     * @return the payload message.
     * @throws SOAPMessageException
     */
	public String extractPayloadFromSOAPMessage(String soapMessage) throws SOAPMessageException {
        try {
            Document doc = createDOMDocument(soapMessage);
            NodeList bodyList = doc.getElementsByTagName("soap-env:Body");
            Element bodyElement = (Element)bodyList.item(0);
            Element payload = (Element)bodyElement.getChildNodes().item(0);
            DOMSource source = new DOMSource(payload);
            Document res = db.newDocument();
            DOMResult target = new DOMResult(res);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, target);
            return convertDOMToString(res);
        } catch (TransformerConfigurationException tce) {
            throw new SOAPMessageException(tce);
        } catch (TransformerException te) {
            throw new SOAPMessageException(te);
        }
    }

	private Document createDOMDocument(String data) throws SOAPMessageException {
		BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(data.getBytes()));
		try {
			Document doc = db.parse(is);
			return doc;
		} catch(SAXException se) {
			throw new SOAPMessageException(se);
		} catch(IOException ioe) {
			throw new SOAPMessageException(ioe);
		}
	}

    private String convertDOMToString(Document dom) throws SOAPMessageException {
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(dom), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
