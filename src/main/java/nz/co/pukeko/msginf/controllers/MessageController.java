package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.models.message.RestMessageRequest;
import nz.co.pukeko.msginf.models.message.RestMessageResponse;
import nz.co.pukeko.msginf.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/message")
public class MessageController {

    private final IMessageService messageService;

    public MessageController(@Autowired IMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(path = "/submit", consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RestMessageResponse> submit(@RequestBody RestMessageRequest payload) {
        Optional<RestMessageResponse> messageResponse = messageService.submit(payload);
        return ResponseEntity.of(Optional.of(messageResponse.orElseGet(RestMessageResponse::new)));
    }

    @GetMapping("/receive")
    public ResponseEntity<List<RestMessageResponse>> receiveMessages(@RequestHeader(name="x-message-system") String messageSystem,
                                        @RequestHeader(name="x-message-connector") String messageConnector,
                                        @RequestHeader(name="x-timeout") Long timeout) {
        return ResponseEntity.of(Optional.of(messageService.receiveMessages(messageSystem, messageConnector, timeout)));
    }

    @PostMapping(path = "/request", consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RestMessageResponse> request(@RequestBody RestMessageRequest payload) {
        Optional<RestMessageResponse> messageResponse = messageService.requestReply(payload);
        return ResponseEntity.of(Optional.of(messageResponse.orElseGet(RestMessageResponse::new)));
    }
}
