package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nz.co.pukekocorp.msginf.infrastructure.exception.MessageException;
import nz.co.pukekocorp.msginf.models.error.ValidationErrors;
import nz.co.pukekocorp.msginf.models.message.RestMessageRequest;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.models.message.TransactionStatus;
import nz.co.pukekocorp.msginf.models.status.Status;
import nz.co.pukekocorp.msginf.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller to submit and receive messages.
 */
@Tag(name = "message", description = "Message API")
@RestController
@RequestMapping("/v1/message")
public class MessageController {

    private final IMessageService messageService;

    /**
     * Construct the Message Controller
     * @param messageService the Message Service
     */
    public MessageController(@Autowired IMessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Submit an asynchronous message
     * @param payload the message
     * @return the message response
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Submit a message asynchronously",
            description = "Submit a message asynchronously",
            tags = {"message"})
    @PostMapping(path = "/submit", consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RestMessageResponse> submit(@Valid @Parameter(description = "The message") @RequestBody RestMessageRequest payload) {
        try {
            Optional<RestMessageResponse> messageResponse = messageService.submit(payload);
            return ResponseEntity.of(messageResponse);
        } catch (MessageException e) {
            return createBadRequestErrorResponse(e.getMessage());
        }
    }

    /**
     * Receive (read) messages off a queue
     * @param messageSystem the messaging system
     * @param messageConnector the connector to use
     * @param timeout the timeout in ms to wait
     * @return the messages read
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Receive (read) messages off a queue",
            description = "Receive (read) messages off a queue",
            tags = {"message"})
    @GetMapping("/receive")
    public ResponseEntity<List<RestMessageResponse>> receiveMessages(@Parameter(description = "The messaging system") @RequestHeader(name="x-message-system") String messageSystem,
                                                                     @Parameter(description = "The message connector") @RequestHeader(name="x-message-connector") String messageConnector,
                                                                     @Parameter(description = "The message timeout (ms)") @RequestHeader(name="x-timeout") Long timeout) {
        try {
            return ResponseEntity.of(Optional.of(messageService.receiveMessages(messageSystem, messageConnector, timeout)));
        } catch (MessageException e) {
            return ResponseEntity.of(Optional.of(Collections.singletonList(createBadRequestErrorResponse(e.getMessage()).getBody())));
        }
    }

    /**
     * Submit a synchronous message
     * @param payload the message
     * @return the message response
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Submit a message synchronously",
            description = "Submit a message synchronously",
            tags = {"message"})
    @PostMapping(path = "/request", consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RestMessageResponse> request(@Valid @Parameter(description = "The message") @RequestBody RestMessageRequest payload) {
        try {
            Optional<RestMessageResponse> messageResponse = messageService.requestReply(payload);
            return ResponseEntity.of(messageResponse);
        } catch (MessageException e) {
            return createBadRequestErrorResponse(e.getMessage());
        }
    }

    /**
     * Publish a message
     * @param payload the message
     * @return the message response
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Publish a message to a topic",
            description = "Publish a message to a topic",
            tags = {"message"})
    @PostMapping(path = "/publish", consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RestMessageResponse> publish(@Valid @Parameter(description = "The message") @RequestBody RestMessageRequest payload) {
        try {
            Optional<RestMessageResponse> messageResponse = messageService.publish(payload);
            return ResponseEntity.of(messageResponse);
        } catch (MessageException e) {
            return createBadRequestErrorResponse(e.getMessage());
        }
    }

    /**
     * Restart the messaging infrastructure.
     * @return the message response
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Restart the messaging infrastructure",
            description = "Restart the messaging infrastructure",
            tags = {"message"})
    @GetMapping("/restart")
    public ResponseEntity<RestMessageResponse> restartMessagingInfrastructure() {
        Optional<RestMessageResponse> messageResponse = messageService.restartMessagingInfrastructure();
        return ResponseEntity.of(messageResponse);
    }

    /**
     * Return the status for the messaging systems.
     * @return the status for the messaging systems.
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Return the status for the messaging systems",
            description = "Return the status for the messaging systems",
            tags = {"message"})
    @GetMapping("/status")
    public ResponseEntity<Status> getSystemStatus() {
        Status status = messageService.getSystemStatus();
        return ResponseEntity.of(Optional.of(status));
    }

    private ResponseEntity<RestMessageResponse> createBadRequestErrorResponse(String errorMessage) {
        String transactionId = UUID.randomUUID().toString();
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.setValidationErrors(List.of(errorMessage));
        RestMessageResponse messageResponse = new RestMessageResponse("Bad Request", null, null, transactionId, TransactionStatus.ERROR, 0L, validationErrors);
        return new ResponseEntity<>(messageResponse, HttpStatus.BAD_REQUEST);
    }
}
