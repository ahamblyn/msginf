package nz.co.pukeko.msginf.client.adapter.jboss;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import nz.co.pukeko.msginf.client.adapter.QueueManager;
import nz.co.pukeko.msginf.client.adapter.MessageTest;

public class JBossSubmitTest extends MessageTest {
	
	public void setUp() {
		super.setUp();
		System.setProperty("org.jboss.mq.il.uil2.useServerHost", "true");
		try {
			// log the times
			queueManager = new QueueManager("jboss", true);
			// don't log the times
			resetQueueManager = new QueueManager("jboss", false);
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}
	
	private Context createJBossContext() throws NamingException {
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "org.jnp.interfaces:org.jboss.naming");
		props.setProperty("java.naming.provider.url", "jnp://localhost:1099");
		return new InitialContext(props);
	}
	
	public void testSubmitSmallFile() throws Exception {
		logger.info("Running submit small file test...");
		runSubmitTest("jboss_submit_binary", "8520.pdf", 10);
		// dequeue the messages and compare expected sizes
		retrieveSubmitMessageSizesAndAnalyze(createJBossContext(), "UIL2ConnectionFactory", "queue/SubmitQueue", 8520);
		logger.info("Submit small file test OK");
	}
}
