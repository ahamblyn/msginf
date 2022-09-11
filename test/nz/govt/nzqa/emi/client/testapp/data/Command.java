package nz.govt.nzqa.emi.client.testapp.data;

public class Command {
	private String test;
	private Connector connector;
	private int numberOfThreads;
	private int numberOfMessagesPerThread;
	private String fileName;
    private int port;

    public Command(String test, Connector connector, int numberOfThreads, int numberOfMessagesPerThread, String fileName, int port) {
		this.test = test;
		this.connector = connector;
		this.numberOfThreads = numberOfThreads;
		this.numberOfMessagesPerThread = numberOfMessagesPerThread;
		this.fileName = fileName;
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }
	
    public String createCommand() {
		StringBuffer command = new StringBuffer();
		if (isWindows()) {
			command.append("test.bat ");
		} else {
			command.append("./test.sh ");
		}
		command.append(test + " ");
		command.append(connector.getMessagingSystemName() + " ");
		command.append(connector.getConnectorName() + " ");
		command.append(numberOfThreads + " ");
		command.append(numberOfMessagesPerThread + " ");
		command.append(fileName + " ");
        command.append(port);
		return command.toString();
	}
	
	private boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os.toUpperCase().startsWith("WINDOWS")) {
			return true;
		} else {
			return false;
		}
	}
}
