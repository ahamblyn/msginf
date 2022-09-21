package nz.co.pukeko.msginf.client.testapp.data;

public class Connector {
	private final String messagingSystemName;
	private final String connectorName;
	
	public Connector(String messagingSystemName, String connectorName) {
		this.messagingSystemName = messagingSystemName;
		this.connectorName = connectorName;
	}
	
	public String getMessagingSystemName() {
		return messagingSystemName;
	}
	
	public String getConnectorName() {
		return connectorName;
	}

	public String toString() {
		return connectorName;
	}
}
