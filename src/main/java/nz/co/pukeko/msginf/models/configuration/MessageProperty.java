package nz.co.pukeko.msginf.models.configuration;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MessageProperty {
    private String name;
    private String value;
}
