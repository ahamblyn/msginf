package nz.co.pukeko.msginf;

import nz.co.pukeko.msginf.client.adapter.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestSubmit.class, TestTextRequestTextReply.class, TestTextRequestBinaryReply.class,
        TestBinaryRequestTextReply.class, TestBinaryRequestBinaryReply.class} )
public class TestSuite {
}
