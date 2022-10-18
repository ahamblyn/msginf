package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.message.RestMessageResponse;

public interface IStatisticsService {

    String getConnectorStatistics(String connectorName);

    String allStatistics();

    RestMessageResponse resetStatistics();
}
