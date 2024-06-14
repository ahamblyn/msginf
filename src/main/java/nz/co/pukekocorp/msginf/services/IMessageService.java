package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.message.RestMessageRequest;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.models.status.Status;

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
     * @throws MessageException the message exception.
     */
    Optional<RestMessageResponse> submit(RestMessageRequest payload) throws MessageException;

    /**
     * Receive (read) messages off a queue
     * @param messagingSystem the messaging system
     * @param messageConnector the connector to use
     * @param timeout the timeout in ms to wait
     * @return the messages read
     * @throws MessageException the message exception.
     */
    List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout) throws MessageException;

    /**
     * Submit a synchronous message
     * @param payload the message
     * @return the message response
     * @throws MessageException the message exception.
     */
    Optional<RestMessageResponse> requestReply(RestMessageRequest payload) throws MessageException;

    /**
     * Publish a message to a topic
     * @param payload the message
     * @return the message response
     * @throws MessageException the message exception.
     */
    Optional<RestMessageResponse> publish(RestMessageRequest payload) throws MessageException;

    /**
     * Restart the messaging infrastructure.
     * @return the message response
     */
    Optional<RestMessageResponse> restartMessagingInfrastructure();

    /**
     * Return the status for the messaging systems.
     * @return the status for the messaging systems.
     */
    Status getSystemStatus();
}
