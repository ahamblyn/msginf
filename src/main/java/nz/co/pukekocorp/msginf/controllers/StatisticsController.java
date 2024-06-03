package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.models.statistics.ConnectorStats;
import nz.co.pukekocorp.msginf.models.statistics.Stats;
import nz.co.pukekocorp.msginf.models.statistics.SystemStats;
import nz.co.pukekocorp.msginf.services.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Retrieve all the statistics",
            description = "Retrieve all the statistics",
            tags = {"statistics"})
    @GetMapping("/all")
    public ResponseEntity<Stats> statistics() {
        return ResponseEntity.of(statisticsService.allStatistics());
    }

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reset the statistics",
            description = "Reset the statistics",
            tags = {"statistics"})
    @GetMapping("/reset")
    public ResponseEntity<RestMessageResponse> reset() {
        return ResponseEntity.of(statisticsService.resetStatistics());
    }

    /**
     * Returns the connector statistics for a system and connector
     * @param systemName the name of a system
     * @param connectorName the name of a connector
     * @return the connector statistics
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Retrieve the connector statistics for a system and connector",
            description = "Retrieve the connector statistics for a system and connector",
            tags = {"statistics"})
    @GetMapping("/system/{systemName}/connector/{connectorName}")
    public ResponseEntity<ConnectorStats> connectorStatistics(@Parameter(description = "The connector name") @PathVariable("connectorName") String connectorName,
                                                              @Parameter(description = "The system name") @PathVariable("systemName") String systemName) {
        return ResponseEntity.of(statisticsService.getStatisticsForConnector(systemName, connectorName));
    }

    /**
     * Returns the system statistics for a system
     * @param systemName the name of a system
     * @return the system statistics
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Retrieve the system statistics for a system",
            description = "Retrieve the system statistics for a system",
            tags = {"statistics"})
    @GetMapping("/system/{systemName}")
    public ResponseEntity<SystemStats> systemStatistics(@Parameter(description = "The system name") @PathVariable("systemName") String systemName) {
        return ResponseEntity.of(statisticsService.getStatisticsForSystem(systemName));
    }
}
