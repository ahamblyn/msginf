package nz.co.pukekocorp.msginf.infrastructure;

import nz.co.pukekocorp.msginf.infrastructure.data.TestStatisticsCollector;
import nz.co.pukekocorp.msginf.infrastructure.properties.TestConfiguration;
import nz.co.pukekocorp.msginf.infrastructure.properties.TestMessageInfrastructurePropertiesFileParser;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses( { TestStatisticsCollector.class, TestConfiguration.class, TestMessageInfrastructurePropertiesFileParser.class})
public class TestSuite {
}
