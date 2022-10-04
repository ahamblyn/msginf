package nz.co.pukeko.msginf.client.adapter;

import java.io.ByteArrayOutputStream;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.infrastructure.util.BigFileReader;
import nz.co.pukeko.msginf.infrastructure.util.Util;

/**
 * A thread used to run tests.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class TestQueueManagerMessageHandler implements Runnable {
	
	/**
	 * The message count.
	 */
	private static long messageCount = 0;
	
	/**
	 * The QueueManager used to send the messages.
	 */
	private final QueueManager queueManager;

	/**
	 * The connector name.
	 */
	private final String connector;
	
	/**
	 * The number of messages to send in this thread.
	 */
	private int numberOfIterations;
	
	/**
	 * The data file name.
	 */
	private String dataFileName;
	
	/**
	 * The name of the test to run.
	 */
	private String testName;
	
	/**
	 * Constructs a TestQueueManagerMessageHandler object.
	 * @param messagingSystem the messaging system.
	 * @param connector the connector name.
	 * @throws MessageException Message exception
	 */
	public TestQueueManagerMessageHandler(String messagingSystem, String connector, boolean logStatistics) throws MessageException {
		this.connector = connector;
		MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
		queueManager = new QueueManager(parser, messagingSystem, logStatistics);
	}

	/**
	 * Constructs a TestQueueManagerMessageHandler object.
	 * @param messagingSystem the messaging system.
	 * @param connector the connector name.
	 * @param numberOfIterations the number of messages to send in this thread.
	 * @param dataFileName the data file name.
	 * @throws MessageException Message exception
	 */
	public TestQueueManagerMessageHandler(String messagingSystem, String connector, int numberOfIterations, String dataFileName, boolean logStatistics) throws MessageException {
		this(messagingSystem, connector, numberOfIterations, dataFileName, logStatistics, null);
	}
	
	/**
	 * Constructs a TestQueueManagerMessageHandler object.
	 * @param messagingSystem the messaging system.
	 * @param connector the connector name.
	 * @param numberOfIterations the number of messages to send in this thread.
	 * @param dataFileName the data file name.
	 * @param testName the name of the test to run.
	 * @throws MessageException Message exception
	 */
	public TestQueueManagerMessageHandler(String messagingSystem, String connector, int numberOfIterations, String dataFileName, boolean logStatistics, String testName) throws MessageException {
		this.connector = connector;
		this.numberOfIterations = numberOfIterations;
		this.dataFileName = dataFileName;
		this.testName = testName;
		MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
		queueManager = new QueueManager(parser, messagingSystem, logStatistics);
	}

	/**
	 * Send a message to reset the count of the message request reply program used to put
	 * messages onto the reply queue.
	 */
	public void sendResetCountMessage() {
		try {
			HeaderProperties<String,Object> resetProperties = new HeaderProperties<>();
			resetProperties.put("reset", Boolean.TRUE);
			// don't expect a reply and don't care what the message is either.
			queueManager.sendMessage(connector, "XXXXXXXXXX", resetProperties);
			queueManager.close();
		} catch (Exception e) {
			// don't care about the exception
		}
	}

	private synchronized long getNextMessageCount() {
		return ++messageCount;
	}
	
	/**
	 * Run the thread.
	 */
	public void run() {
		// if the file name ends in .dat or .zip then it is binary, else text.
		if (dataFileName.contains(".dat") || dataFileName.contains(".zip") || dataFileName.contains(".pdf")) {
			// read binary file
			byte[] binaryData;
			ByteArrayOutputStream bos;
	        BigFileReader bfr = new BigFileReader();
			try {
		        binaryData = bfr.read2array(dataFileName);
				bos = new ByteArrayOutputStream();
				bos.write(binaryData);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return;
			}
			for (int i = 0; i < numberOfIterations; i++) {
				try {
					Object reply = queueManager.sendMessage(connector, bos, createTestNameHeaderProperties(testName));
					handleReply(reply);
				} catch (MessageException me) {
					log.error("Message Exception", me);
				}
			}
		} else {
			String temp;
			try {
				temp = Util.readFile(dataFileName);
			} catch (MessageException me) {
				log.error("Message Exception", me);
				return;
			}
			for (int i = 0; i < numberOfIterations; i++) {
				try {
					Object reply = queueManager.sendMessage(connector, temp, createTestNameHeaderProperties(testName));
					handleReply(reply);
				} catch (MessageException me) {
					log.error("Message Exception", me);
				}
			}
		}
		queueManager.close();
	}

	private HeaderProperties<String,Object> createTestNameHeaderProperties(String testName) {
		HeaderProperties<String,Object> headerProperties = new HeaderProperties<>();
		headerProperties.put("testname", testName);
		return headerProperties;
	}

	private void handleReply(Object reply) {
		log.info("Message number: " + getNextMessageCount());
		if (reply != null) {
			if (testName.equals("reply")) {
				if (reply instanceof String response) {
					if (response.startsWith("TextMessage") || response.startsWith("BytesMessage")) {
						log.info(response);
					} else {
						log.info("Text Message of length " + ((String)reply).length() + " bytes returned.");
					}
				} else {
					// byte[] returned
					log.info("Binary Message of length " + ((byte[])reply).length + " bytes returned.");
				}
			} else {
				// echo test
				log.info(reply.toString());
			}
		}
	}
}
