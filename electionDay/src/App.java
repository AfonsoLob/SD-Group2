import java.util.ArrayList;
import java.util.List;

import Interfaces.*;
import Monitores.*;
import Threads.*;
import Logging.*;


public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("ElectionSimulation started");
        int maxVoters = 2;
        int maxCapacity = 3;

        IPollingStation pollingStation = MPollingStation.getInstance(maxCapacity);

        // Mock variables
        IVotingBooth votingBooth = new IVotingBooth();
        IExitPoll exitPoll = new IExitPoll();
        Logger logger = new Logger(1,2,3);


        List<TVoter> voters = new ArrayList<>();
        for (int i = 0; i < maxVoters; i++) {
            TVoter voter = new TVoter(i, pollingStation, votingBooth,exitPoll,logger);
            voters.add(voter);
        }

        TClerk clerk = new TClerk(1, pollingStation);
        
        //run threads
        Thread clerkThread = new Thread(clerk);
        clerkThread.start();
        for (TVoter voter : voters) {
            Thread voterThread = new Thread(voter);
            voterThread.start();
        }

    }
}
