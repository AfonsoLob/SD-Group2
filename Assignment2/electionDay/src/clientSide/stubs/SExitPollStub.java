package clientSide.stubs;
import clientSide.interfaces.ExitPoll.IExitPoll_all;
import commInfra.ClientCom;
import commInfra.Message;
import commInfra.MessageType;

public class SExitPollStub implements IExitPoll_all {
    private final String serverHostName;
    private final int serverPort;
    private final String loggerHostName;
    private final int loggerPort;

    public SExitPollStub(String host, int port, String loggerHost, int loggerPort) {
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
            case LOG_EXIT_POLL_VOTE:
                outMessage = new Message(type, (int) args[0], (boolean) args[1]);  // voterId, myVote
                break;
            // case LOG_EXIT_POLL_CLOSED:
            //     outMessage = new Message(type, (int) args[0]);  // stillVotersInQueue
            //     break;
            // case LOG_EXIT_POLL_RESULTS:
            //     outMessage = new Message(type);
            //     break;
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

    // Methods from IExitPoll_Voter
    @Override
    public void exitPollingStation(int voterId, boolean myVote, boolean response) {
        ClientCom com = new ClientCom(serverHostName, serverPort);
        Message outMessage = new Message(MessageType.EXIT_POLL_ENTER, voterId, myVote, response);

        while (!com.open()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        com.sendAndReceive(outMessage);
        com.close();
        
        sendLogMessage(MessageType.LOG_EXIT_POLL_VOTE, voterId, myVote);
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

        return inMessage.getType() == MessageType.EXIT_POLL_OPENED;
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
        Message outMessage = new Message(MessageType.EXIT_POLL_CLOSE, -1, stillVotersInQueue);

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
