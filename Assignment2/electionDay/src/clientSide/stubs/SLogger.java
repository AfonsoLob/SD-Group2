package clientSide.stubs;

import clientSide.interfaces.Logger.ILogger_all;
import commInfra.ClientCom;
import commInfra.Message;
import commInfra.MessageType;

public class SLogger implements ILogger_all {

    private final String serverHostName;
    private final int serverPortNum;

    /**
     * Constructor for SLogger.
     *
     * @param serverHostName The hostname of the server where the Logger is running.
     * @param serverPortNum  The port number on which the Logger server is listening.
     */
    private SLogger(String serverHostName, int serverPortNum) {
        this.serverHostName = serverHostName;
        this.serverPortNum = serverPortNum;
    }

    public static ILogger_all getInstance(String serverHostName, int serverPortNum) {
        return new SLogger(serverHostName, serverPortNum);
    }

    @Override
    public void voterAtDoor(int voterId) {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for voterAtDoor (" + voterId + ").");
            return;
        }
        outMessage = new Message(MessageType.LOG_VOTER_AT_DOOR, voterId);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in voterAtDoor (" + voterId + ") communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public void voterEnteringQueue(int voterId) {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for voterEnteringQueue (" + voterId + ").");
            return;
        }
        outMessage = new Message(MessageType.LOG_VOTER_ENTERING_QUEUE, voterId);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in voterEnteringQueue (" + voterId + ") communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public void validatingVoter(int voterId, boolean valid) {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for validatingVoter (" + voterId + ", " + valid + ").");
            return;
        }
        outMessage = new Message(MessageType.LOG_VALIDATING_VOTER, voterId, valid);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in validatingVoter (" + voterId + ", " + valid + ") communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public void voterInBooth(int voterId, boolean voteA) {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for voterInBooth (" + voterId + ", " + voteA + ").");
            return;
        }
        outMessage = new Message(MessageType.LOG_VOTER_IN_BOOTH, voterId, voteA);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in voterInBooth (" + voterId + ", " + voteA + ") communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public void exitPollVote(int voterId, String vote) {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for exitPollVote (" + voterId + ", '" + vote + "').");
            return;
        }
        outMessage = new Message(MessageType.LOG_EXIT_POLL_VOTE, voterId, vote);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in exitPollVote (" + voterId + ", '" + vote + "') communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public void stationOpening() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for stationOpening.");
            return;
        }
        outMessage = new Message(MessageType.LOG_STATION_OPENING);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in stationOpening communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public void stationClosing() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for stationClosing.");
            return;
        }
        outMessage = new Message(MessageType.LOG_STATION_CLOSING);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOG_ACK) {
            System.err.println("SLogger: Error in stationClosing communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
    }

    @Override
    public String getVoteCounts() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;
        String voteCounts = ""; // Default to empty string

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for getVoteCounts.");
            return voteCounts;
        }
        outMessage = new Message(MessageType.REQ_VOTE_COUNTS);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage != null && inMessage.getType() == MessageType.REP_VOTE_COUNTS) {
            voteCounts = inMessage.getStringVal();
        } else {
            System.err.println("SLogger: Error in getVoteCounts communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
        return voteCounts;
    }

    @Override
    public int getVotersProcessed() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;
        int votersProcessed = -1; // Default error value

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for getVotersProcessed.");
            return votersProcessed;
        }
        outMessage = new Message(MessageType.REQ_VOTERS_PROCESSED);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage != null && inMessage.getType() == MessageType.REP_VOTERS_PROCESSED) {
            votersProcessed = inMessage.getIntVal();
        } else {
            System.err.println("SLogger: Error in getVotersProcessed communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
        return votersProcessed;
    }

    @Override
    public boolean isStationOpen() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;
        boolean isOpen = false; // Default value

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for isStationOpen.");
            return isOpen;
        }
        outMessage = new Message(MessageType.REQ_IS_STATION_OPEN);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage != null && inMessage.getType() == MessageType.REP_IS_STATION_OPEN) {
            isOpen = inMessage.getBoolVal();
        } else {
            System.err.println("SLogger: Error in isStationOpen communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
        return isOpen;
    }

    @Override
    public String getCurrentVoterInBooth() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;
        String currentVoter = ""; // Default to empty string

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for getCurrentVoterInBooth.");
            return currentVoter;
        }
        outMessage = new Message(MessageType.REQ_CURRENT_VOTER_IN_BOOTH);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage != null && inMessage.getType() == MessageType.REP_CURRENT_VOTER_IN_BOOTH) {
            currentVoter = inMessage.getStringVal();
        } else {
            System.err.println("SLogger: Error in getCurrentVoterInBooth communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
        return currentVoter;
    }

    @Override
    public int getCurrentQueueSize() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;
        int queueSize = -1; // Default error value

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for getCurrentQueueSize.");
            return queueSize;
        }
        outMessage = new Message(MessageType.REQ_CURRENT_QUEUE_SIZE);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage != null && inMessage.getType() == MessageType.REP_CURRENT_QUEUE_SIZE) {
            queueSize = inMessage.getIntVal();
        } else {
            System.err.println("SLogger: Error in getCurrentQueueSize communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        }
        cc.close();
        return queueSize;
    }

    @Override
    public void saveCloseFile() {
        ClientCom cc = new ClientCom(serverHostName, serverPortNum);
        Message outMessage, inMessage;

        if (!cc.open()) {
            System.err.println("SLogger: Failed to open connection for saveCloseFile (SIMULATION_END).");
            return;
        }
        // PLoggerProxy maps SIMULATION_END to loggerInter.saveCloseFile()
        outMessage = new Message(MessageType.SIMULATION_END);
        inMessage = cc.sendAndReceive(outMessage);

        if (inMessage == null || inMessage.getType() != MessageType.LOGGER_TERMINATED) {
            System.err.println("SLogger: Error in saveCloseFile (SIMULATION_END) communication or unexpected reply: " + (inMessage != null ? inMessage.getType() : "null"));
        } else {
            System.out.println("SLogger: Logger signaled termination via SIMULATION_END.");
        }
        cc.close();
    }

    // Methods from ILogger_GUI or ILogger_Common not directly handled by PLoggerProxy
    // for client->server RPC should be implemented if ILogger_all requires them.
    // For example, clear() is in server Logger but not in PLoggerProxy.
    // If clientSide.interfaces.Logger.ILogger_all includes them, they might be no-ops
    // or throw UnsupportedOperationException in a stub context if not remotely callable.

    @Override
    public void clear() {
        // This method is present in the server-side Logger implementation
        // but not exposed via PLoggerProxy for remote calls.
        // If it's part of the client-side ILogger_all interface,
        // it should either be a no-op or throw an exception.
        System.err.println("SLogger: clear() method is not supported for remote invocation.");
        // throw new UnsupportedOperationException("clear() is not remotely callable on the Logger stub.");
    }

    // Additional methods from ILogger_GUI if they were part of clientSide.interfaces.Logger.ILogger_all
    // and not meant for RPC would also go here, likely as no-ops or throwing UnsupportedOperationException.
    // For example, if ILogger_GUI had methods like:
    // public void updateVoterState(...) { /* no-op or throw */ }
    // public void updateStatistics(...) { /* no-op or throw */ }
    // These are typically handled by a local GUI, not via RPC to a logger.
}
