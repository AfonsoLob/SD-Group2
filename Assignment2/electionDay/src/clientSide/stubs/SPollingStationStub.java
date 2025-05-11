package clientSide.stubs;

import clientSide.interfaces.Pollingstation.IPollingStation_all;
import commInfra.ClientCom;
import commInfra.Message;
import commInfra.MessageType;
// import example.commInfra.*;

public class SPollingStationStub implements IPollingStation_all {
    private final String serverHostName;
    private final int serverPort;
    private final String loggerHostName;
    private final int loggerPort;

    public SPollingStationStub(String host, int port, String loggerHost, int loggerPort) {
        this.serverHostName = host;
        this.serverPort = port;
        this.loggerHostName = loggerHost;
        this.loggerPort = loggerPort;
    }

    private void sendLogMessage(MessageType type, Object... args) {
        ClientCom com = new ClientCom(loggerHostName, loggerPort);
        Message outMessage;

        // Create appropriate message based on type and args
        switch (type) {
            case LOG_VOTER_AT_DOOR:
            case LOG_VOTER_ENTERING_QUEUE:
            case LOG_VALIDATING_VOTER:
                outMessage = new Message(type, (int) args[0]);
                break;
            case LOG_VOTER_IN_BOOTH:
                outMessage = new Message(type, (int) args[0], (boolean) args[1]);
                break;
            case LOG_STATION_OPENING:
            case LOG_STATION_CLOSING:
                outMessage = new Message(type);
                break;
            default:
                System.err.println("Invalid log message type: " + type);
                return;
        }

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

    // Methods from IPollingStation_Voter
    @Override
    public boolean enterPollingStation(int voterId) {
        sendLogMessage(MessageType.LOG_VOTER_AT_DOOR, voterId);
        
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

        if (inMessage.getType() == MessageType.VOTER_ENTER_GRANTED) {
            sendLogMessage(MessageType.LOG_VOTER_ENTERING_QUEUE, voterId);
        }

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

        boolean isValid = inMessage.getType() == MessageType.ID_VALID;
        sendLogMessage(MessageType.LOG_VALIDATING_VOTER, voterId, isValid);

        return isValid;
    }

    @Override
    public void voteA(int voterId) {
        sendVote(voterId, true); // 1 - A
    }

    @Override
    public void voteB(int voterId) {
        sendVote(voterId, false); // 0 - B
    }

    private void sendVote(int voterId, boolean vote) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.VOTE_CAST_REQUEST, voterId, vote);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        com.sendAndReceive(outMessage);
        com.close();
        
        sendLogMessage(MessageType.LOG_VOTER_IN_BOOTH, voterId, vote); // TODO: Check if it's here or in callNextVoter
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

        return inMessage.getType() == MessageType.ID_VALID;
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
        
        sendLogMessage(MessageType.LOG_STATION_OPENING);
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
        
        sendLogMessage(MessageType.LOG_STATION_CLOSING);
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