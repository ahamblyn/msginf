package nz.govt.nzqa.emi.client.adapter.joram;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nz.govt.nzqa.emi.client.adapter.QueueManager;
import nz.govt.nzqa.emi.infrastructure.exception.MessageException;
import nz.govt.nzqa.emi.client.adapter.MessageTest;

public class JORAMSubmitTest extends MessageTest {
	
	public void setUp() {
		super.setUp();
		try {
			// log the times
			queueManager = new QueueManager("joram", true);
			// don't log the times
			resetQueueManager = new QueueManager("joram", false);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}
	
	private Context createJORAMContext() throws NamingException {
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
		props.setProperty("java.naming.factory.host", "localhost");
		props.setProperty("java.naming.factory.port", "16400");
		return new InitialContext(props);
	}
	
	public void testSubmitSmallFile() throws Exception {
		logger.info("Running submit small file test...");
		runSubmitTest("joram_submit_binary", "8520.pdf", 10);
		// dequeue the messages and compare expected sizes
		retrieveSubmitMessageSizesAndAnalyze(createJORAMContext(), "qcf", "TestQueue", 8520);
		logger.info("Submit small file test OK");
	}
}
