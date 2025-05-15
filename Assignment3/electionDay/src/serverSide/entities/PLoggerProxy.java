package serverSide.entities;


import commInfra.Message;
import commInfra.MessageException;
import commInfra.ServerCom.ServerComHandler;
import serverSide.interfaces.Logger.ILogger_all;


public class PLoggerProxy extends Thread {

    private static int nProxy = 0;
    private ServerComHandler sconi;
    private ILogger_all loggerInter;

    private PLoggerProxy(ServerComHandler sconi, ILogger_all loggerInter) {
        super("PLoggerProxy_" + PLoggerProxy.getProxyId());
        this.sconi = sconi;
        this.loggerInter = loggerInter;
    }

    public static PLoggerProxy getInstanceLoggerProxy(ServerComHandler sconi, ILogger_all loggerInter) {
        return new PLoggerProxy(sconi, loggerInter);
    }

    private static int getProxyId() {
        Class<?> cl = null;
        int proxyId;

        try {
            cl = Class.forName("serverSide.entities.PLoggerProxy");
        } catch (ClassNotFoundException e) {
            System.err.println("Data type PLoggerProxy was not found!");
            e.printStackTrace();
            System.exit(1);
        }
        synchronized (cl) {
            proxyId = nProxy;
            nProxy += 1;
        }
        return proxyId;
    }

    @Override
    public void run ()
    {
        Message inMessage = null,                                      // service request
        outMessage = null;                                     // service reply

        /* service providing */

        inMessage = (Message) sconi.readMessage();                     // get service request
        try{
                outMessage = processAndReply (inMessage);         // process it
        }catch (MessageException e){
            System.err.println ("Thread " + getName () + ": " + e.getMessage () + "!");
            System.err.println (e.getMessageVal ().toString ());
            System.exit (1);
        }
          sconi.writeMessage(outMessage);                                // send service reply
          sconi.close ();                                                // close the communication channel
        }


    public Message processAndReply (Message inMessage) throws MessageException
    {
        Message outMessage = null;                                     // mensagem de resposta

        /* validation of the incoming message */

        switch (inMessage.getType()){
            case LOG_VOTER_AT_DOOR:
                if (inMessage.getId() < 0) // Assuming voterId is an int and should be non-negative
                    throw new MessageException("Invalid voterId for LOG_VOTER_AT_DOOR!", inMessage);
                loggerInter.voterAtDoor(inMessage.getId());
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;
            case LOG_VOTER_ENTERING_QUEUE:
                if (inMessage.getId() < 0)
                    throw new MessageException("Invalid voterId for LOG_VOTER_ENTERING_QUEUE!", inMessage);
                loggerInter.voterEnteringQueue(inMessage.getId());
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;

            case LOG_VALIDATING_VOTER:
                if (inMessage.getId() < 0)
                    throw new MessageException("Invalid voterId for LOG_VALIDATING_VOTER!", inMessage);
                    // System.out.println("WEZA Thread " + getName() + ": validating voter " + inMessage.getId() + " with option " + inMessage.getCloseIn());
                    loggerInter.validatingVoter(inMessage.getId(), inMessage.getCloseIn());
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;
            case LOG_VOTER_IN_BOOTH:
                 if (inMessage.getId() < 0)
                     throw new MessageException("Invalid voterId for LOG_VOTER_IN_BOOTH!", inMessage);
                loggerInter.voterInBooth(inMessage.getId(), inMessage.getVotingOption());
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;

            case LOG_EXIT_POLL_VOTE:
                if (inMessage.getId() < 0)
                    throw new MessageException("Invalid voterId for LOG_EXIT_POLL_VOTE!", inMessage);

                // System.out.println("didHeVote: " + inMessage.didHeVote());
                if(!inMessage.didHeVote())
                    loggerInter.exitPollVote(inMessage.getId(), "");
                else
                    if(inMessage.getVotingOption())
                        loggerInter.exitPollVote(inMessage.getId(), "A");
                    else
                        loggerInter.exitPollVote(inMessage.getId(), "B");
               
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;

            case LOG_STATION_OPENING:
                loggerInter.stationOpening();
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;
            case LOG_STATION_CLOSING:
                loggerInter.stationClosing();
                outMessage = new Message(commInfra.MessageType.LOG_ACK);
                break;
            case REQ_VOTE_COUNTS:
                String voteCounts = loggerInter.getVoteCounts();
                outMessage = new Message(commInfra.MessageType.REP_VOTE_COUNTS, voteCounts);
                break;
            case REQ_VOTERS_PROCESSED:
                int votersProcessed = loggerInter.getVotersProcessed();
                outMessage = new Message(commInfra.MessageType.REP_VOTERS_PROCESSED, votersProcessed);
                break;
            case REQ_IS_STATION_OPEN:
                boolean isStationOpen = loggerInter.isStationOpen();
                outMessage = new Message(commInfra.MessageType.REP_IS_STATION_OPEN, isStationOpen);
                break;
            case REQ_CURRENT_VOTER_IN_BOOTH:
                String currentVoterInBooth = loggerInter.getCurrentVoterInBooth();
                outMessage = new Message(commInfra.MessageType.REP_CURRENT_VOTER_IN_BOOTH, currentVoterInBooth);
                break;
            case REQ_CURRENT_QUEUE_SIZE:
                int currentQueueSize = loggerInter.getCurrentQueueSize();
                outMessage = new Message(commInfra.MessageType.REP_CURRENT_QUEUE_SIZE, currentQueueSize);
                break;
            case SIMULATION_END:
                loggerInter.saveCloseFile();
                outMessage = new Message(commInfra.MessageType.LOGGER_TERMINATED);
                break;
            default:
                throw new MessageException ("Invalid message type: " + inMessage.getType(), inMessage);
        }
        return outMessage;
    }
}
