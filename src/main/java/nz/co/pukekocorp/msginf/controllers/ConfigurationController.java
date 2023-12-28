package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import nz.co.pukekocorp.msginf.models.configuration.Configuration;
import nz.co.pukekocorp.msginf.models.configuration.System;
import nz.co.pukekocorp.msginf.services.IConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * REST Controller to return configuration information.
 */
@Tag(name = "configuration", description = "Configuration API")
@RestController
@RequestMapping("/v1/configuration")
public class ConfigurationController {

    private final IConfigurationService configurationService;

    /**
     * Construct the Configuration Controller
     * @param configurationService the Configuration Service
     */
    public ConfigurationController(@Autowired IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Returns the configuration for all the messaging systems
     * @return the configuration for all the messaging systems
     */
    @Operation(
            summary = "Retrieve all the configuration",
            description = "Retrieve all the configuration",
            tags = {"configuration"})
    @GetMapping("/all")
    public Configuration all() {
        Optional<Configuration> configuration = configurationService.allConfiguration();
        return configuration.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No System Configuration found"));
    }

    /**
     * Returns the configuration for a messaging systems
     * @param name the name of the messaging system
     * @return the System configuration
     */
    @Operation(
            summary = "Retrieve the configuration for a messaging system",
            description = "Retrieve the configuration for a messaging system",
            tags = {"configuration"})
    @GetMapping("/system/{systemName}")
    public System system(@Parameter(description = "The messaging system name") @PathVariable("systemName") String name) {
        Optional<System> system = configurationService.getSystem(name);
        return system.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "System Configuration " + name + " not found"));
    }

}
