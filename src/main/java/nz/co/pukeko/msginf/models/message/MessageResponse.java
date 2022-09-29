package nz.co.pukeko.msginf.models.message;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String message;
    private String transactionId;
    private Long responseTimeInMilliseconds;
}
