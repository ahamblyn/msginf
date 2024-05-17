package nz.co.pukekocorp.msginf.infrastructure.properties;

import nz.co.pukekocorp.msginf.models.configuration.RequestType;

import java.util.HashMap;
import java.util.Map;

public class ExpectedConnectorDataUtil {
    public static final Map<String, ExpectedConnectorData> EXPECTED_CONNECTOR_DATA_MAP;

    static {
        EXPECTED_CONNECTOR_DATA_MAP = new HashMap<>();
        ExpectedConnectorData activemqSubmitTextExpectedData = new ExpectedConnectorData(false,
                "TestQueue", "", "", "",
                "QueueConnectionFactory", "", RequestType.TEXT, 0,
                20000, false, false);
        EXPECTED_CONNECTOR_DATA_MAP.put("submit_text", activemqSubmitTextExpectedData);

        ExpectedConnectorData activemqRequestReplyTextExpectedData = new ExpectedConnectorData(false,
                "", "RequestQueue", "ReplyQueue", "",
                "QueueConnectionFactory", "", RequestType.TEXT, 0,
                20000, true, false);
        EXPECTED_CONNECTOR_DATA_MAP.put("text_request_text_reply", activemqRequestReplyTextExpectedData);

        ExpectedConnectorData activemqPubSubExpectedData = new ExpectedConnectorData(false,
                "", "", "", "TestTopic",
                "", "TopicConnectionFactory", RequestType.TEXT, 0,
                20000, true, false);
        EXPECTED_CONNECTOR_DATA_MAP.put("pubsub_text", activemqPubSubExpectedData);
    }

}
