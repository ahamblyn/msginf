package nz.co.pukeko.msginf.client.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.exception.QueueManagerException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.pref.xmlbeans.XMLMessageInfrastructurePropertiesFileParser;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This servlet passes requests to the messaging infrastructure via the QueueManager.
 * It is used by non-Java clients to communicate with the messaging infrastructure via HTTP.
 * Three parameters are sent in the request: messaging system, connector and data:
 * Messaging system is the name of the  system to use in the XML properties file.
 * Connector is the name of the connector in the XML properties file and data is the message data. 
 * For asynchronous (submit) messages, the client can wait for and read an acknowledgement
 * in the response body.

 * For synchronous (request/reply) messages, the client waits for and reads the response in the 
 * response body.
 * It can only handle text messages.
 * 
 * @author Alisdair Hamblyn
 */
public class SenderServlet extends HttpServlet {

	/**
	 * The QueueManagers.
	 */
	private Hashtable<String,QueueManager> queueManagers;
	
    /**
	 * The log4j2 logger.
     */
	private static final Logger logger = LogManager.getLogger(SenderServlet.class);

    /**
     * Initialises the servlet.
     * @param config the servlet configuration.
     * @throws ServletException servlet exception
     */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		MessagingLoggerConfiguration.configure();
		try {
			initialiseQueueManagers();
		} catch (MessageException me) {
			logger.error(me.getMessage(), me);
			throw new ServletException(me);
		}
	}

	/**
     * Handles GET requests.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws IOException IO exception
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		String messagingSystem = request.getParameter("messagingSystem");
		String connector = request.getParameter("connector");
		String data = request.getParameter("data");
		// if the connector and data are null then a multipart request
		// has been sent
		if (connector == null && data == null) {
			Hashtable<String,String> requestData = extractMultipartFormRequestData(request);
			data = requestData.get("data");
		}
		String resetCount = request.getParameter("resetCount");
		if (resetCount == null || resetCount.equals("")) {
			resetCount = "false";
		}
		boolean bResetCount = Boolean.parseBoolean(resetCount);
		String debug = request.getParameter("debug");
		if (debug == null || debug.equals("")) {
			debug = "false";
		}
		boolean bDebug = Boolean.parseBoolean(debug);
		if (connector == null || connector.equals("")) {
			connector = "RequestReplyConnector";
		}
		// no data at all
		if (data == null || data.equals("")) {
			noFileMessageData(out);
			return;
		}
		String messageID = request.getParameter("messageID");
		if (messageID == null || messageID.equals("")) {
			messageID = Long.toString(System.currentTimeMillis());
		}
		if (bDebug) {
			logger.debug("Messaging System," + messagingSystem);
			logger.debug("Connector," + connector);
			logger.debug("Data," + data);
		}
		try {
			String payload;
			if (bResetCount) {
				payload = "RESET_MESSAGE_COUNT";
			} else {
				payload = "<Data><MessageID>" + messageID + "</MessageID>" + data + "</Data>";
			}
			long time = System.currentTimeMillis();
			if (bDebug) {
				logger.debug("Payload," + payload);
			}
			// get the required queue manager
			QueueManager qm = queueManagers.get(messagingSystem);
			if (qm == null) {
				// queue manager not found - inform user
				throw new QueueManagerException("The queue manager for " + messagingSystem + " was not found.");
			}
			String result = (String)qm.sendMessage(connector, payload);
			long timeTaken = System.currentTimeMillis() - time;
			logger.debug("Time taken for QueueManager to deal with the message," + timeTaken / 1000f);
			// if the reply from the queue manager is null then the message was not expecting a reply,
			// therefore it is a submit (asynchronous) message.
			if (result == null) {
				submitAcknowledgeMessage(out);
			} else {
				replyMessage(out, result);
			}
		} catch (MessageException me) {
			exceptionMessageData(out, me);
		}
	}

	private Hashtable<String,String> extractMultipartFormRequestData(HttpServletRequest request) {
		Hashtable<String,String> result = new Hashtable<>();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> data = upload.parseRequest(request);
            for (FileItem fi : data) {
                result.put(fi.getFieldName(), fi.getString());
            }
        } catch (Exception e) {
			logger.error("File Upload Exception", e);
		}
		return result;
	}

	/**
	 * Initialise the QueueManager.
	 */
	private void initialiseQueueManagers() throws MessageException {
		if (queueManagers == null) {
			queueManagers = new Hashtable<>();
			// get the available messaging systems from the XML properties file
			XMLMessageInfrastructurePropertiesFileParser parser = new XMLMessageInfrastructurePropertiesFileParser();
			List<String> availableMessagingSystems = parser.getAvailableMessagingSystems();
            for (String messagingSystem : availableMessagingSystems) {
                // create queue managers which log statistics.
                queueManagers.put(messagingSystem, new QueueManager(messagingSystem, true));
            }
            logger.debug(queueManagers);
		}
	}

	/**
     * Handles POST requests.
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @throws IOException IO exception
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}

	/**
	 * Gets the servlet info.
	 * @return the servlet info.
	 */
	public String getServletInfo() {
		return "Sender Servlet";
	}

	private void submitAcknowledgeMessage(PrintWriter out) {
		out.print("Message sent successfully.");
		out.flush();
		out.close();
	}

	private void replyMessage(PrintWriter out, String result) {
		out.print(result);
		out.flush();
		out.close();
	}

	private void noFileMessageData(PrintWriter out) {
		out.print("No file data entered.");
		out.flush();
		out.close();
	}

	private void exceptionMessageData(PrintWriter out, Exception e) {
		out.println(e.getMessage());
		e.printStackTrace(out);
		out.flush();
		out.close();
	}
}