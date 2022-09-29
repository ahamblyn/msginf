package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.message.MessageResponse;

import java.util.Optional;

public interface IMessageService {
    public Optional<MessageResponse> submit(String messageSystem, String messageConnector, String payload);
}
