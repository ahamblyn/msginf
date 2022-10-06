package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.models.message.RestMessageResponse;

import java.util.List;
import java.util.Optional;

public interface IMessageService {
    public Optional<RestMessageResponse> submit(String messageSystem, String messageConnector, String payload);

    public List<String> receiveMessages(String messagingSystem, String messageConnector, long timeout);

    public Optional<RestMessageResponse> requestReply(String messageSystem, String messageConnector, String payload,
                                                      HeaderProperties<String,Object> headerProperties);
}
