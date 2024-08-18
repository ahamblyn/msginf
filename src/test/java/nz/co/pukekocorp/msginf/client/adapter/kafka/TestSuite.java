package nz.co.pukekocorp.msginf.client.adapter.kafka;

import nz.co.pukekocorp.msginf.client.connector.DestinationChannelFactoryTest;
import nz.co.pukekocorp.msginf.client.connector.MessageFactoryTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestPublishSubscribeText.class, TestPublishSubscribeBinary.class, MessageFactoryTest.class,
        DestinationChannelFactoryTest.class})
public class TestSuite {
}
