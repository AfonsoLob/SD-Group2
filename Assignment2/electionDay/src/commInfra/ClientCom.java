package commInfra;

import java.io.*;
import java.net.*;

public class ClientCom {
    private String serverHostName;
    private int serverPortNumb;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientCom(String serverHostName, int serverPortNumb) {
        this.serverHostName = serverHostName;
        this.serverPortNumb = serverPortNumb;
    }

    public boolean open() {
        try {
            socket = new Socket(serverHostName, serverPortNumb);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void writeObject(Object obj) throws IOException {
        out.writeObject(obj);
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}