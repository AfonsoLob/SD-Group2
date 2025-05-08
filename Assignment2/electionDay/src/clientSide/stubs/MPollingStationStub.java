package clientSide.stubs;

import commInfra.*;
import clientSide.interfaces.Pollingstation.IPollingStation_all;

public class MPollingStationStub implements IPollingStation_all {
    private final String serverHostName;
    private final int serverPort;

    public MPollingStationStub(String host, int port) {
        this.serverHostName = host;
        this.serverPort = port;
    }

    // Methods from IPollingStation_Voter
    @Override
    public boolean enterPollingStation(int voterId) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage, inMessage;

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        outMessage = new Message(MessageType.VOTER_ENTER_REQUEST, voterId);
        inMessage = com.sendAndReceive(outMessage);
        com.close();

        return inMessage.getType() == MessageType.VOTER_ENTER_GRANTED;
    }

    @Override
    public boolean waitIdValidation(int voterId) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage, inMessage;

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        outMessage = new Message(MessageType.ID_CHECK_REQUEST, voterId);
        inMessage = com.sendAndReceive(outMessage);
        com.close();

        return inMessage.getType() == MessageType.ID_VALID;
    }

    @Override
    public void voteA(int voterId) {
        sendVote(voterId, 1); // 1 - A
    }

    @Override
    public void voteB(int voterId) {
        sendVote(voterId, 0); // 0 - B
    }

    private void sendVote(int voterId, int vote) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.VOTE_CAST_REQUEST, voterId, vote, null);

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
        Message outMessage = new Message(MessageType.POLLING_STATION_OPEN);
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

        return inMessage.getType() == MessageType.POLLING_STATION_READY;
    }

    // Methods from IPollingStation_Clerk
    @Override
    public boolean callNextVoter() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.VALIDATE_NEXT_VOTER);
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

        return inMessage.getType() == MessageType.VOTER_ENTER_GRANTED;
    }

    @Override
    public void openPollingStation() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.POLLING_STATION_OPEN);

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
    public void closePollingStation() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.POLLING_STATION_CLOSE);

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
    public int numberVotersInQueue() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.ELECTION_RESULTS_REQUEST);
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

        if (inMessage.getType() == MessageType.ELECTION_RESULTS_RESPONSE) {
            return inMessage.getContent() instanceof Integer ? (Integer) inMessage.getContent() : -1; // Assuming content is an Integer
        }

        return -1; // Return -1 if the response is invalid
    }

    @Override
    public void printFinalResults() {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.ELECTION_RESULTS_REQUEST);

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