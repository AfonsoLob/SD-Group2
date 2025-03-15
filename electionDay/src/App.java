import java.util.ArrayList;
import java.util.List;

import Interfaces.*;
import Monitores.*;
import Threads.*;
import Logging.*;


public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("ElectionSimulation started");
        int maxVoters = 5;
        int maxCapacity = 2;

        IPollingStation pollingStation = MPollingStation.getInstance(maxCapacity);
        IVotingBooth votingBooth = MVotingBooth.getInstance();

        // Mock variables
        IExitPoll exitPoll = new IExitPoll();
        Logger logger = new Logger(1,2,3);


        List<TVoter> voters = new ArrayList<>();
        List<Thread> voterThreads = new ArrayList<>();

        for (int i = 0; i < maxVoters; i++) {
            TVoter voter = new TVoter(i, pollingStation, votingBooth,exitPoll,logger);
            voters.add(voter);
        }

        TClerk clerk = new TClerk(10, pollingStation);
        
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
    }
}
