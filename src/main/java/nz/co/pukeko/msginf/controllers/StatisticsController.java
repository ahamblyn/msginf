package nz.co.pukeko.msginf.controllers;

import nz.co.pukeko.msginf.services.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private IStatisticsService statisticsService;

    public StatisticsController(@Autowired IStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/all")
    public String all() {
        String statistics = statisticsService.allStatistics();
        return statistics;
    }

    @GetMapping("/connector/{connectorName}")
    public String system(@PathVariable("connectorName") String name) {
        String statistics = statisticsService.getConnectorStatistics(name);
        return statistics;
    }
}
