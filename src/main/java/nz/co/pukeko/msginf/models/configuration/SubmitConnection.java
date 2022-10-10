package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SubmitConnection {
    private String submitQueueName;
    private String submitQueueConnFactoryName;
    private String requestType;
    private Integer messageTimeToLive;
    private List<MessageProperty> messageProperties;
}
