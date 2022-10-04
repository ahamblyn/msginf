package nz.co.pukeko.msginf.client.adapter;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.naming.Context;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.listener.MessageRequestReply;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.PropertiesFileException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.BigFileReader;
import nz.co.pukeko.msginf.client.listener.MessageReceiver;

import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.models.message.MessageType;
import org.junit.jupiter.api.AfterAll;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MessageTest {
    protected static QueueManager queueManager;
    protected static QueueManager resetQueueManager;
	protected static MessageRequestReply messageRequestReply;
	protected static MessageInfrastructurePropertiesFileParser parser;

	public static void setUp() {
		try {
			parser = new MessageInfrastructurePropertiesFileParser();
			messageRequestReply = new MessageRequestReply(parser, "activemq",
					"QueueConnectionFactory", "RequestQueue",
					"ReplyQueue", "true");
		} catch (PropertiesFileException e) {
			throw new RuntimeException(e);
		}
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
			HeaderProperties<String,Object> resetProperties = new HeaderProperties<>();
			resetProperties.put("reset", Boolean.TRUE);
			// don't expect a reply and don't care what the message is either.
			MessageRequest messageRequest = TestUtil.createMessageRequest(MessageRequestType.SUBMIT, MessageType.TEXT, "", connector, "XXXXXXXXXX");
			messageRequest.setHeaderProperties(resetProperties);
			resetQueueManager.sendMessage(messageRequest);
		} catch (Exception e) {
			// don't care about the exception
		}
	}

	protected HeaderProperties<String,Object> createTestNameHeaderProperties(String testName) {
		HeaderProperties<String,Object> headerProperties = new HeaderProperties<>();
		headerProperties.put("testname", testName);
		return headerProperties;
	}

	protected String createRequestXML(String type, int size) {
		return "<?xml version=\"1.0\"?>" +
					 "<DataRequest>" +
					 "  <ReplyType>" + type + "</ReplyType>" +
					 "  <ReplySize>" + size + "</ReplySize>" +
					 "</DataRequest>";
	}
	
	protected ByteArrayOutputStream readStreamFromFile(String fileName) {
		// Try the "data" directory first - will work from Eclipse as the data directory 
		// is relative to the Eclipse working directory.
		// read binary file
		ByteArrayOutputStream bos;
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
				log.error(ex.getMessage(), ex);
			}
		}
		return bos;
	}
	
	protected byte[] readBinaryFile(String fileName) throws Exception {
		byte[] binaryData;
        BigFileReader bfr = new BigFileReader();
        binaryData = bfr.read2array(fileName);
        return binaryData;
	}
	
	protected void retrieveSubmitMessageSizesAndAnalyze(Context jmsContext, String queueConnectionFactoryName, String queueName, int expectedMessageSize) throws Exception {
		MessageReceiver mr = new MessageReceiver(parser, jmsContext, queueConnectionFactoryName, queueName);
		mr.setup();
		List<Integer> messageSizes = mr.readMessagesSizes();
		mr.close();
		// analyse message sizes
		if (messageSizes != null) {
            for (Integer messageSize : messageSizes) {
                if (messageSize != expectedMessageSize) {
                    fail("The message retrieved is not of the expected size: " + expectedMessageSize + ". A message of " + messageSize + " was retrieved.");
                }
            }
        }
	}
	
	protected void runSubmitTest(String connector, String fileName, int numberOfMessages) throws Exception {
		ByteArrayOutputStream bos = readStreamFromFile(fileName);
		if (bos != null) {
			for (int i = 0; i < numberOfMessages; i++) {
				// no reply expected as it is a submit
				MessageRequest messageRequest = TestUtil.createMessageRequest(MessageRequestType.SUBMIT, MessageType.BINARY, "", connector, "");
				messageRequest.setMessageStream(bos);
				messageRequest.setHeaderProperties(createTestNameHeaderProperties("submit"));
				queueManager.sendMessage(messageRequest);
			}
		} else {
			fail("Unable to read: " + fileName);
		}
	}
	
	protected void runReplyTest(String connector, String type, int size, int numberOfMessages) throws Exception {
		for (int i = 0; i < numberOfMessages; i++) {
			String message = createRequestXML(type, size);
			MessageRequest messageRequest = TestUtil.createMessageRequest(MessageRequestType.REQUEST_RESPONSE, MessageType.TEXT, "", connector, message);
			messageRequest.setHeaderProperties(createTestNameHeaderProperties("reply"));
			MessageResponse response = queueManager.sendMessage(messageRequest);
			if (type.equals("text")) {
				assertNotNull(response.getTextResponse());
				assertEquals(size, response.getTextResponse().length());
			} else {
				assertNotNull(response.getBinaryResponse());
				assertEquals(size, response.getBinaryResponse().length);
			}
		}
		sendResetCountMessage(connector);
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
	
	protected void runEchoTest(String connector, int numberOfMessages) throws Exception {
		for (int i = 0; i < numberOfMessages; i++) {
			String message = "Message[" + (i + 1) + "]";
			MessageRequest messageRequest = TestUtil.createMessageRequest(MessageRequestType.REQUEST_RESPONSE, MessageType.TEXT, "", connector, message);
			messageRequest.setHeaderProperties(createTestNameHeaderProperties("echo"));
			MessageResponse reply = queueManager.sendMessage(messageRequest);
			assertEquals(message, reply.getTextResponse());
			log.info(reply.toString());
		}
		sendResetCountMessage(connector);
		log.info(QueueStatisticsCollector.getInstance().toString());
	}
}
