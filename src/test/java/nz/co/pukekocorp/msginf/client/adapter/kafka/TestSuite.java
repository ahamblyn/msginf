package nz.co.pukekocorp.msginf.client.adapter.kafka;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestPublishSubscribeText.class, TestPublishSubscribeBinary.class})
public class TestSuite {
}
