package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubmitConnection {
    private String submitQueueName;
    private String submitQueueConnFactoryName;
    private String messageClassName;
    private String requesterClassName;
    private Integer messageTimeToLive;
    private Integer replyWaitTime;
}
