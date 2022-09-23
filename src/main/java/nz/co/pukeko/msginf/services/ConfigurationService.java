package nz.co.pukeko.msginf.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.models.configuration.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
@Slf4j
public class ConfigurationService implements IConfigurationService {

    @Override
    public Optional<Configuration> allConfiguration() {
        try {
            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();
            //read json file and convert to customer object
            Resource resource = new ClassPathResource("msginf-config.json");
            File file = resource.getFile();
            Configuration configuration = objectMapper.readValue(file, Configuration.class);
            return Optional.of(configuration);
        } catch (Exception e) {
            log.error("All configuration exception", e);
        }
        return Optional.empty();
    }
}
