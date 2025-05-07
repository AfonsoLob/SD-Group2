package clientSide.stubs;

import commInfra.*;

public class MPollingStationStub {
    private final String serverHostName;
    private final int serverPort;

    public MPollingStationStub(String host, int port) {
        this.serverHostName = host;
        this.serverPort = port;
    }

    public boolean requestEntry(int voterID) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage, inMessage;

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }

        outMessage = new Message(MessageType.VOTER_ENTER_REQUEST, voterID);
        inMessage = com.sendAndReceive(outMessage);
        com.close();

        return inMessage.getType() == MessageType.VOTER_ENTER_GRANTED;
    }

    public boolean validateID(int voterID) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage, inMessage;

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }

        outMessage = new Message(MessageType.ID_CHECK_REQUEST, voterID);
        inMessage = com.sendAndReceive(outMessage);
        com.close();

        return inMessage.getType() == MessageType.ID_VALID;
    }

    public void castVote(int voterID, int vote) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.VOTE_CAST, voterID, vote, null);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }

        com.sendAndReceive(outMessage);
        com.close();
    }

    public void notifyExit(int voterID) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.VOTER_EXIT, voterID);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }

        com.sendAndReceive(outMessage);
        com.close();
    }
}
