package nz.co.pukeko.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukeko.msginf.models.configuration.Configuration;
import nz.co.pukeko.msginf.models.configuration.System;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ConfigurationService implements IConfigurationService {

    @Override
    public Optional<Configuration> allConfiguration() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            return Optional.of(parser.getConfiguration());
        } catch (Exception e) {
            log.error("Unable to retrieve the configuration", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<System> getSystem(String name) {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser(name);
            return Optional.of(parser.getCurrentSystem());
        } catch (Exception e) {
            log.error("Unable to retrieve the configuration", e);
        }
        return Optional.empty();
    }
}
