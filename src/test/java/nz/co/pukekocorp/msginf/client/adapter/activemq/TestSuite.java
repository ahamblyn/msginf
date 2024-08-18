package nz.co.pukekocorp.msginf.client.adapter.activemq;

import nz.co.pukekocorp.msginf.client.connector.DestinationChannelFactoryTest;
import nz.co.pukekocorp.msginf.client.connector.MessageFactoryTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestSubmit.class, TestTextRequestTextReply.class, TestTextRequestBinaryReply.class,
        TestBinaryRequestTextReply.class, TestBinaryRequestBinaryReply.class, TestPublishSubscribeText.class,
        TestPublishSubscribeBinary.class, MessageFactoryTest.class, DestinationChannelFactoryTest.class})
public class TestSuite {
}
