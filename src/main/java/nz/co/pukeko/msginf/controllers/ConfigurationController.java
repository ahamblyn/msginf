package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.models.configuration.Configuration;
import nz.co.pukeko.msginf.services.IConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/msginf/configuration")
public class ConfigurationController {

    private IConfigurationService configurationService;

    public ConfigurationController(@Autowired IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/all")
    public Configuration all() {
        Optional<Configuration> configuration = configurationService.allConfiguration();
        if (configuration.isPresent()) {
            return configuration.get();
        } else {
            return new Configuration();
        }
    }

}
