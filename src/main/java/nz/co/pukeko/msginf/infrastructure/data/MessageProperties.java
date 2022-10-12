package nz.co.pukeko.msginf.infrastructure.data;

import java.util.HashMap;

/**
 * MessageProperties implements a set of named properties to associate with a message.
 * 
 * @author Stephen Denne
 */
public class MessageProperties<String> extends HashMap<String, String> {

    public MessageProperties(MessageProperties<String> configMessageProperties) {
        super(configMessageProperties);
    }

    public MessageProperties() {

    }
}
