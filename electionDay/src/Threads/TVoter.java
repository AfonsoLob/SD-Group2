package Threads;
import Interfaces.IPollingStation;
import Interfaces.IVotingBooth;
import Interfaces.IExitPoll;
import Logging.Logger;


public class TVoter implements Runnable {
    private int voterId;
    private final IPollingStation pollingStation;
    private final IVotingBooth votingBooth;
    private final IExitPoll exitPoll;
    private final Logger logger;
    private int myVote;
    private String name;

    public TVoter(int id, IPollingStation pollingStation, IVotingBooth votingBooth, IExitPoll exitPoll, Logger logger) {
        this.voterId = id;
        this.name = "Voter-" + id;
        this.pollingStation = pollingStation;
        this.votingBooth = votingBooth;
        this.exitPoll = exitPoll;
        this.logger = logger;
        this.myVote = -1;
    }

    public void run() {
        while (pollingStation.isOpen()) {
            // Try to enter the polling station
            if (pollingStation.enterPollingStation(voterId)) {
                logger.log("Voter " + voterId + " entered polling station");

                // Validate ID
                boolean idValid = pollingStation.waitIdValidation(voterId);
                logger.log("Voter " + voterId + " ID validation: " + (idValid ? "valid" : "invalid"));

                if (idValid) {
                    // Cast vote
                    myVote = votingBooth.castVote(voterId);
                    logger.log("Voter " + voterId + " voted for candidate " + myVote);
                }

                

                // Exit polling station
                // pollingStation.exitPollingStation(voterId);
                // logger.log("Voter " + voterId + " exited polling station");

                // Exit poll
                // if (myVote != -1 && exitPoll.approachVoter(voterId)) {
                //     logger.log("Voter " + voterId + " approached for exit poll");
                //     boolean response = exitPoll.reportVote(voterId, myVote, true);
                //     logger.log("Voter " + voterId + " responded to exit poll: " + response);
                // }

                // Reborn with probability or exit
                if (Math.random() < 0.3) {
                    // 30% chance to be "reborn" with new ID
                    int newId = voterId + 1000; // Simple way to create new ID
                    logger.log("Voter " + voterId + " reborn as " + newId);
                    voterId = newId;
                    name = "Voter-" + newId;
                    myVote = -1;
                } else {
                    break; // Exit the simulation
                }
            }

            // Wait before trying again
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}