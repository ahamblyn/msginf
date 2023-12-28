package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;

/**
 * The Statistics Service interface.
 */
public interface IStatisticsService {

    /**
     * Returns the statistics for a connector
     * @param connectorName the name of a connector
     * @return the statistics
     */
    String getConnectorStatistics(String connectorName);

    /**
     * Returns the statistics for all the messaging systems
     * @return the statistics for all the messaging systems
     */
    String allStatistics();

    /**
     * Reset the statistics
     * @return the result of the reset
     */
    RestMessageResponse resetStatistics();
}
