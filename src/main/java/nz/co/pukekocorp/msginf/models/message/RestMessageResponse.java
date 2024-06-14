package nz.co.pukekocorp.msginf.models.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import nz.co.pukekocorp.msginf.models.error.ValidationErrors;

/**
 * REST message response
 * @param responseMessage The response message
 * @param textMessage The text message response
 * @param binaryMessage The binary message response (base64 encoded)
 * @param transactionId The transaction id
 * @param transactionStatus The transaction status
 * @param responseTimeInMilliseconds The response time in ms
 * @param validationErrors The validation errors
 */
@Schema(description = "The Rest Message Response model")
public record RestMessageResponse(@Schema(description = "The response message") String responseMessage,
                                  @JsonInclude(JsonInclude.Include.NON_NULL) @Schema(description = "The text message response") String textMessage,
                                  @JsonInclude(JsonInclude.Include.NON_NULL) @Schema(description = "The binary message response (base64 encoded)") String binaryMessage,
                                  @Schema(description = "The transaction id") String transactionId,
                                  @Schema(description = "The transaction status") TransactionStatus transactionStatus,
                                  @Schema(description = "The response time in ms") Long responseTimeInMilliseconds,
                                  @Schema(description = "The validation errors") ValidationErrors validationErrors) {


    /**
     * RestMessageResponse constructor
     * @param responseMessage Response message
     * @param transactionId Transaction Id
     * @param transactionStatus Transaction status
     */
    public RestMessageResponse(String responseMessage, String transactionId, TransactionStatus transactionStatus) {
        this(responseMessage, null, null, transactionId, transactionStatus, 0L, null);
    }

    /**
     * RestMessageResponse constructor
     * @param responseMessage Response message
     * @param transactionId Transaction Id
     * @param transactionStatus Transaction status
     * @param responseTimeInMilliseconds Response time
     */
    public RestMessageResponse(String responseMessage, String transactionId, TransactionStatus transactionStatus, long responseTimeInMilliseconds) {
        this(responseMessage, null, null, transactionId, transactionStatus, responseTimeInMilliseconds, null);
    }
}
