package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.models.message.MessageResponse;

import java.util.List;
import java.util.Optional;

public interface IMessageService {
    public Optional<MessageResponse> submit(String messageSystem, String messageConnector, String payload);

    public List<String> receiveMessages(String messagingSystem, String messageConnector, long timeout);

    public String requestReply(String messageSystem, String messageConnector, String payload, HeaderProperties<String,Object> headerProperties);
}
