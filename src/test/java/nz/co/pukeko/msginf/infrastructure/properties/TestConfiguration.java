package nz.co.pukeko.msginf.infrastructure.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.models.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;

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
        //print customer details
        log.info(configuration.toString());
    }
}
