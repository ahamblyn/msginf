package nz.co.pukeko.msginf.client.adapter.spring;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * A Message producer to put messages onto a queue.
 * 
 * @author Alisdair Hamblyn
 */
public class SpringProducer {
	
	/**
	 * The Spring JMS Template as defined in the applicationContext.xml file.
	 */
    private JmsTemplate template;
    
    /**
     * The name of the queue to put messages onto.
     */
    private String queue;
    
    /**
     * Puts the message onto the queue.
     * @param messageData the message.
     */
    public void sendMessage(final String messageData) {
    	MessageCreator mc = new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(messageData);
                return message;
			}
		};
    	template.send(queue, mc);
    }

    /**
     * Gets the Spring JMS template.
     * @return the Spring JMS template.
     */
    public JmsTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the Spring JMS template.
     * @param template the Spring JMS template. 
     */
    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    /**
     * Gets the queue name.
     * @return the queue name.
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets the queue name.
     * @param queue the queue name.
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }
}
