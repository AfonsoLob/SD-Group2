import Interfaces.*;
import Logging.*;
import Monitores.*;
import Threads.*;
import java.util.ArrayList;
import java.util.List;


public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("ElectionSimulation started");
        int maxVoters = 5;
        int maxCapacity = 2;
        int maxVotes = 10;
        Logger logger = Logger.getInstance(maxVoters, maxCapacity, maxVotes);

        IPollingStation pollingStation = MPollingStation.getInstance(maxCapacity, logger);
        IVotingBooth votingBooth = MVotingBooth.getInstance();

        // Mock variables
        IExitPoll exitPoll = new IExitPoll();


        List<TVoter> voters = new ArrayList<>();
        List<Thread> voterThreads = new ArrayList<>();

        for (int i = 0; i < maxVoters; i++) {
            TVoter voter = TVoter.getInstance(i, pollingStation, votingBooth, exitPoll, logger);
            voters.add(voter);
        }

        TClerk clerk = TClerk.getInstance(maxVotes, pollingStation, logger);
        
        //run threads
        Thread clerkThread = new Thread(clerk);
        clerkThread.start();
        
        for (TVoter voter : voters) {
            Thread voterThread = new Thread(voter);
            voterThreads.add(voterThread);
            voterThread.start();
        }

        clerkThread.join();
        for (Thread thread : voterThreads) {
            thread.join();
        }

        votingBooth.printFinalResults();
        System.out.println("ElectionSimulation finished");
        logger.saveCloseFile();
    }
}
