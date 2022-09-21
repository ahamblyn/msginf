package nz.co.pukeko.msginf.viewer.data;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * This class acts on messages on queues. 
 * @author alisdairh
 */
public class MessageQueue {
	private final Context context;
	private final Queue queue;
	private QueueSession queueSession;
	private final String messagingSystemName;
	
	/**
	 * Constructs the MessageQueue.
	 * @param messagingSystemName the messaging system name.
	 * @param context the JNDI context for the messaging system.
	 * @param queue the JMS queue to use.
	 * @throws JMSException JMS Exception
	 */
	public MessageQueue(String messagingSystemName, Context context, Queue queue) throws JMSException {
		this.messagingSystemName = messagingSystemName;
		this.context = context;
		this.queue = queue;
		open();
	}

	/**
	 * Constructs the MessageQueue.
	 * @param messagingSystemName the messaging system name.
	 * @param context the JNDI context for the messaging system.
	 * @param queueName the name of the queue to use.
	 * @throws JMSException JMS Exception
	 */
	public MessageQueue(String messagingSystemName, Context context, String queueName) throws NamingException, JMSException {
		this.messagingSystemName = messagingSystemName;
		this.context = context;
		// look up queue in context
		this.queue = (Queue)context.lookup("queue/" + queueName);
		open();
	}

	private void open() throws JMSException {
		QueueConnectionFactory queueConnectionFactory = getQueueConnectionFactory();
		QueueConnection queueConnection = Objects.requireNonNull(queueConnectionFactory).createQueueConnection();
		queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queueConnection.start();
	}

	/**
	 * Browses the messages on the queue.
	 * @return the messages as a List.
	 * @throws JMSException JMS Exception
	 */
	public List<Message> getMessages() throws JMSException {
		QueueBrowser queueBrowser = queueSession.createBrowser(queue);
		List<Message> messages = new ArrayList<>();
		Enumeration messageEnumeration = queueBrowser.getEnumeration();
		while (messageEnumeration.hasMoreElements()) {
			messages.add((Message)messageEnumeration.nextElement());
		}
		queueBrowser.close();
		return messages;
	}
	
	/**
	 * Deletes the messages from the queue.
	 * @return the number of messages deleted.
	 * @throws JMSException JMS Exception
	 */
	public int deleteMessages() throws JMSException {
		MessageConsumer consumer = queueSession.createConsumer(queue);
		int messageCount = 0;
		while (true) {
			Message message = consumer.receive(10000);
			if (message == null) {
				consumer.close();
				break;
			} else {
				messageCount++;
			}
		}
		return messageCount;
	}
	
	/**
	 * The name to use for the tab.
	 * @return the name to use for the tab.
	 */
	public String getTabName() {
		return messagingSystemName + ":" + queue.toString();
	}
	
	private QueueConnectionFactory getQueueConnectionFactory() {
		try {
			NamingEnumeration<Binding> ne = context.listBindings("");
			if (ne != null) {
				while (ne.hasMore()) {
					Binding binding = ne.next();
					if (binding != null) {
						Object o = binding.getObject();
						if (o instanceof QueueConnectionFactory) {
							return (QueueConnectionFactory)o;
						}
					}
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String toString() {
		return queue.toString();
	}

}
