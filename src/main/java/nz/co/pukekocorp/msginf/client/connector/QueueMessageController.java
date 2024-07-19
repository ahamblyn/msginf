package nz.co.pukekocorp.msginf.client.connector;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.client.connector.channel.DestinationChannelFactory;
import nz.co.pukekocorp.msginf.client.connector.message.receive.MessageReceiver;
import nz.co.pukekocorp.msginf.client.connector.message.send.MessageSender;
import nz.co.pukekocorp.msginf.infrastructure.exception.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Optional;

/**
 * The QueueMessageController puts messages onto the queues defined in the properties file.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class QueueMessageController extends AbstractMessageController {

	/**
	 * The JAVAX_JMS queue.
	 */
	private javax.jms.Queue javaxQueue;

	/**
	 * The JAKARTA_JMS queue.
	 */
	private jakarta.jms.Queue jakartaQueue;

	/**
	 * The JAVAX_JMS reply queue.
	 */
	private javax.jms.Queue javaxReplyQueue;

	/**
	 * The JAKARTA_JMS reply queue.
	 */
	private jakarta.jms.Queue jakartaReplyQueue;

	/**
	 * The queue connection factory name.
	 */
	private final String queueConnFactoryName;

	/**
	 * The queue name.
	 */
	private final String queueName;

	/**
	 * The reply queue name.
	 */
	private String replyQueueName;

	/**
	 * The JAVAX_JMS request-reply message producer.
	 */
	private javax.jms.MessageProducer javaxRequestReplyMessageProducer;

	/**
	 * The JAKARTA_JMS request-reply message producer.
	 */
	private jakarta.jms.MessageProducer jakartaRequestReplyMessageProducer;

	/**
	 * The message request consumer.
	 */
	private ConsumerMessageRequester messageRequester;

	/**
	 * Whether a reply is expected.
	 */
	private final boolean replyExpected;

	/**
	 * Message time to live.
	 */
	private final int messageTimeToLive;

	/**
	 * The reply wait time.
	 */
	private int replyWaitTime = 0;

	/**
	 * Whether to use a message selector.
	 */
	private boolean useMessageSelector = true;

    /**
     * Constructs the QueueMessageController instance.
	 * @param parser the properties file parser.
     * @param messagingSystem the messaging system in the properties file to use.
     * @param connector the name of the connector as defined in the properties file.
     * @param jndiContext the JNDI context.
     * @throws MessageException Message exception
     */
	public QueueMessageController(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector,
                                  Context jndiContext) throws MessageException {
	    this.messagingSystem = messagingSystem;
	    this.connector = connector;
		this.useConnectionPooling = parser.getUseConnectionPooling(messagingSystem);
		this.jmsImplementation = parser.getJmsImplementation(messagingSystem);
		this.destinationChannelFactory = new DestinationChannelFactory(this, this.useConnectionPooling, this.connector);
		this.messageSender = new MessageSender(this);
		this.messageReceiver = new MessageReceiver(this);
		this.valid = true;
		if (parser.doesSubmitExist(messagingSystem, connector)) {
			this.replyExpected = false;
			this.queueName = parser.getSubmitConnectionSubmitQueueName(messagingSystem, connector);
			this.queueConnFactoryName = parser.getSubmitConnectionSubmitQueueConnFactoryName(messagingSystem, connector);
			this.messageTimeToLive = parser.getSubmitConnectionMessageTimeToLive(messagingSystem, connector);
			this.configMessageProperties = parser.getSubmitConnectionMessageProperties(messagingSystem, connector);
		} else if (parser.doesRequestReplyExist(messagingSystem, connector)) {
			this.replyExpected = true;
			this.queueName = parser.getRequestReplyConnectionRequestQueueName(messagingSystem, connector);
			this.replyQueueName = parser.getRequestReplyConnectionReplyQueueName(messagingSystem, connector);
			this.queueConnFactoryName = parser.getRequestReplyConnectionRequestQueueConnFactoryName(messagingSystem, connector);
			this.messageTimeToLive = parser.getRequestReplyConnectionMessageTimeToLive(messagingSystem, connector);
			this.replyWaitTime = parser.getRequestReplyConnectionReplyWaitTime(messagingSystem, connector);
			this.useMessageSelector = parser.getRequestReplyConnectionUseMessageSelector(messagingSystem, connector);
			this.configMessageProperties = parser.getRequestReplyConnectionMessageProperties(messagingSystem, connector);
		} else {
			// No configuration found.
			throw new ConfigurationException("The " + connector + " connector does not exist in the configuration file for the " + messagingSystem + " messaging system.");
		}
        try {
			setupJMSObjects(parser, messagingSystem, jndiContext);
        } catch (Exception e) {
		    // Invalidate the message controller.
		    setValid(false);
            throw new MessageControllerException(e);
        }
	}
   
    /**
     * This method sends the message to the JMS objects.
     * @param messageRequest the message request.
     * @return the message response.
     * @throws MessageException if the message cannot be sent.
     */
   public MessageResponse sendMessage(MessageRequest messageRequest) throws MessageException {
	   return messageSender.sendMessage(messageRequest, this.messagingSystem, this.connector, this.jmsImplementation);
   }

	/**
	 * Return the JAVAX_JMS destination.
	 * @return the JAVAX_JMS destination.
	 */
	public javax.jms.Destination getJavaxDestination() {
	   return javaxQueue;
	}

	/**
	 * Return the JAKARTA_JMS destination.
	 * @return the JAKARTA_JMS destination.
	 */
	public jakarta.jms.Destination getJakartaDestination() {
		return jakartaQueue;
	}

	/**
	 * Set up the JMS Objects
	 * @param parser the properties file parser
	 * @param messagingSystem the messaging system
	 * @param jndiContext the JNDI context
	 * @throws Exception exception
	 */
	public void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext)
			throws Exception {
		destinationChannel = makeNewDestinationChannel(parser, messagingSystem, jndiContext).orElseThrow(() -> {
			throw new RuntimeException("The destination channel cannot be created for " + messagingSystem);
		});
		messageControllerSetupFactory.setupMessageController(jndiContext, jmsImplementation);
	}

	/**
	 * Set up the Javax JMS Objects
	 * @param jndiContext the JNDI context
	 * @throws javax.jms.JMSException JMS exception
	 */
	public void setupJavaxJMSObjects(Context jndiContext)
			throws javax.jms.JMSException, NamingException {
		javaxQueue = (javax.jms.Queue)jndiContext.lookup(this.queueName);
		if (replyQueueName != null) {
			javaxReplyQueue = (javax.jms.Queue)jndiContext.lookup(replyQueueName);
		}
		javaxMessageProducer = destinationChannel.createMessageProducer(this.javaxQueue);
		javaxRequestReplyMessageProducer = destinationChannel.createMessageProducer(this.javaxQueue);
		if (messageTimeToLive > 0) {
			javaxMessageProducer.setTimeToLive(messageTimeToLive);
			javaxRequestReplyMessageProducer.setTimeToLive(messageTimeToLive);
		}
		// only create a requester for request-reply message controllers.
		if (replyExpected) {
			messageRequester = new ConsumerMessageRequester(destinationChannel, javaxRequestReplyMessageProducer, javaxReplyQueue,
					replyWaitTime, useMessageSelector);
		}
	}

	/**
	 * Set up the Jakarta JMS Objects
	 * @param jndiContext the JNDI context
	 * @throws jakarta.jms.JMSException JMS exception
	 */
	public void setupJakartaJMSObjects(Context jndiContext)
			throws jakarta.jms.JMSException, NamingException {
		jakartaQueue = (jakarta.jms.Queue)jndiContext.lookup(this.queueName);
		if (replyQueueName != null) {
			jakartaReplyQueue = (jakarta.jms.Queue)jndiContext.lookup(replyQueueName);
		}
		jakartaMessageProducer = destinationChannel.createMessageProducer(this.jakartaQueue);
		jakartaRequestReplyMessageProducer = destinationChannel.createMessageProducer(this.jakartaQueue);
		if (messageTimeToLive > 0) {
			jakartaMessageProducer.setTimeToLive(messageTimeToLive);
			jakartaRequestReplyMessageProducer.setTimeToLive(messageTimeToLive);
		}
		// only create a requester for request-reply message controllers.
		if (replyExpected) {
			messageRequester = new ConsumerMessageRequester(destinationChannel, jakartaRequestReplyMessageProducer, jakartaReplyQueue,
					replyWaitTime, useMessageSelector);
		}
	}

	/**
	 * Create the destination channel
	 * @param parser the properties file parser
	 * @param messagingSystem the messaging system
	 * @param jndiContext the JNDI context
	 * @return the destination channel
	 * @throws MessageException Message exception
	 */
	public Optional<DestinationChannel> makeNewDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException {
		try {
			DestinationChannel destinationChannel = (DestinationChannel) destinationChannelFactory.createDestinationChannel(parser, queueConnFactoryName, messagingSystem, jndiContext, jmsImplementation);
			return Optional.of(destinationChannel);
		} catch (Exception e) {
			// Invalidate the message controller.
			setValid(false);
			throw new DestinationChannelException("Unable to lookup the queue connection factory: " + queueConnFactoryName, e);
		}
	}

	public ConsumerMessageRequester getMessageRequester() {
		return messageRequester;
	}

	/**
     * Gets this object as a String.
     * @return this object as a String.
     */
    public String toString(){
        return "QueueName = "+ this.queueName + "...QueueConnectionFactoryName = "+ this.queueConnFactoryName;
    }
    
    /**
     * Release the MessageController's resources.
     */
    public void release() {
    	log.debug("Release the message controller.");
        try {
        	if (messageRequester != null) {
            	messageRequester.close();
        	}
        	if (javaxMessageProducer != null) {
				javaxMessageProducer.close();
        	}
			if (jakartaMessageProducer != null) {
				jakartaMessageProducer.close();
			}
        	if (javaxRequestReplyMessageProducer != null) {
				javaxRequestReplyMessageProducer.close();
        	}
			if (jakartaRequestReplyMessageProducer != null) {
				jakartaRequestReplyMessageProducer.close();
			}
			destinationChannel.close();
        } catch (javax.jms.JMSException | jakarta.jms.JMSException e) {
			// Invalidate the message controller.
			setValid(false);
        	log.error(e.getMessage(), e);
        }
    }

}
