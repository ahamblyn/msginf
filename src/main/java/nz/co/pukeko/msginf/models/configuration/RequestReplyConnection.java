package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestReplyConnection {
    private String requestQueueName;
    private String replyQueueName;
    private String requestQueueConnFactoryName;
    private String messageClassName;
    private String requesterClassName;
    private Integer messageTimeToLive;
    private Integer replyWaitTime;
}
