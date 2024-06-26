package nz.co.pukekocorp.msginf.client.connector;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.configuration.JmsImplementation;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.time.Instant;
import java.util.Optional;

/**
 * The TopicMessageController puts messages onto the topics defined in the properties file.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class TopicMessageController extends AbstractMessageController {

	/**
	 * The JAVAX_JMS topic.
	 */
	private javax.jms.Topic javaxTopic;

	/**
	 * The JAKARTA_JMS topic.
	 */
	private jakarta.jms.Topic jakartaTopic;

	/**
	 * The topic connection factory name.
	 */
	private final String topicConnFactoryName;

	/**
	 * The topic name.
	 */
	private final String topicName;

    /**
     * Constructs the TopicMessageController instance.
	 * @param parser the properties file parser.
     * @param messagingSystem the messaging system in the properties file to use.
     * @param connector the name of the connector as defined in the properties file.
     * @param jndiContext the JNDI context.
     * @throws MessageException Message exception
     */
	public TopicMessageController(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, String connector,
                                  Context jndiContext) throws MessageException {
		this.messagingSystem = messagingSystem;
	    this.connector = connector;
		this.useConnectionPooling = parser.getUseConnectionPooling(messagingSystem);
		this.jmsImplementation = parser.getJmsImplementation(messagingSystem);
		this.valid = true;
		if (parser.doesPublishSubscribeExist(messagingSystem, connector)) {
			this.topicName = parser.getPublishSubscribeConnectionPublishSubscribeTopicName(messagingSystem, connector);
			this.topicConnFactoryName = parser.getPublishSubscribeConnectionPublishSubscribeTopicConnFactoryName(messagingSystem, connector);
			this.messageTimeToLive = parser.getPublishSubscribeConnectionMessageTimeToLive(messagingSystem, connector);
			this.configMessageProperties = parser.getPublishSubscribeConnectionMessageProperties(messagingSystem, connector);
		} else {
			// No configuration found.
			throw new ConfigurationException("The " + connector + " connector does not exist in the configuration file for the " + messagingSystem + " messaging system.");
		}

      try {
		  if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
			  javaxTopic = (javax.jms.Topic) jndiContext.lookup(this.topicName);
		  }
		  if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
			  jakartaTopic = (jakarta.jms.Topic) jndiContext.lookup(this.topicName);
		  }
		  setupJMSObjects(parser, messagingSystem, jndiContext);
      } catch (javax.jms.JMSException | jakarta.jms.JMSException | NamingException e) {
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
    Instant start = Instant.now();
	MessageResponse messageResponse = new MessageResponse();
    messageResponse.setMessageRequest(messageRequest);
    try {
		if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
			javax.jms.Message jmsMessage = createJavaxMessage(messageRequest).orElseThrow(() -> {
				throw new RuntimeException("Unable to create JMS message.");
			});
			setMessageProperties(jmsMessage, messageRequest.getMessageProperties());
			((javax.jms.TopicPublisher)javaxMessageProducer).publish(jmsMessage);
			collateStats(connector, start);
			return messageResponse;
		}
		if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
			jakarta.jms.Message jmsMessage = createJakartaMessage(messageRequest).orElseThrow(() -> {
				throw new RuntimeException("Unable to create JMS message.");
			});
			setMessageProperties(jmsMessage, messageRequest.getMessageProperties());
			((jakarta.jms.TopicPublisher)jakartaMessageProducer).publish(jmsMessage);
			collateStats(connector, start);
			return messageResponse;
		}
    } catch (Exception e) {
    	// increment failed message count
		collector.incrementFailedMessageCount(messagingSystem, connector);
		// Invalidate the message controller.
		setValid(false);
		if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
			throw new DestinationUnavailableException(String.format("%s destination is unavailable", getJavaxDestination().toString()), e);
		}
		if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
			throw new DestinationUnavailableException(String.format("%s destination is unavailable", getJakartaDestination().toString()), e);
		}
    }
	   return messageResponse;
   }

	/**
	 * Return the JAVAX_JMS destination.
	 * @return the JAVAX_JMS destination.
	 */
   public javax.jms.Destination getJavaxDestination() {
		return javaxTopic;
	}

	/**
	 * Return the JAKARTA_JMS destination.
	 * @return the JAKARTA_JMS destination.
	 */
	public jakarta.jms.Destination getJakartaDestination() {
		return jakartaTopic;
	}

	/**
	 * Set up the JMS Objects
	 * @param parser the properties file parser
	 * @param messagingSystem the messaging system
	 * @param jndiContext the JNDI context
	 * @throws MessageException Message exception
	 * @throws javax.jms.JMSException JMS exception
	 * @throws jakarta.jms.JMSException JMS exception
	 */
    public void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext)
			throws MessageException, javax.jms.JMSException, jakarta.jms.JMSException {
		destinationChannel = makeNewDestinationChannel(parser, messagingSystem, jndiContext).orElseThrow(() -> {
			throw new RuntimeException("The destination channel cannot be created for " + messagingSystem);
		});
		if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
			javaxMessageProducer = ((TopicChannel) destinationChannel).createTopicPublisher(this.javaxTopic);
		}
		if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
			jakartaMessageProducer = ((TopicChannel) destinationChannel).createTopicPublisher(this.jakartaTopic);
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
			if (jmsImplementation == JmsImplementation.JAVAX_JMS) {
				javax.jms.TopicConnectionFactory topicConnectionFactory = (javax.jms.TopicConnectionFactory) jndiContext.lookup(topicConnFactoryName);
				javax.jms.TopicConnection topicConnection;
				topicConnection = topicConnectionFactory.createTopicConnection();
				topicConnection.start();
				javax.jms.Session session = topicConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
				var topicChannel = new TopicChannel(topicConnection, session, parser.getUseDurableSubscriber(messagingSystem));
				return Optional.of(topicChannel);
			}
			if (jmsImplementation == JmsImplementation.JAKARTA_JMS) {
				jakarta.jms.TopicConnectionFactory topicConnectionFactory = (jakarta.jms.TopicConnectionFactory) jndiContext.lookup(topicConnFactoryName);
				jakarta.jms.TopicConnection topicConnection;
				if (useConnectionPooling) { // only available for Jakarta JMS
					log.info("Using JMS Connection Pooling for " + messagingSystem + ":" + connector);
					int maxConnections = parser.getMaxConnections(messagingSystem);
					var jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
					jmsPoolConnectionFactory.setConnectionFactory(topicConnectionFactory);
					jmsPoolConnectionFactory.setMaxConnections(maxConnections);
					topicConnection = jmsPoolConnectionFactory.createTopicConnection();
				} else {
					topicConnection = topicConnectionFactory.createTopicConnection();
				}
				topicConnection.start();
				jakarta.jms.Session session = topicConnection.createSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE);
				var topicChannel = new TopicChannel(topicConnection, session, parser.getUseDurableSubscriber(messagingSystem));
				return Optional.of(topicChannel);
			}
		} catch (javax.jms.JMSException | jakarta.jms.JMSException | NamingException e) {
			// Invalidate the message controller.
			setValid(false);
			throw new DestinationChannelException("Unable to lookup the topic connection factory: " + topicConnFactoryName, e);
		}
		return Optional.empty();
	}

	/**
	 * Return the topich channel.
	 * @return the topich channel.
	 */
	public TopicChannel getTopicChannel() {
		return (TopicChannel) destinationChannel;
	}

    /**
     * Gets this object as a String.
     * @return this object as a String.
     */
    public String toString(){
        return "TopicName = "+ this.topicName + "...TopicConnectionFactoryName = "+ this.topicConnFactoryName;
    }
    
    /**
     * Release the MessageController's resources.
     */
    public void release() {
    	log.debug("Release the message controller.");
        try {
        	if (javaxMessageProducer != null) {
				javaxMessageProducer.close();
        	}
			if (jakartaMessageProducer != null) {
				jakartaMessageProducer.close();
			}
			destinationChannel.close();
        } catch (javax.jms.JMSException | jakarta.jms.JMSException e) {
			// Invalidate the message controller.
			setValid(false);
        	log.error(e.getMessage(), e);
        }
    }

}
