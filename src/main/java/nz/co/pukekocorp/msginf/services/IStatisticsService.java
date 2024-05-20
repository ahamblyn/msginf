package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;
import nz.co.pukekocorp.msginf.models.statistics.ConnectorStats;
import nz.co.pukekocorp.msginf.models.statistics.Stats;
import nz.co.pukekocorp.msginf.models.statistics.SystemStats;

import java.util.Optional;

/**
 * The Statistics Service interface.
 */
public interface IStatisticsService {

    /**
     * Returns the connector statistics for a system and connector
     * @param systemName the name of a system
     * @param connectorName the name of a connector
     * @return the connector statistics
     */
    Optional<ConnectorStats> getStatisticsForConnector(String systemName, String connectorName);

    /**
     * Returns the system statistics for a system
     * @param systemName the name of a system
     * @return the system statistics
     */
    Optional<SystemStats> getStatisticsForSystem(String systemName);

    /**
     * Returns the statistics for all the messaging systems
     * @return the statistics for all the messaging systems
     */
    Optional<Stats> allStatistics();

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    Optional<RestMessageResponse> resetStatistics();
}
