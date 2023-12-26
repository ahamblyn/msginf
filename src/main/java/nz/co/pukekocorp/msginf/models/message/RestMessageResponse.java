package nz.co.pukekocorp.msginf.models.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "The Rest Message Response model")
public class RestMessageResponse {
    @Schema(description = "The response message")
    private String responseMessage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "The text message response")
    private String textMessage;
    // base64 encoded
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "The binary message response (base64 encoded)")
    private String binaryMessage;
    @Schema(description = "The transaction id")
    private String transactionId;
    @Schema(description = "The transaction status")
    private TransactionStatus transactionStatus;
    @Schema(description = "The response time in ms")
    private Long responseTimeInMilliseconds;

    public RestMessageResponse(String responseMessage, String transactionId, TransactionStatus transactionStatus) {
        this.responseMessage = responseMessage;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.responseTimeInMilliseconds = 0L;
    }

    public RestMessageResponse(String responseMessage, String transactionId, TransactionStatus transactionStatus, long responseTimeInMilliseconds) {
        this.responseMessage = responseMessage;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.responseTimeInMilliseconds = responseTimeInMilliseconds;
    }
}
