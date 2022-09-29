package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.models.configuration.Configuration;
import nz.co.pukeko.msginf.models.configuration.System;
import nz.co.pukeko.msginf.services.IConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/configuration")
public class ConfigurationController {

    private IConfigurationService configurationService;

    public ConfigurationController(@Autowired IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/all")
    public Configuration all() {
        Optional<Configuration> configuration = configurationService.allConfiguration();
        return configuration.orElseGet(Configuration::new);
    }

    @GetMapping("/system/{systemName}")
    public System system(@PathVariable("systemName") String name) {
        Optional<System> system = configurationService.getSystem(name);
        return system.orElseGet(System::new);
    }

}
