package Threads;
import Interfaces.IPollingStation;
import Interfaces.IVotingBooth;
import Interfaces.IExitPoll;
import Logging.Logger;


public class TVoter implements Runnable {
    private int voterId;
    private final IPollingStation pollingStation;
    private final IVotingBooth votingBooth;
    // private final IExitPoll exitPoll;
    private final Logger logger;

    public TVoter(int id, IPollingStation pollingStation, IVotingBooth votingBooth, IExitPoll exitPoll, Logger logger) {
        this.voterId = id;
        // this.name = "Voter-" + id;
        this.pollingStation = pollingStation;
        this.votingBooth = votingBooth;
        // this.exitPoll = exitPoll;
        this.logger = logger;
        // this.myVote = -1;
    }

    @Override
    public void run() {
        do {
            // Try to enter the polling station
            pollingStation.enterPollingStation(voterId);
            logger.voterEnteringQueue(voterId);

            // Validate ID
            int response = 0;
            while (response == 0) {
                // Wait for ID validation
                response = pollingStation.waitIdValidation(voterId);
            }

            if(pollingStation.isOpen()){
                if (response == 1) {
                    // Cast vote
                    System.out.println("Voter " + voterId + " ID validation correct!");
                    if (Math.random() < 0.4) {
                        votingBooth.voteA();
                        logger.voterInBooth(voterId, true); // vote A
                    } else {
                        votingBooth.voteB();
                        logger.voterInBooth(voterId, false); // vote B
                    }
                } else{
                    System.out.println("Voter " + voterId + " ID validation incorrect!");
                }
            }
            
            
            logger.voterExiting(voterId, false);
            // Exit polling station
            // pollingStation.exitPollingStation(voterId);
            // logger.log("Voter " + voterId + " exited polling station");

            // Exit poll
            // if (myVote != -1 && exitPoll.approachVoter(voterId)) {
            //     boolean response = exitPoll.reportVote(voterId, myVote, true);
            //     logger.log("Voter " + voterId + " responded to exit poll: " + response);
            // }

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