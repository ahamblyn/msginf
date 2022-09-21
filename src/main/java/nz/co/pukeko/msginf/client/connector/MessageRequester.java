package nz.co.pukeko.msginf.client.connector;

import javax.jms.Message;

import nz.co.pukeko.msginf.infrastructure.exception.MessageRequesterException;

/**
 * This message requester interface. 
 * @author alisdairh
 */
public interface MessageRequester {
    /**
     * Handles the request-reply.
     * @param message the request message
     * @return the reply message.
     * @throws MessageRequesterException Message requester exception
     */
	Message request(Message message) throws MessageRequesterException;
	
    /**
     * Closes the MessageRequester.
     * @throws MessageRequesterException Message requester exception
     */
    void close() throws MessageRequesterException;
}

