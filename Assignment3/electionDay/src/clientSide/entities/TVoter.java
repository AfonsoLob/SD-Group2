package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.ExitPoll.IExitPoll_all;
import serverSide.interfaces.PollingStation.IPollingStation_all;

public class TVoter extends Thread {
    private int voterId;
    private final IPollingStation_all pollingStation;
    private final IExitPoll_all exitPoll;


    private TVoter(int voterId, IPollingStation_all pollingStation, IExitPoll_all exitPoll) {
        this.voterId = voterId;
        this.pollingStation = pollingStation;
        this.exitPoll = exitPoll;

    }

    public static TVoter getInstance(int voterId, IPollingStation_all pollingStation, IExitPoll_all exitPoll) {
        return new TVoter(voterId, pollingStation, exitPoll);
    }

    @Override
    public void run() {
        do {
            try {
                // Log initial state if needed, e.g., "STARTED_LIFECYCLE"

                // 1. Enter Polling Station
                if (!pollingStation.enterPollingStation(voterId)) {
                    // This case should ideally not happen if enterPollingStation blocks until successful or throws error
                    break; // End lifecycle if cannot enter
                }
                // logger.logVoterState(voterId, "ENTERED_PS", "Successfully entered polling station queue."); // Already logged by MPollingStation

                // 2. Wait for ID Validation
                boolean idValidated = pollingStation.waitIdValidation(voterId);
                // logger.logVoterState(voterId, "ID_VALIDATION_CHECKED", "ID Validated: " + idValidated); // Already logged by MPollingStation

                boolean votedForA = false;
                if (!idValidated) {
                    // No explicit leave method from polling station in this state in IPollingStation_Voter
                    // The voter just exits the system at this point as per typical logic.
                } else {
                    // 3. Vote (randomly A or B for this example)
                    votedForA = Math.random() < 0.5;
                    if (votedForA) {
                        pollingStation.voteA(voterId);
                        // logger.logVoterState(voterId, "VOTED_A_ATTEMPT", ""); // Logged by MPollingStation
                    } else {
                        pollingStation.voteB(voterId);
                        // logger.logVoterState(voterId, "VOTED_B_ATTEMPT", ""); // Logged by MPollingStation
                    }
                }

                // 4. Exit Polling Station (implicitly done after voting, now interact with Exit Poll)
                // The decision to respond to exit poll and the actual response is handled within MExitPoll
                // The 'response' parameter to exitPollingStation in IExitPoll_Voter indicates if the voter is even willing to be initially approached.
                // Let's assume all voters are initially willing to be approached for simplicity here.
                boolean willingToRespondInitially = true; 
                exitPoll.exitPollingStation(voterId, votedForA, willingToRespondInitially);

                // Add delay after exit poll before rebirth/reappearance
                try {
                    Thread.sleep(1800); // 1.8 seconds delay
                } catch (InterruptedException e) {
                    break;
                }

                // Reborn with probability or exit
                if (Math.random() < 0.5) {
                    int newId = voterId + 1000; // Simple way to create new ID for rebirth tracking
                    voterId = newId;
                }

                // Wait before trying again
                try {
                    Thread.sleep(2000); // 2 seconds delay before next cycle
                } catch (InterruptedException e) {
                    break;
                }

            } catch (RemoteException e) {
                System.err.println("Voter " + voterId + " RemoteException: " + e.getMessage());
                break;
            }
        } while (checkIfShouldContinue()); // Continue only if polling station is open
        
        System.out.println("Voter " + voterId + " lifecycle ended - polling station closed or error occurred");
    }
    
    private boolean checkIfShouldContinue() {
        try {
            return pollingStation.isOpen();
        } catch (RemoteException e) {
            System.err.println("Voter " + voterId + " could not check polling station status: " + e.getMessage());
            return false; // Stop if we can't communicate with the polling station
        }
    }
}
