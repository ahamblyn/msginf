package nz.co.pukeko.msginf.client.adapter;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import nz.co.pukeko.msginf.client.adapter.spring.SpringProducer;
import junit.framework.TestCase;

/**
 * JUnit test the Spring Producer.
 * 
 * @author Alisdair Hamblyn
 */
public class TestSpringProducer extends TestCase {
	
	/**
	 * The Spring Producer. 
	 */
	private SpringProducer producer;
	
	/**
	 * Create the Spring Producer.
	 */
	public void setUp() {
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
        producer = (SpringProducer) factory.getBean("producer");
	}
	
	/**
	 * Run the test.
	 */
	public void testSendMessage() {
		for (int i = 0; i < 10; i++) {
			String message = "Message[" + (i + 1) + "] from TestSpringProducer";
			System.out.println("Sending \"" + message + "\"");
			producer.sendMessage(message);
		}
	}
}
