package nz.co.pukeko.msginf.models.message;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestMessageResponse {
    private String message;
    private String transactionId;
    private TransactionStatus transactionStatus;
    private Long responseTimeInMilliseconds;

    public RestMessageResponse(String message, String transactionId, TransactionStatus transactionStatus) {
        this.message = message;
        this.transactionId = transactionId;
        this.transactionStatus = transactionStatus;
        this.responseTimeInMilliseconds = 0L;
    }
}
