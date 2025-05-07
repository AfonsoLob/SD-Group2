package clientSide.Main;

import clientSide.entities.TVoter;
import clientSide.stubs.MPollingStationStub;
import clientSide.stubs.MExitPollStub;

public class TvoterMain {
    public static void main(String[] args) {
        // Check if number of voters is provided
        if (args.length != 1) {
            System.out.println("Usage: java TvoterMain <number_of_voters>");
            System.exit(1);
        }

        // Parse number of voters
        int numVoters = Integer.parseInt(args[0]);
        if (numVoters < 3 || numVoters > 10) {
            System.out.println("Number of voters must be between 3 and 10");
            System.exit(1);
        }

        // Stubs
        MPollingStationStub pollingStation = new MPollingStationStub();
        MExitPollStub exitPoll = new MExitPollStub();

        // Create and start voter threads
        Thread[] voters = new Thread[numVoters];
        for (int i = 0; i < numVoters; i++) {
            voters[i] = new Thread(TVoter.getInstance(i, pollingStation, exitPoll));
            voters[i].start();
        }

        // Wait for all voters to finish
        for (int i = 0; i < numVoters; i++) {
            try {
                voters[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
