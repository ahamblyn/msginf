package nz.co.pukekocorp.msginf.client.adapter.activemq;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestSubmit.class, TestTextRequestTextReply.class, TestTextRequestBinaryReply.class,
        TestBinaryRequestTextReply.class, TestBinaryRequestBinaryReply.class, TestPublishSubscribeText.class,
        TestPublishSubscribeBinary.class})
public class TestSuite {
}
