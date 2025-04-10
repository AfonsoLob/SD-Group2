package commInfra;

import java.io.*;
import java.net.*;

public class ServerCom {
    private ServerSocket serverSocket;
    private int portNumber;

    public ServerCom(int portNumber) throws IOException {
        this.portNumber = portNumber;
        this.serverSocket = new ServerSocket(portNumber);
    }

    public Socket accept() throws IOException {
        return serverSocket.accept();
    }

    public void close() throws IOException {
        serverSocket.close();
    }
}
