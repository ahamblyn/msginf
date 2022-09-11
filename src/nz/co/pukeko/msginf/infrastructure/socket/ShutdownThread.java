package nz.co.pukeko.msginf.infrastructure.socket;

import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import nz.co.pukeko.msginf.client.adapter.AdministerMessagingInfrastructure;

/**
 * This thread listens to a port and shuts down the messaging infrastructure
 * when a connection is made to this port.
 */
public class ShutdownThread implements Runnable {
    private static Logger logger = Logger.getLogger(ShutdownThread.class);
    private int port;

    public ShutdownThread(int port) {
        this.port = port;
    }

    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Listening to port: " + port);
        } catch (IOException e) {
            logger.error("Could not listen to port: " + port, e);
            return;
        }
        Socket clientSocket = null;
        try {
            logger.info("Waiting for connections on port: " + port);
            clientSocket = serverSocket.accept();
            logger.info("Connection made on port: " + port);
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
        logger.info("Shutting down the application");
        AdministerMessagingInfrastructure.getInstance().shutdown();
    }
}
