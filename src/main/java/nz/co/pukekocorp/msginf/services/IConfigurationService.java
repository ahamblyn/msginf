package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.configuration.Configuration;
import nz.co.pukekocorp.msginf.models.configuration.System;

import java.util.Optional;

/**
 * The Configuration Service interface.
 */
public interface IConfigurationService {

    /**
     * Returns the configuration for all the messaging systems
     * @return the configuration for all the messaging systems
     */
    Optional<Configuration> allConfiguration();

    /**
     * Returns the configuration for a messaging systems
     * @param name the name of the messaging system
     * @return the System configuration
     */
    Optional<System> getSystem(String name);
}
