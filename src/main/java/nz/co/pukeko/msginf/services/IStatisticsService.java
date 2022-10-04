package nz.co.pukeko.msginf.services;

import nz.co.pukeko.msginf.models.message.MessageResponse;

public interface IStatisticsService {

    public String getConnectorStatistics(String connectorName);

    public String allStatistics();

    public MessageResponse resetStatistics();
}
