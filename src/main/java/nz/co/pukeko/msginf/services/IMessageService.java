package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.message.RestMessageRequest;
import nz.co.pukeko.msginf.models.message.RestMessageResponse;

import java.util.List;
import java.util.Optional;

public interface IMessageService {
    public Optional<RestMessageResponse> submit(RestMessageRequest payload);

    public List<RestMessageResponse> receiveMessages(String messagingSystem, String messageConnector, long timeout);

    public Optional<RestMessageResponse> requestReply(RestMessageRequest payload);
}
