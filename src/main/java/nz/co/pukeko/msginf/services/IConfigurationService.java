package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.configuration.Configuration;
import nz.co.pukeko.msginf.models.configuration.System;

import java.util.Optional;

public interface IConfigurationService {
    public Optional<Configuration> allConfiguration();

    public Optional<System> getSystem(String name);
}
