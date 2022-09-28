package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.models.message.MessageResponse;
import nz.co.pukeko.msginf.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/msginf/message")
public class MessageController {

    private IMessageService messageService;

    public MessageController(@Autowired IMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(path = "/submit")
    public MessageResponse submit(@RequestBody String payload) {
        Optional<MessageResponse> messageResponse = messageService.submit(payload);
        return messageResponse.orElseGet(MessageResponse::new);
    }

}
