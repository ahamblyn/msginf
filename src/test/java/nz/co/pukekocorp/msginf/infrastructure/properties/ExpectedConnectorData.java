package nz.co.pukekocorp.msginf.infrastructure.properties;

import nz.co.pukekocorp.msginf.models.configuration.RequestType;

public record ExpectedConnectorData(boolean compressBinaryMessages, String submitQueueName, String requestQueueName,
                                    String replyQueueName, String publishSubscribeTopicName, String queueConnFactoryName,
                                    String topicConnFactoryName, RequestType requestType, int messageTimeToLive,
                                    int replyWaitTime, boolean useMessageSelector, boolean useDurableSubscriber) {
}
