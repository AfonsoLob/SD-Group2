package serverSide.entities;

import commInfra.Message;
import commInfra.ServerCom;

import serverSide.interfaces.ExitPoll.IExitPoll_all;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * PMExitPollClientProxy - Thread that handles communication with an exit poll client.
 * It implements the protocol for interaction.
 * It manages the Thread life cycle, the state and the communication channels
 */
public class PMExitPollClientProxy extends Thread {
    /**
    *  Number of instantiayed threads.
    */
    private static int nProxy = 0;

    /**
   *  Communication channel.
   */
    private ServerCom sconi;
    
    /**
     * Exit poll shared region reference
     */
    private final IExitPoll_all ExitPoll;

    /**
    *  Voter identification.
    */
    private int voterId;

    /**
    *  Voter state.
    */
    private int voterState;


    /**
     * Create a proxy thread to handle communication with an exit poll client.
     *
     * @param sock socket connected to the client
     * @param exitPoll reference to the exit poll shared region
     */
    public PMExitPollClientProxy(Socket sock, ExitPoll exitPoll) {
        this.sock = sock;
        this.exitPoll = exitPoll;
    }



    /**
     * Thread life cycle.
     * Service provider agent is running in the PM server.
     */
    @Override
    public void run() {
        Message inMessage, outMessage;

        try {
            // Create input and output channels
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());

            // Process messages until told to terminate
            boolean sessionDone = false;
            while (!sessionDone) {
                // Get incoming message
                inMessage = (Message) in.readObject();

                // Process message
                switch (inMessage.getType()) {
                    // Process exit poll data
                    case Message.SUBMIT_EXIT_POLL:
                        ExitPollData pollData = (ExitPollData) inMessage.getData();
                        exitPoll.receiveExitPollData(pollData);
                        outMessage = new Message(Message.ACK);
                        break;

                    // Request exit poll results
                    case Message.REQUEST_POLL_RESULTS:
                        PollResults results = exitPoll.getExitPollResults();
                        outMessage = new Message(Message.POLL_RESULTS, results);
                        break;

                    // End of service
                    case Message.END:
                        outMessage = new Message(Message.ACK);
                        sessionDone = true;
                        break;

                    default:
                        throw new RuntimeException("Invalid message type: " + inMessage.getType());
                }

                // Send reply
                out.writeObject(outMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("PMExitPollClientProxy: " + e.getMessage());
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
