package nz.co.pukeko.msginf.infrastructure.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.client.adapter.AdministerMessagingInfrastructure;

/**
 * This thread listens to a port and shuts down the messaging infrastructure
 * when a connection is made to this port.
 */
@Slf4j
public class ShutdownThread implements Runnable {
    private final int port;

    public ShutdownThread(int port) {
        this.port = port;
    }

    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
            log.info("Listening to port: " + port);
        } catch (IOException e) {
            log.error("Could not listen to port: " + port, e);
            return;
        }
        Socket clientSocket;
        try {
            log.info("Waiting for connections on port: " + port);
            clientSocket = serverSocket.accept();
            log.info("Connection made on port: " + port);
        } catch (IOException e) {
            log.error("Could not listen to port: " + port, e);
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
        log.info("Shutting down the application");
        AdministerMessagingInfrastructure.getInstance().shutdown();
    }
}
