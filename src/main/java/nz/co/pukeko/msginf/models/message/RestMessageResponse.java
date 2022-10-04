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
    private Long responseTimeInMilliseconds;

    public RestMessageResponse(String message, String transactionId) {
        this.message = message;
        this.transactionId = transactionId;
        this.responseTimeInMilliseconds = 0L;
    }
}
