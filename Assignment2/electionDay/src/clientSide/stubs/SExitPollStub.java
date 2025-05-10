package clientSide.stubs;

import clientSide.interfaces.ExitPoll.IExitPoll_all;
import commInfra.ClientCom;
import commInfra.Message;
import commInfra.MessageType;

public class SExitPollStub implements IExitPoll_all {
    private final String serverHostName;
    private final int serverPort;

    public SExitPollStub(String host, int port) {
        this.serverHostName = host;
        this.serverPort = port;
    }

    // Methods from IExitPoll_Voter
    @Override
    public void exitPollingStation(int voterId, boolean myVote, boolean response) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.EXIT_POLL_ENTER, voterId, myVote, response); // TODO: create a new message constructor?

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        com.sendAndReceive(outMessage);
        com.close();
    }

    @Override
    public boolean isOpen() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.EXIT_POLL_OPEN);
        Message inMessage;

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        inMessage = com.sendAndReceive(outMessage);
        com.close();

        return inMessage.getType() == MessageType.EXIT_POLL_RESPONSE;
    }

    // Methods from IExitPoll_Pollster
    @Override
    public void inquire() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.EXIT_POLL_INQUIRY);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        com.sendAndReceive(outMessage);
        com.close();
    }

    @Override
    public void printExitPollResults() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.EXIT_POLL_PRINT);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        com.sendAndReceive(outMessage);
        com.close();
    }

    // Methods from IExitPoll_Clerk
    @Override
    public void closeIn(int stillVotersInQueue) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.EXIT_POLL_CLOSED, -1, stillVotersInQueue);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        com.sendAndReceive(outMessage);
        com.close();
    }
}
