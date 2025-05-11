package commInfra;

import java.io.*;
import java.net.*;

/**
 * Client communication class for the Election Day simulation.
 * Handles socket connections and message exchange with servers.
 */
public class ClientCom {
    /**
     * Communication socket
     */
    private Socket socket = null;
    
    /**
     * Input stream
     */
    private ObjectInputStream in = null;
    
    /**
     * Output stream
     */
    private ObjectOutputStream out = null;
    
    /**
     * Server hostname (or IP address)
     */
    private String serverHostname;
    
    /**
     * Server port number
     */
    private int serverPortNum;
    
    /**
     * Instantiation of a communication channel
     * 
     * @param serverHostName server host name
     * @param serverPortNum server port number
     */
    public ClientCom(String serverHostName, int serverPortNum) {
        this.serverHostname = serverHostName;
        this.serverPortNum = serverPortNum;
    }
    
    /**
     * Open communication channel
     * 
     * @return true if channel was successfully opened, false otherwise
     */
    public boolean open() {
        boolean success = true;
        SocketAddress serverAddress = new InetSocketAddress(serverHostname, serverPortNum);
        
        try {
            socket = new Socket();
            socket.connect(serverAddress);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Client: " + serverHostname + " is unknown");
            success = false;
        } catch (IOException e) {
            // System.err.println("Client: couldn't establish connection to " + serverHostname + ":" + serverPortNum);
            success = false;
        }
        
        return success;
    }
    
    /**
     * Close communication channel
     * 
     * @return true if channel was successfully closed, false otherwise
     */
    public boolean close() {
        boolean success = true;
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Client: error closing socket, streams: " + e.getMessage());
            success = false;
        }
        
        return success;
    }
    
    /**
     * Send a message and wait for the reply
     * 
     * @param messageToSend message to be sent
     * @return message received as reply
     */
    public Message sendAndReceive(Message messageToSend) {
        Message messageReceived = null;
        
        try {
            
            out.writeObject(messageToSend);
            out.flush();
            messageReceived = (Message) in.readObject();
            // System.out.println("WEZA 1 Client: sending message: " + messageToSend);
        } catch (IOException e) {
            // System.err.println("Client: error sending/receiving message: " + e.getMessage());
            messageReceived = null;
        } catch (ClassNotFoundException e) {
            // System.err.println("Client: received object is not of the expected type: " + e.getMessage());
            messageReceived = null;
        }

        System.out.println("WEZA 2 lient: sending message: " + messageToSend);
        return messageReceived;
    }
}