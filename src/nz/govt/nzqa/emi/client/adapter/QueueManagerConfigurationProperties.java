package nz.govt.nzqa.emi.client.adapter;

/**
 * This class holds the QueueManager configuration properties 
 * for each connector defined in the XML properties file.
 * 
 * @author Alisdair Hamblyn
 */

public class QueueManagerConfigurationProperties {
	
	/**
	 * The mime type.
	 */
    private String mimetype;
    
    /**
     * The submit schema.
     */
    private String submitSchema;
    
    /**
     * The request schema.
     */
    private String requestSchema;
    
    /**
     * The reply schema.
     */
    private String replySchema;
    
    /**
     * Whether to validate the submit message using the submit schema or not.
     */
    private boolean validateSubmit = false;
    
    /**
     * Whether to validate the request message using the request schema or not.
     */
    private boolean validateRequest = false;

    /**
     * Whether to validate the reply message using the reply schema or not.
     */
    private boolean validateReply = false;

    /**
     * Whether to put any validation errors onto the dead letter queue or not.
     */
    private boolean validateError = false;

    /**
     * Whether to compress binary messages or not.
     */
    private boolean compressBinaryMessages = false;
    
    /**
     * Whether the configuration is for request-reply or not.
     */
    private boolean requestReply = false;

    /**
     * The source application name.
     */
    private String sourceName;

    /**
     * The destination application name.
     */
    private String destinationName;

    /**
     * Whether to use a SOAP envelope around the message or not.
     */
    private boolean useSOAPEnvelope;

    /**
     * Constructs a QueueManagerConfigurationProperties object.
     * @param mimetype the mime type.
     * @param submitSchema the submit schema.
     * @param requestSchema the request schema.
     * @param replySchema the reply schema
     * @param validateSubmit whether to validate the submit message using the submit schema or not.
     * @param validateRequest whether to validate the request message using the request schema or not.
     * @param validateReply whether to validate the reply message using the reply schema or not.
     * @param validateError whether to put any validation errors onto the dead letter queue or not.
     * @param compressBinaryMessages whether to compress binary messages or not.
     */
    public QueueManagerConfigurationProperties(String mimetype, String submitSchema, String requestSchema, String replySchema, boolean validateSubmit, boolean validateRequest, boolean validateReply, boolean validateError, boolean compressBinaryMessages, boolean requestReply, String sourceName, String destinationName, boolean useSOAPEnvelope) {
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
        this.mimetype = mimetype;
        this.submitSchema = submitSchema;
        this.requestSchema = requestSchema;
        this.replySchema = replySchema;
        this.validateSubmit = validateSubmit;
        this.validateRequest = validateRequest;
        this.validateReply = validateReply;
        this.validateError = validateError;
        this.compressBinaryMessages = compressBinaryMessages;
        this.requestReply = requestReply;
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.useSOAPEnvelope = useSOAPEnvelope;
    }

    /**
     * Gets the mime type.
     * @return the mime type.
     */
    public String getMimetype() {
        return this.mimetype;
    }

    /**
     * Gets the submit schema.
     * @return the submit schema.
     */
    public String getSubmitSchema() {
        return this.submitSchema;
    }

    /**
     * Gets the request schema.
     * @return the request schema.
     */
    public String getRequestSchema() {
        return this.requestSchema;
    }

    /**
     * Gets the reply schema.
     * @return the reply schema.
     */
    public String getReplySchema() {
        return this.replySchema;
    }

    /**
     * Gets whether to validate the submit message or not.
     * @return whether to validate the submit message or not.
     */
    public boolean isValidateSubmit() {
        return this.validateSubmit;
    }

    /**
     * Gets whether to validate the request message or not.
     * @return whether to validate the request message or not.
     */
    public boolean isValidateRequest() {
        return this.validateRequest;
    }

    /**
     * Gets whether to validate the reply message or not.
     * @return whether to validate the reply message or not.
     */
    public boolean isValidateReply() {
        return this.validateReply;
    }

    /**
     * Gets whether to put validation errors onto the dead letter queue or not.
     * @return whether to put validation errors onto the dead letter queue or not.
     */
    public boolean isValidateError() {
        return this.validateError;
    }

    /**
     * Gets whether to compress binary messages or not.
     * @return whether to compress binary messages or not.
     */
    public boolean isCompressBinaryMessages() {
        return this.compressBinaryMessages;
    }

    /**
     * Gets whether configuration is request-reply or not.
     * @return whether configuration is request-reply or not.
     */
    public boolean isRequestReply() {
        return this.requestReply;
    }
    
    /**
     * The SOAP destination name.
     * @return the SOAP destination name.
     */
    public String getDestinationName() {
		return destinationName;
	}

    /**
     * The SOAP source name.
     * @return the SOAP source name.
     */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * Gets whether to use a SOAP envelope around the message or not.
	 * @return whether to use a SOAP envelope around the message or not.
	 */
	public boolean isUseSOAPEnvelope() {
		return useSOAPEnvelope;
	}

	/**
     * Gets this object as a String.
     * @return this object as a String.
     */
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Mime Type: " + mimetype);
    	sb.append("\nSubmit Schema: " + submitSchema);
    	sb.append("\nRequest Schema: " + requestSchema);
    	sb.append("\nReply Schema: " + replySchema);
    	sb.append("\nValidate Submit: " + validateSubmit);
    	sb.append("\nValidate Request: " + validateRequest);
    	sb.append("\nValidate Reply: " + validateReply);
    	sb.append("\nValidate Error: " + validateError);
    	sb.append("\nCompress Binary Messages: " + compressBinaryMessages);
    	sb.append("\nRequest-Reply: " + requestReply);
    	sb.append("\nSource Name: " + sourceName);
    	sb.append("\nDestination Name: " + destinationName);
    	sb.append("\nUse SOAP Envelope: " + useSOAPEnvelope);
        return sb.toString();
    }
}
