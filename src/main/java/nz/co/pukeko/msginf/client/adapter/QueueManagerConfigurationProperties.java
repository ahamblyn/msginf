package nz.co.pukeko.msginf.client.adapter;

/**
 * This class holds the QueueManager configuration properties
 * for each connector defined in the XML properties file.
 *
 * @param compressBinaryMessages Whether to compress binary messages or not.
 * @param requestReply           Whether the configuration is for request-reply or not.
 * @author Alisdair Hamblyn
 */

public record QueueManagerConfigurationProperties(boolean compressBinaryMessages, boolean requestReply) {

    /**
     * Constructs a QueueManagerConfigurationProperties object.
     *
     * @param compressBinaryMessages whether to compress binary messages or not.
     */
    public QueueManagerConfigurationProperties {
    }

    /**
     * Gets whether to compress binary messages or not.
     *
     * @return whether to compress binary messages or not.
     */
    @Override
    public boolean compressBinaryMessages() {
        return this.compressBinaryMessages;
    }

    /**
     * Gets whether configuration is request-reply or not.
     *
     * @return whether configuration is request-reply or not.
     */
    @Override
    public boolean requestReply() {
        return this.requestReply;
    }

    /**
     * Gets this object as a String.
     *
     * @return this object as a String.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Compress Binary Messages: ").append(compressBinaryMessages);
        sb.append("\nRequest-Reply: ").append(requestReply);
        return sb.toString();
    }
}
