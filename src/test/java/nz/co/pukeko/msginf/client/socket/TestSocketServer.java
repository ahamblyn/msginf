package nz.co.pukeko.msginf.client.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class TestSocketServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(TestSocketServer.class);
    private final int port;

    public TestSocketServer(int port) {
        this.port = port;
    }

    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Listening to port: " + port);
        } catch (IOException e) {
            logger.error("Could not listen to port: " + port, e);
            return;
        }
        Socket clientSocket;
        try {
            logger.info("Waiting  for connections on port: " + port);
            clientSocket = serverSocket.accept();
            logger.info("Connect made on port: " + port);
        } catch (IOException e) {
            logger.error("Could not listen to port: " + port, e);
            return;
        }
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            // don't care...
        }
        logger.info("Exiting TestSocketServer thread");
    }
}
