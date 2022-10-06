package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;
import nz.co.pukeko.msginf.models.message.RestMessageResponse;
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
    public RestMessageResponse submit(@RequestHeader(name="x-message-system") String messageSystem,
                                      @RequestHeader(name="x-message-connector") String messageConnector,
                                      @RequestBody String payload) {
        Optional<RestMessageResponse> messageResponse = messageService.submit(messageSystem, messageConnector, payload);
        return messageResponse.orElseGet(RestMessageResponse::new);
    }

    @GetMapping("/receive")
    public List<String> receiveMessages(@RequestHeader(name="x-message-system") String messageSystem,
                                        @RequestHeader(name="x-message-connector") String messageConnector,
                                        @RequestHeader(name="x-timeout") Long timeout) {
        return messageService.receiveMessages(messageSystem, messageConnector, timeout);
    }

    @PostMapping(path = "/request")
    public RestMessageResponse request(@RequestHeader(name="x-message-system") String messageSystem,
                                  @RequestHeader(name="x-message-connector") String messageConnector,
                                  @RequestBody String payload) {
        // TODO add header properties to request header and make non-mandatory
        HeaderProperties<String,Object> headerProperties = new HeaderProperties<>();
        headerProperties.put("testname", "reply");
        Optional<RestMessageResponse> messageResponse = messageService.requestReply(messageSystem, messageConnector, payload, headerProperties);
        return messageResponse.orElseGet(RestMessageResponse::new);
    }
}
