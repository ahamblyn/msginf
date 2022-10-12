package nz.co.pukeko.msginf.models.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestMessageResponse {
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String textMessage;
    // base64 encoded
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String binaryMessage;
    private String transactionId;
    private TransactionStatus transactionStatus;
    private Long responseTimeInMilliseconds;

    public RestMessageResponse(String message, String transactionId, TransactionStatus transactionStatus) {
        this.message = message;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.responseTimeInMilliseconds = 0L;
    }

    public RestMessageResponse(String message, String transactionId, TransactionStatus transactionStatus, long responseTimeInMilliseconds) {
        this.message = message;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.responseTimeInMilliseconds = responseTimeInMilliseconds;
    }
}
