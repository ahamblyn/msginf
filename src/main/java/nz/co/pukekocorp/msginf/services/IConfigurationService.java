package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.configuration.Configuration;
import nz.co.pukekocorp.msginf.models.configuration.System;

import java.util.Optional;

public interface IConfigurationService {
    Optional<Configuration> allConfiguration();

    Optional<System> getSystem(String name);
}
