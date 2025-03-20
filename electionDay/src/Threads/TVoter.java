package Threads;
import Interfaces.IExitPoll;
import Interfaces.IPollingStation;
import Interfaces.IVotingBooth;

public class TVoter implements Runnable {
    private int voterId;
    private final IPollingStation pollingStation;
    private final IVotingBooth votingBooth;
    private final IExitPoll exitPoll;
    private boolean myVote;

    private TVoter(int id, IPollingStation pollingStation, IVotingBooth votingBooth, IExitPoll exitPoll) {
        this.voterId = id;
        // this.name = "Voter-" + id;
        this.pollingStation = pollingStation;
        this.votingBooth = votingBooth;
        this.exitPoll = exitPoll;
        // this.myVote = -1;
    }
    
    public static TVoter getInstance(int id, IPollingStation pollingStation, IVotingBooth votingBooth, IExitPoll exitPoll) {
        return new TVoter(id, pollingStation, votingBooth, exitPoll);
    }

    @Override
    public void run() {
        do {
            // Try to enter the polling station
            pollingStation.enterPollingStation(voterId);

            // Validate ID
            int response = 0;
            while (response == 0) {
                // Wait for ID validation
                response = pollingStation.waitIdValidation(voterId);
            }

            if (response == 1) {
                // Cast vote
                System.out.println("Voter " + voterId + " ID validation correct!");
                
                if (Math.random() < 0.4) {
                    System.out.println(voterId + " voted for candidate A");
                    votingBooth.voteA(voterId);
                    this.myVote = true; // 1 for A
                } else {
                    System.out.println(voterId + " voted for candidate b");
                    votingBooth.voteB(voterId);
                    this.myVote = false; // 0 for B
                }
                // Exit polling station 
                exitPoll.exitPollingStation(voterId, myVote);
            }

            // Reborn with probability or exit
            if (Math.random() < 0.5) {
                int newId = voterId + 1000; // Simple way to create new ID
                System.out.println("Voter " + voterId + " reborn as " + newId);
                voterId = newId;
            } else {
                System.out.println("Voter " + voterId + " reborn with same ID");
            }
                // Wait before trying again
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }

        } while ((pollingStation.isOpen()));
        System.out.println("Voter " + voterId + " terminated");
    }
}