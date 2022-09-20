package nz.co.pukeko.msginf.client.socket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TestSocketClient {
    private static TestSocketServer socketServer;

    @BeforeAll
    public static void setUp() {
        socketServer = new TestSocketServer(9955);
    }

    @Test
    public void socketServer() {
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
            assertNotNull(clientSocket);
        } catch (UnknownHostException e) {
            fail("Couldn't find localhost", e);
        } catch (IOException e) {
            fail("IO exception", e);
        }
    }
}
