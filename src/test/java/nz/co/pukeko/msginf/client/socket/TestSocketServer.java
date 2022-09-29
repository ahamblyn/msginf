package nz.co.pukeko.msginf.client.socket;

import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

@Slf4j
public class TestSocketServer implements Runnable {
    private final int port;

    public TestSocketServer(int port) {
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
            log.info("Waiting  for connections on port: " + port);
            clientSocket = serverSocket.accept();
            log.info("Connect made on port: " + port);
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
        log.info("Exiting TestSocketServer thread");
    }
}
