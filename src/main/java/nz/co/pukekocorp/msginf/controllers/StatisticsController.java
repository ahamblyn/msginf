package nz.co.pukekocorp.msginf.controllers;

import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.services.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    public StatisticsController(@Autowired IStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/all")
    public String all() {
        return statisticsService.allStatistics();
    }

    @GetMapping("/reset")
    public RestMessageResponse reset() {
        return statisticsService.resetStatistics();
    }

    @GetMapping("/connector/{connectorName}")
    public String system(@PathVariable("connectorName") String name) {
        return statisticsService.getConnectorStatistics(name);
    }
}
