package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.configuration.Configuration;

import java.util.Optional;

public interface IConfigurationService {
    public Optional<Configuration> allConfiguration();
}
