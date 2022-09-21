package nz.co.pukeko.msginf.client.adapter;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.naming.Context;

import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.util.BigFileReader;
import nz.co.pukeko.msginf.client.listener.MessageReceiver;

import junit.framework.AssertionFailedError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageTest {
    protected static Logger logger = LogManager.getLogger(MessageTest.class);
    protected static QueueManager queueManager;
    protected static QueueManager resetQueueManager;
	protected static MessageRequestReply messageRequestReply;

	public static void setUp() {
		MessagingLoggerConfiguration.configure();
		messageRequestReply = new MessageRequestReply("activemq",
				"QueueConnectionFactory", "RequestQueue",
				"ReplyQueue", "true");
		messageRequestReply.run();
	}

	@AfterAll
	public static void tearDown() {
		// Sleep so messages finish processing before shutdown
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		messageRequestReply.shutdown();
		queueManager.close();
		resetQueueManager.close();
		QueueStatisticsCollector.getInstance().resetQueueStatistics();
		AdministerMessagingInfrastructure.getInstance().shutdown();
	}

	protected static void sendResetCountMessage(String connector) {
		try {
			HeaderProperties resetProperties = new HeaderProperties();
			resetProperties.put("reset", Boolean.TRUE);
			// don't expect a reply and don't care what the message is either.
			resetQueueManager.sendMessage(connector, "XXXXXXXXXX", resetProperties);
		} catch (Exception e) {
			// don't care about the exception
		}
	}

	protected HeaderProperties createTestNameHeaderProperties(String testName) {
		HeaderProperties headerProperties = new HeaderProperties();
		headerProperties.put("testname", testName);
		return headerProperties;
	}

	protected String createRequestXML(String type, String size) {
		String xml = "<?xml version=\"1.0\"?>" + 
					 "<DataRequest>" +
					 "  <ReplyType>" + type + "</ReplyType>" +
					 "  <ReplySize>" + size + "</ReplySize>" +
					 "</DataRequest>";
		return xml;
	}
	
	protected ByteArrayOutputStream readStreamFromFile(String fileName) {
		// Try the "data" directory first - will work from Eclipse as the data directory 
		// is relative to the Eclipse working directory.
		// read binary file
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			bos.write(readBinaryFile("data/" + fileName));
		} catch (Exception e) {
			// Try the "../../data" directory - directory used by the Ant build script when running the unit tests.
			bos = new ByteArrayOutputStream();
			try {
				bos = new ByteArrayOutputStream();
				bos.write(readBinaryFile("../../data/" + fileName));
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		return bos;
	}
	
	protected byte[] readBinaryFile(String fileName) throws Exception {
		byte[] binaryData = null;
        BigFileReader bfr = new BigFileReader();
        binaryData = bfr.read2array(fileName);
        return binaryData;
	}
	
	protected void retrieveSubmitMessageSizesAndAnalyze(Context jmsContext, String queueConnectionFactoryName, String queueName, int expectedMessageSize) throws Exception {
		MessageReceiver mr = new MessageReceiver(jmsContext, queueConnectionFactoryName, queueName);
		mr.setup();
		List<Integer> messageSizes = mr.readMessagesSizes();
		mr.close();
		// analyse message sizes
		if (messageSizes != null) {
            for (Integer messageSize : messageSizes) {
                if (messageSize != expectedMessageSize) {
                    throw new AssertionFailedError("The message retrieved is not of the expected size: " + expectedMessageSize + ". A message of " + messageSize.intValue() + " was retrieved.");
                }
            }
        }
	}
	
	protected void runSubmitTest(String connector, String fileName, int numberOfMessages) throws Exception {
		ByteArrayOutputStream bos = readStreamFromFile(fileName);
		if (bos != null) {
			for (int i = 0; i < numberOfMessages; i++) {
				// no reply expected as it is a submit
				queueManager.sendMessage(connector, bos, createTestNameHeaderProperties("submit"));
			}
		} else {
			throw new AssertionFailedError("Unable to read: " + fileName);
		}
	}
	
	protected void runReplyTest(String connector, String type, String size, int numberOfMessages) throws Exception {
		for (int i = 0; i < numberOfMessages; i++) {
			String message = createRequestXML(type, size);
			Object reply = queueManager.sendMessage(connector, message, createTestNameHeaderProperties("reply"));
			if (type.equals("text")) {
				// assert that a String is returned and it is of the size requested.
				if (!(reply instanceof String)) {
					throw new AssertionFailedError("The text reply message is not a String. A text message was requested.");
				}
				if (!(((String)reply).length() == Integer.parseInt(size))) {
					throw new AssertionFailedError("The text reply message is not of the size requested.");
				}
			} else {
				// assert that a byte[] is returned and it is of the size requested.
				if (!(reply instanceof byte[])) {
					throw new AssertionFailedError("The binary reply message is not a byte[]. A binary message was requested.");
				}
				if (!(((byte[])reply).length == Integer.parseInt(size))) {
					throw new AssertionFailedError("The binary reply message is not of the size requested.");
				}
			}
		}
		sendResetCountMessage(connector);
		logger.info(QueueStatisticsCollector.getInstance().toString());
	}
	
	protected void runEchoTest(String connector, int numberOfMessages) throws Exception {
		for (int i = 0; i < numberOfMessages; i++) {
			String message = "Message[" + (i + 1) + "]";
			Object reply = queueManager.sendMessage(connector, message, createTestNameHeaderProperties("echo"));
			assertEquals(message, (String)reply);
			logger.info(reply);
		}
		sendResetCountMessage(connector);
		logger.info(QueueStatisticsCollector.getInstance().toString());
	}
}
