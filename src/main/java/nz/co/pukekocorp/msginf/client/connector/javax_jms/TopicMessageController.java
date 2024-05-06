package nz.co.pukekocorp.msginf.client.connector.javax_jms;

import javax.jms.*;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.exception.*;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageResponse;

import javax.naming.Context;
import javax.naming.NamingException;
import java.time.Instant;

/**
 * The TopicMessageController puts messages onto the topics defined in the properties file.
 * 
 * @author Alisdair Hamblyn
 */
@Slf4j
public class TopicMessageController extends AbstractMessageController {

   /**
    * The application JMS topic.
    */
   private final Topic topic;

   /**
    * The topic connection factory name.
    */
   private final String topicConnFactoryName;

   /**
    * The application topic name.
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
	  this.connector = connector;
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
         topic = (Topic) jndiContext.lookup(this.topicName);
		  setupJMSObjects(parser, messagingSystem, jndiContext);
      } catch (JMSException | NamingException e) {
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
        Message jmsMessage = createMessage(messageRequest).orElseThrow(() -> {
			throw new RuntimeException("Unable to create JMS message.");
		});
		setMessageProperties(jmsMessage, messageRequest.getMessageProperties());
		((TopicPublisher)messageProducer).publish(jmsMessage);
		collateStats(connector, start);
        return messageResponse;
    } catch (JMSException e) {
    	// increment failed message count
		collector.incrementFailedMessageCount(connector);
        throw new DestinationUnavailableException(e);
    }
   }

	public Destination getDestination() {
		return topic;
	}

	/**
	 * Set up the JMS Objects
	 * @param parser the properties file parser
	 * @param messagingSystem the messaging system
	 * @param jndiContext the JNDI context
	 * @throws MessageException Message exception
	 * @throws JMSException JMS exception
	 */
    public void setupJMSObjects(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException, JMSException {
		destinationChannel = makeNewDestinationChannel(parser, messagingSystem, jndiContext);
		messageProducer = ((TopicChannel) destinationChannel).createTopicPublisher(this.topic);
	}

	/**
	 * Create the destination channel
	 * @param parser the properties file parser
	 * @param messagingSystem the messaging system
	 * @param jndiContext the JNDI context
	 * @return the destination channel
	 * @throws MessageException Message exception
	 */
	public DestinationChannel makeNewDestinationChannel(MessageInfrastructurePropertiesFileParser parser, String messagingSystem, Context jndiContext) throws MessageException {
		try {
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup(topicConnFactoryName);
			TopicConnection topicConnection;
			topicConnection = topicConnectionFactory.createTopicConnection();
			topicConnection.start();
			Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			return new TopicChannel(topicConnection, session);
		} catch (JMSException | NamingException e) {
			throw new DestinationChannelException("Unable to lookup the topic connection factory: " + topicConnFactoryName, e);
		}
	}

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
        	if (messageProducer != null) {
				messageProducer.close();
        	}
			destinationChannel.close();
        } catch (JMSException e) {
        	log.error(e.getMessage(), e);
        }
    }

}
