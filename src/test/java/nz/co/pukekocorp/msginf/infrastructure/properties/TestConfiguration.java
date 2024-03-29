package nz.co.pukekocorp.msginf.infrastructure.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.models.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class TestConfiguration {

    @Test
    public void configurationJson() throws Exception {
        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        //read json file and convert to customer object
        Resource resource = new ClassPathResource("msginf-config.json");
        File file = resource.getFile();
        Configuration configuration = objectMapper.readValue(file, Configuration.class);
        assertNotNull(configuration);
    }
}
