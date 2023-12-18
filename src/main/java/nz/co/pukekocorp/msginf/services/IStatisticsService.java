package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.message.RestMessageResponse;

public interface IStatisticsService {

    String getConnectorStatistics(String connectorName);

    String allStatistics();

    RestMessageResponse resetStatistics();
}
