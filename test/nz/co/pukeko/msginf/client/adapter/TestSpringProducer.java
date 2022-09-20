package nz.co.pukeko.msginf.client.adapter;

import nz.co.pukeko.msginf.infrastructure.util.ClassPathHacker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import nz.co.pukeko.msginf.client.adapter.spring.SpringProducer;

import java.io.IOException;

/**
 * JUnit test the Spring Producer.
 * 
 * @author Alisdair Hamblyn
 */
public class TestSpringProducer {
	private static Logger logger = LogManager.getLogger(TestSpringProducer.class);

	/**
	 * The Spring Producer. 
	 */
	private static SpringProducer producer;
	
	/**
	 * Create the Spring Producer.
	 */
	@BeforeAll
	public static void setUp() {
		try {
			ClassPathHacker.addFile("C:\\alisdair\\java\\apache-activemq-5.17.2\\activemq-all-5.17.2.jar");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
        producer = (SpringProducer) factory.getBean("producer");
	}
	
	/**
	 * Run the test.
	 */
	@Test
	public void sendMessage() {
		for (int i = 0; i < 10; i++) {
			String message = "Message[" + (i + 1) + "] from TestSpringProducer";
			logger.info("Sending \"" + message + "\"");
			producer.sendMessage(message);
		}
	}
}
