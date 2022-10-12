package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RestMessageRequestProperty {
    private String name;
    private String value;
}
