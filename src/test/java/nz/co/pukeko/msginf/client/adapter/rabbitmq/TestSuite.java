package nz.co.pukeko.msginf.client.adapter.rabbitmq;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestSubmit.class, TestTextRequestTextReply.class} )
public class TestSuite {
}
