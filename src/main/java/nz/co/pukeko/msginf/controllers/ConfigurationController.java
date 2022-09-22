package nz.co.pukeko.msginf.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/msginf/configuration")
public class ConfigurationController {

    @GetMapping("/all")
    public String all() {
        // TODO return JSON
        return "All configuration...";
    }

}
