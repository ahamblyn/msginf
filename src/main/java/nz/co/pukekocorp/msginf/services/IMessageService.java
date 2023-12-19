package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.message.RestMessageRequest;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;

import java.util.List;
import java.util.Optional;

public interface IMessageService {
    Optional<RestMessageResponse> submit(RestMessageRequest payload);

    List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout);

    Optional<RestMessageResponse> requestReply(RestMessageRequest payload);
}
