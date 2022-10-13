package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.configuration.Configuration;
import nz.co.pukeko.msginf.models.configuration.System;

import java.util.Optional;

public interface IConfigurationService {
    Optional<Configuration> allConfiguration();

    Optional<System> getSystem(String name);
}
