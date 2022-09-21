package nz.co.pukeko.msginf.client.adapter;

/**
 * This class holds the QueueManager configuration properties
 * for each connector defined in the XML properties file.
 *
 * @param mimetype               The mime type.
 * @param submitSchema           The submit schema.
 * @param requestSchema          The request schema.
 * @param replySchema            The reply schema.
 * @param validateSubmit         Whether to validate the submit message using the submit schema or not.
 * @param validateRequest        Whether to validate the request message using the request schema or not.
 * @param validateReply          Whether to validate the reply message using the reply schema or not.
 * @param validateError          Whether to put any validation errors onto the dead letter queue or not.
 * @param compressBinaryMessages Whether to compress binary messages or not.
 * @param requestReply           Whether the configuration is for request-reply or not.
 * @author Alisdair Hamblyn
 */

public record QueueManagerConfigurationProperties(String mimetype, String submitSchema, String requestSchema,
                                                  String replySchema, boolean validateSubmit, boolean validateRequest,
                                                  boolean validateReply, boolean validateError,
                                                  boolean compressBinaryMessages, boolean requestReply) {

    /**
     * Constructs a QueueManagerConfigurationProperties object.
     *
     * @param mimetype               the mime type.
     * @param submitSchema           the submit schema.
     * @param requestSchema          the request schema.
     * @param replySchema            the reply schema
     * @param validateSubmit         whether to validate the submit message using the submit schema or not.
     * @param validateRequest        whether to validate the request message using the request schema or not.
     * @param validateReply          whether to validate the reply message using the reply schema or not.
     * @param validateError          whether to put any validation errors onto the dead letter queue or not.
     * @param compressBinaryMessages whether to compress binary messages or not.
     */
    public QueueManagerConfigurationProperties {
        if (mimetype == null) {
            mimetype = "";
        }
        if (submitSchema == null) {
            submitSchema = "";
        }
        if (requestSchema == null) {
            requestSchema = "";
        }
        if (replySchema == null) {
            replySchema = "";
        }
    }

    /**
     * Gets the mime type.
     *
     * @return the mime type.
     */
    @Override
    public String mimetype() {
        return this.mimetype;
    }

    /**
     * Gets the submit schema.
     *
     * @return the submit schema.
     */
    @Override
    public String submitSchema() {
        return this.submitSchema;
    }

    /**
     * Gets the request schema.
     *
     * @return the request schema.
     */
    @Override
    public String requestSchema() {
        return this.requestSchema;
    }

    /**
     * Gets the reply schema.
     *
     * @return the reply schema.
     */
    @Override
    public String replySchema() {
        return this.replySchema;
    }

    /**
     * Gets whether to validate the submit message or not.
     *
     * @return whether to validate the submit message or not.
     */
    @Override
    public boolean validateSubmit() {
        return this.validateSubmit;
    }

    /**
     * Gets whether to validate the request message or not.
     *
     * @return whether to validate the request message or not.
     */
    @Override
    public boolean validateRequest() {
        return this.validateRequest;
    }

    /**
     * Gets whether to validate the reply message or not.
     *
     * @return whether to validate the reply message or not.
     */
    @Override
    public boolean validateReply() {
        return this.validateReply;
    }

    /**
     * Gets whether to put validation errors onto the dead letter queue or not.
     *
     * @return whether to put validation errors onto the dead letter queue or not.
     */
    @Override
    public boolean validateError() {
        return this.validateError;
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
        sb.append("Mime Type: ").append(mimetype);
        sb.append("\nSubmit Schema: ").append(submitSchema);
        sb.append("\nRequest Schema: ").append(requestSchema);
        sb.append("\nReply Schema: ").append(replySchema);
        sb.append("\nValidate Submit: ").append(validateSubmit);
        sb.append("\nValidate Request: ").append(validateRequest);
        sb.append("\nValidate Reply: ").append(validateReply);
        sb.append("\nValidate Error: ").append(validateError);
        sb.append("\nCompress Binary Messages: ").append(compressBinaryMessages);
        sb.append("\nRequest-Reply: ").append(requestReply);
        return sb.toString();
    }
}
