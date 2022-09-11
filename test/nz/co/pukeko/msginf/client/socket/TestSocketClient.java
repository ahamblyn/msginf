package nz.co.pukeko.msginf.client.socket;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

public class TestSocketClient extends TestCase {
    private static Logger logger = Logger.getLogger(TestSocketClient.class);
    private TestSocketServer socketServer;

    public void setUp() {
        socketServer = new TestSocketServer(9955);
    }

    public void testSocketServer() {
        Thread thread = new Thread(socketServer);
        thread.start();
        // sleep for a few seconds
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            // whatever...
        }
        // connect to the server socket
        Socket clientSocket = null;
        try {
            clientSocket = new Socket("localhost", 9955);
            logger.info("Connected to server localhost:9955");
        } catch (UnknownHostException e) {
            logger.error("Couldn't find localhost", e);
        } catch (IOException e) {
            logger.error("IO exception", e);
        }
    }
}
