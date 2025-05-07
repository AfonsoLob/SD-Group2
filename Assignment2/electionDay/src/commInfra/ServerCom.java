package commInfra;

import java.io.*;
import java.net.*;

/**
 * Server communication class for the Election Day simulation.
 * Handles server socket creation and client connection acceptance.
 */
public class ServerCom {
    /**
     * Listening socket
     */
    private ServerSocket listeningSocket = null;
    
    /**
     * Communication socket
     */
    private Socket communicationSocket = null;
    
    /**
     * Server port number for listening
     */
    private int serverPortNum;
    
    /**
     * Timeout for accepting connections (milliseconds)
     */
    private int timeout;
    
    /**
     * Instantiation of a communication channel (default timeout: 10000ms)
     * 
     * @param portNum port number for listening
     */
    public ServerCom(int portNum) {
        this.serverPortNum = portNum;
        this.timeout = 10000; // Default timeout: 10 seconds
    }
    
    /**
     * Instantiation of a communication channel with specified timeout
     * 
     * @param portNum port number for listening
     * @param timeout timeout for accepting connections (milliseconds)
     */
    public ServerCom(int portNum, int timeout) {
        this.serverPortNum = portNum;
        this.timeout = timeout;
    }
    
    /**
     * Start listening for client connections
     * 
     * @return true if server socket was successfully created, false otherwise
     */
    public boolean start() {
        try {
            listeningSocket = new ServerSocket(serverPortNum);
            listeningSocket.setSoTimeout(timeout);
        } catch (BindException e) {
            System.err.println("Server: address already in use: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("Server: error creating server socket: " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Accept a client connection
     * 
     * @return connection handler (or null if accept timed out or failed)
     */
    public ServerComHandler accept() {
        ServerComHandler connectionHandler = null;
        
        try {
            communicationSocket = listeningSocket.accept();
            connectionHandler = new ServerComHandler(communicationSocket);
        } catch (SocketTimeoutException e) {
            // This is not an error, it's just a timeout
            return null;
        } catch (IOException e) {
            System.err.println("Server: error accepting client connection: " + e.getMessage());
            return null;
        }
        
        return connectionHandler;
    }
    
    /**
     * Close the server listening socket
     */
    public void end() {
        try {
            if (listeningSocket != null) {
                listeningSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Server: error closing listening socket: " + e.getMessage());
        }
    }
    
    /**
     * Server communication handler for handling client connection
     */
    public static class ServerComHandler {
        /**
         * Communication socket with client
         */
        private Socket socket;
        
        /**
         * Input stream
         */
        private ObjectInputStream in;
        
        /**
         * Output stream
         */
        private ObjectOutputStream out;
        
        /**
         * Constructor - create handler for client connection
         * 
         * @param clientSocket socket connected to client
         * @throws IOException if stream creation fails
         */
        public ServerComHandler(Socket clientSocket) throws IOException {
            this.socket = clientSocket;
            // Important: create output stream first to avoid deadlock
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        
        /**
         * Receive a message from the client
         * 
         * @return message received
         */
        public Message readMessage() {
            Message message = null;
            
            try {
                message = (Message) in.readObject();
            } catch (IOException e) {
                System.err.println("Server: error reading message: " + e.getMessage());
                return null;
            } catch (ClassNotFoundException e) {
                System.err.println("Server: received object is not of the expected type: " + e.getMessage());
                return null;
            }
            
            return message;
        }
        
        /**
         * Send a message to the client
         * 
         * @param message message to be sent
         * @return true if message was successfully sent, false otherwise
         */
        public boolean writeMessage(Message message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Server: error sending message: " + e.getMessage());
                return false;
            }
            
            return true;
        }
        
        /**
         * Close the communication channel
         */
        public void close() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Server: error closing connection: " + e.getMessage());
            }
        }
    }
}
