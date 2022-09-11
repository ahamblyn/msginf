package nz.govt.nzqa.emi.client.adapter;

import java.io.ByteArrayOutputStream;

import nz.govt.nzqa.emi.infrastructure.data.HeaderProperties;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.infrastructure.logging.MessagingLoggerConfiguration;
import nz.govt.nzqa.emi.infrastructure.util.BigFileReader;
import nz.govt.nzqa.emi.infrastructure.util.Util;
import nz.govt.nzqa.emi.client.adapter.QueueManager;

import org.apache.log4j.Logger;

/**
 * A thread used to run tests.
 * 
 * @author Alisdair Hamblyn
 */
public class TestQueueManagerMessageHandler implements Runnable {
	
	/**
	 * The log4j logger.
	 */
	private static Logger logger = Logger.getLogger(TestQueueManagerMessageHandler.class);
	
	/**
	 * The message count.
	 */
	private static long messageCount = 0;
	
	/**
	 * The QueueManager used to send the messages.
	 */
	private QueueManager queueManager;

	/**
	 * The connector name.
	 */
	private String connector;
	
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
	 * @throws MessageException
	 */
	public TestQueueManagerMessageHandler(String messagingSystem, String connector, boolean logStatistics) throws MessageException {
		MessagingLoggerConfiguration.configure();
		this.connector = connector;
		queueManager = new QueueManager(messagingSystem, logStatistics);
	}

	/**
	 * Constructs a TestQueueManagerMessageHandler object.
	 * @param messagingSystem the messaging system.
	 * @param connector the connector name.
	 * @param numberOfIterations the number of messages to send in this thread.
	 * @param dataFileName the data file name.
	 * @throws MessageException
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
	 * @throws MessageException
	 */
	public TestQueueManagerMessageHandler(String messagingSystem, String connector, int numberOfIterations, String dataFileName, boolean logStatistics, String testName) throws MessageException {
		MessagingLoggerConfiguration.configure();
		this.connector = connector;
		this.numberOfIterations = numberOfIterations;
		this.dataFileName = dataFileName;
		this.testName = testName;
		queueManager = new QueueManager(messagingSystem, logStatistics);
	}

	/**
	 * Send a message to reset the count of the message request reply program used to put
	 * messages onto the reply queue.
	 */
	public void sendResetCountMessage() {
		try {
			HeaderProperties resetProperties = new HeaderProperties();
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
		if (dataFileName.indexOf(".dat") != -1 || dataFileName.indexOf(".zip") != -1 || dataFileName.indexOf(".pdf") != -1) {
			// read binary file
			byte[] binaryData = null;
			ByteArrayOutputStream bos = null;
	        BigFileReader bfr = new BigFileReader();
			try {
		        binaryData = bfr.read2array(dataFileName);
				bos = new ByteArrayOutputStream();
				bos.write(binaryData);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return;
			}
			for (int i = 0; i < numberOfIterations; i++) {
				try {
					Object reply = queueManager.sendMessage(connector, bos, createTestNameHeaderProperties(testName));
					handleReply(reply);
				} catch (MessageException me) {
					logger.error("Message Exception", me);
				}
			}
		} else {
			String temp = null;
			try {
				temp = Util.readFile(dataFileName);
			} catch (MessageException me) {
				logger.error("Message Exception", me);
				return;
			}
			for (int i = 0; i < numberOfIterations; i++) {
				try {
					Object reply = queueManager.sendMessage(connector, temp, createTestNameHeaderProperties(testName));
					handleReply(reply);
				} catch (MessageException me) {
					logger.error("Message Exception", me);
				}
			}
		}
		queueManager.close();
	}

	private HeaderProperties createTestNameHeaderProperties(String testName) {
		HeaderProperties headerProperties = new HeaderProperties();
		headerProperties.put("testname", testName);
		return headerProperties;
	}

	private void handleReply(Object reply) {
		logger.info("Message number: " + getNextMessageCount());
		if (reply != null) {
			if (testName.equals("reply")) {
				if (reply instanceof String) {
					String response = (String)reply;
					if (response.startsWith("TextMessage") || response.startsWith("BytesMessage")) {
						logger.info(response);
					} else {
						logger.info("Text Message of length " + ((String)reply).length() + " bytes returned.");
					}
				} else {
					// byte[] returned
					logger.info("Binary Message of length " + ((byte[])reply).length + " bytes returned.");
				}
			} else {
				// echo test
				logger.info(reply);
			}
		}
	}
}
