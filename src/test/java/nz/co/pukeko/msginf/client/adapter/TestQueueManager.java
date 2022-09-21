package nz.co.pukeko.msginf.client.adapter;

import java.util.List;

import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.infrastructure.logging.MessagingLoggerConfiguration;
import nz.co.pukeko.msginf.infrastructure.socket.ShutdownThread;
import nz.co.pukeko.msginf.infrastructure.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to test the messaging infrastructure.
 * 
 * @author Alisdair Hamblyn
 */
public class TestQueueManager {
	private static final Logger logger = LogManager.getLogger(TestQueueManager.class);
	private final String testName;
	private final String messagingSystem;
	private final String connectorName;
	private final int numberOfThreads;
	private final int numberOfIterations;
	private final String dataFileName;

	/**
	 * Constructs the TestQueueManager.
	 * @param testName the test type: submit, echo, or reply.
	 * @param numberOfThreads the number of threads.
	 * @param numberOfIterations the number of messages to send per thread.
	 * @param dataFileName the data file name.
	 */
	public TestQueueManager(String testName, String messagingSystem, String connectorName, int numberOfThreads, int numberOfIterations, String dataFileName) {
		MessagingLoggerConfiguration.configure();
		this.testName = testName;
		this.messagingSystem = messagingSystem;
		this.connectorName = connectorName;
		this.numberOfThreads = numberOfThreads;
		this.numberOfIterations = numberOfIterations;
		this.dataFileName = dataFileName;
	}

	/**
	 * Runs the test.
	 * @param args the command line parameters.
	 */
	public static void main(String[] args) {
		// check the parameters
		if (args.length != 7) {
			usage();
			return;
		}
		try {
			String testName = args[0];
			if (!testName.equals("submit") && !testName.equals("echo") && !testName.equals("reply") && !testName.equals("receive")) {
				usage();
				return;
			}
			String messagingSystem = args[1];
			String connectorName = args[2];
			int threads = Integer.parseInt(args[3]);
			int iterations = Integer.parseInt(args[4]);
			String dataFileName = args[5];
            int port = Integer.parseInt(args[6]);
            TestQueueManager test = new TestQueueManager(testName, messagingSystem, connectorName, threads, iterations, dataFileName);
            // start the socket server thread
            test.startSocketServerThread(port);
            test.run();
			test.stats();
			test.close(port);
		} catch (NumberFormatException nfe) {
			System.out.println("Please enter numbers for <Threads>, <Messages>, and <Port>.");
		} catch (MessageException me) {
			logger.error(me);
		}
	}
	
	public static void usage() {
		System.out.println("Usage: TestQueueManager <Test> <System> <Connector> <Threads> <Messages> <File> <Port>");
		System.out.println("<Test>:			submit|echo|reply|receive");
		System.out.println("<System>:		The name of the system to be used as defined in the xml properties file.");
		System.out.println("<Connector>:	The name of the connector to be used as defined in the xml properties file.");
		System.out.println("<Threads>:		The number of threads to use in the test.");
		System.out.println("<Messages>:		The number of messages to send per thread.");
		System.out.println("<File>:			The file containing the message data.");
        System.out.println("<Port>:			The port the shutdown thread is listening to.");
	}
	
	/**
	 * Display the statistics for the test.
	 */
	public void stats() {
		logger.info(QueueStatisticsCollector.getInstance().toString());
	}

	/**
	 * Shutdown the messaging infrastructure.
	 */
	public void close() {
		AdministerMessagingInfrastructure.getInstance().shutdown();
	}

    /**
	 * Shutdown the messaging infrastructure using a port.
     * @param port the port
	 */
	public void close(int port) {
        // shutdown via the socket thread
        Util.connectToPort("localhost", port);
    }

    private void startSocketServerThread(int port) {
        Thread thread = new Thread(new ShutdownThread(port));
        thread.start();
    }

    private void requestReply(String testName) throws MessageException {
		Thread[] threads = new Thread[numberOfThreads];
		// start all the threads.
		for (int i = 0; i < numberOfThreads; i++) {
			threads[i] = new Thread(new TestQueueManagerMessageHandler(messagingSystem, connectorName, numberOfIterations, dataFileName, true, testName));
			threads[i].start();
		}
		// Join them to the main thread.
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// reset the message count once the threads have finished
		TestQueueManagerMessageHandler messageHandler = new TestQueueManagerMessageHandler(messagingSystem, connectorName, false);
		messageHandler.sendResetCountMessage();
	}
	
	private void submit() throws MessageException {
		Thread[] threads = new Thread[numberOfThreads];
		// start all the threads.
		for (int i = 0; i < numberOfThreads; i++) {
			threads[i] = new Thread(new TestQueueManagerMessageHandler(messagingSystem, connectorName, numberOfIterations, dataFileName, true, "submit"));
			threads[i].start();
		}
		// Join them to the main thread.
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void receive() throws MessageException {
		TestMessageReceiver mr = new TestMessageReceiver(messagingSystem, connectorName);
		List<String> messages = mr.receiveMessages(10000);
		if (messages != null) {
			for (int i = 0; i < messages.size(); i++) {
				String message = messages.get(i);
				logger.info("Message[" + i + "]: " + message);
			}
		} else {
			logger.info("No messages...");
		}
	}

	/**
	 * Runs the test.
	 * @throws MessageException Message exception
	 */
	public void run() throws MessageException {
		switch (testName) {
			case "submit" -> submit();
			case "echo" -> requestReply("echo");
			case "reply" -> requestReply("reply");
			case "receive" -> receive();
			default -> {
			}
		}
	}
}
