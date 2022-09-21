package nz.co.pukeko.msginf.client.testapp.data;

import java.util.ArrayList;
import java.util.List;

public class Command {
	private final String test;
	private final Connector connector;
	private final int numberOfThreads;
	private final int numberOfMessagesPerThread;
	private final String fileName;
    private final int port;

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
	
    public String[] createCommand() {
		List<String> commands = new ArrayList<>();
		if (isWindows()) {
			commands.add("test.bat");
		} else {
			commands.add("./test.sh");
		}
		commands.add(test);
		commands.add(connector.getMessagingSystemName());
		commands.add(connector.getConnectorName());
		commands.add(String.valueOf(numberOfThreads));
		commands.add(String.valueOf(numberOfMessagesPerThread));
		commands.add(fileName);
		commands.add(String.valueOf(port));
		return commands.toArray(String[]::new);
	}
	
	private boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.toUpperCase().startsWith("WINDOWS");
	}
}
