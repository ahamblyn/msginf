package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.infrastructure.properties.MessageInfrastructurePropertiesFileParser;
import nz.co.pukekocorp.msginf.models.configuration.Configuration;
import nz.co.pukekocorp.msginf.models.configuration.System;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The Configuration Service implementation.
 */
@Service
@Slf4j
public class ConfigurationService implements IConfigurationService {

    /**
     * Returns the configuration for all the messaging systems
     * @return the configuration for all the messaging systems
     */
    @Override
    public Optional<Configuration> allConfiguration() {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            return Optional.ofNullable(parser.getConfiguration());
        } catch (Exception e) {
            log.error("Unable to retrieve the configuration", e);
        }
        return Optional.empty();
    }

    /**
     * Returns the configuration for a messaging systems
     * @param name the name of the messaging system
     * @return the System configuration
     */
    @Override
    public Optional<System> getSystem(String name) {
        try {
            MessageInfrastructurePropertiesFileParser parser = new MessageInfrastructurePropertiesFileParser();
            return parser.getSystem(name);
        } catch (Exception e) {
            log.error("Unable to retrieve the configuration", e);
        }
        return Optional.empty();
    }
}
