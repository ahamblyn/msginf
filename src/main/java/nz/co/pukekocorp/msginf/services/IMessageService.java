package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.message.RestMessageRequest;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;

import java.util.List;
import java.util.Optional;

/**
 * The Message Service interface.
 */
public interface IMessageService {

    /**
     * Submit an asynchronous message
     * @param payload the message
     * @return the message response
     */
    Optional<RestMessageResponse> submit(RestMessageRequest payload);

    /**
     * Receive (read) messages off a queue
     * @param messagingSystem the messaging system
     * @param messageConnector the connector to use
     * @param timeout the timeout in ms to wait
     * @return the messages read
     */
    List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout);

    /**
     * Submit a synchronous message
     * @param payload the message
     * @return the message response
     */
    Optional<RestMessageResponse> requestReply(RestMessageRequest payload);

    /**
     * Publish a message to a topic
     * @param payload the message
     * @return the message response
     */
    Optional<RestMessageResponse> publish(RestMessageRequest payload);

    /**
     * Restart the messaging infrastructure.
     * @return the message response
     */
    Optional<RestMessageResponse> restartMessagingInfrastructure();
}
