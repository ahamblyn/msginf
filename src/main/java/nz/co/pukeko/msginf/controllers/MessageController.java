package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/message")
public class MessageController {

    private IMessageService messageService;

    public MessageController(@Autowired IMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(path = "/submit")
    public MessageResponse submit(@RequestHeader(name="x-message-system") String messageSystem,
                                  @RequestHeader(name="x-message-connector") String messageConnector,
                                  @RequestBody String payload) {
        Optional<MessageResponse> messageResponse = messageService.submit(messageSystem, messageConnector, payload);
        return messageResponse.orElseGet(MessageResponse::new);
    }

    @GetMapping("/receive")
    public List<String> receiveMessages(@RequestHeader(name="x-message-system") String messageSystem,
                                        @RequestHeader(name="x-message-connector") String messageConnector,
                                        @RequestHeader(name="x-timeout") Long timeout) {
        return messageService.receiveMessages(messageSystem, messageConnector, timeout);
    }

    @PostMapping(path = "/request")
    public String request(@RequestHeader(name="x-message-system") String messageSystem,
                                  @RequestHeader(name="x-message-connector") String messageConnector,
                                  @RequestBody String payload) {
        HeaderProperties<String,Object> headerProperties = new HeaderProperties<>();
        headerProperties.put("testname", "reply");
        return messageService.requestReply(messageSystem, messageConnector, payload, headerProperties);
    }
}
