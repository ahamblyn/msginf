package nz.co.pukekocorp.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class RequestReplyConnection {
    private String requestQueueName;
    private String replyQueueName;
    private String requestQueueConnFactoryName;
    private String requestType;
    private Integer messageTimeToLive;
    private Integer replyWaitTime;
    private Boolean useMessageSelector;
    private List<MessageProperty> messageProperties;
}
