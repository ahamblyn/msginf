package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.services.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to return statistical information.
 */
@Tag(name = "statistics", description = "Statistics API")
@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    /**
     * Construct the Statistics Controller
     * @param statisticsService the Statistics Service
     */
    public StatisticsController(@Autowired IStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Returns the statistics for all the messaging systems
     * @return the statistics for all the messaging systems
     */
    @Operation(
            summary = "Retrieve all the statistics",
            description = "Retrieve all the statistics",
            tags = {"statistics"})
    @GetMapping("/all")
    public String all() {
        return statisticsService.allStatistics();
    }

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    @Operation(
            summary = "Reset the statistics",
            description = "Reset the statistics",
            tags = {"statistics"})
    @GetMapping("/reset")
    public RestMessageResponse reset() {
        return statisticsService.resetStatistics();
    }

    /**
     * Returns the statistics for a system and connector
     * @param systemName the name of a system
     * @param connectorName the name of a connector
     * @return the statistics
     */
    @Operation(
            summary = "Retrieve the statistics for a system and connector",
            description = "Retrieve the statistics for a system and connector",
            tags = {"statistics"})
    @GetMapping("/system/{systemName}/connector/{connectorName}")
    public String statistics(@Parameter(description = "The connector name") @PathVariable("connectorName") String connectorName,
                         @Parameter(description = "The system name") @PathVariable("systemName") String systemName) {
        return statisticsService.getStatistics(systemName, connectorName);
    }
}
