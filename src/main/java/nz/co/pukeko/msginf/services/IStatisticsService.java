package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.message.RestMessageResponse;

public interface IStatisticsService {

    public String getConnectorStatistics(String connectorName);

    public String allStatistics();

    public RestMessageResponse resetStatistics();
}
