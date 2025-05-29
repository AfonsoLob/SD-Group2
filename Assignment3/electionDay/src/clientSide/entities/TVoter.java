package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.ExitPoll.IExitPoll_all;
import serverSide.interfaces.Logger.ILogger;
import serverSide.interfaces.PollingStation.IPollingStation_all;

public class TVoter extends Thread {
    private final int voterId;
    private final IPollingStation_all pollingStation;
    private final IExitPoll_all exitPoll;
    private final ILogger logger; // General logger for simulation events

    public TVoter(int voterId, IPollingStation_all pollingStation, IExitPoll_all exitPoll, ILogger logger) {
        this.voterId = voterId;
        this.pollingStation = pollingStation;
        this.exitPoll = exitPoll;
        this.logger = logger;
        // You might want to log voter creation here if desired
        // try {
        //     if (this.logger != null) this.logger.logVoterState(voterId, "CREATED", "");
        // } catch (RemoteException e) {
        //     System.err.println("Voter " + voterId + ": Error logging creation: " + e.getMessage());
        // }
    }

    @Override
    public void run() {
        try {
            // Log initial state if needed, e.g., "STARTED_LIFECYCLE"
            if (logger != null) logger.logVoterState(voterId, "STARTED", "Lifecycle started.");

            // 1. Enter Polling Station
            if (!pollingStation.enterPollingStation(voterId)) {
                // This case should ideally not happen if enterPollingStation blocks until successful or throws error
                if (logger != null) logger.logVoterState(voterId, "FAILED_ENTER_PS", "Could not enter polling station.");
                return; // End lifecycle if cannot enter
            }
            // logger.logVoterState(voterId, "ENTERED_PS", "Successfully entered polling station queue."); // Already logged by MPollingStation

            // 2. Wait for ID Validation
            boolean idValidated = pollingStation.waitIdValidation(voterId);
            // logger.logVoterState(voterId, "ID_VALIDATION_CHECKED", "ID Validated: " + idValidated); // Already logged by MPollingStation

            if (!idValidated) {
                if (logger != null) logger.logVoterState(voterId, "ID_INVALID", "ID not validated, leaving.");
                // No explicit leave method from polling station in this state in IPollingStation_Voter
                // The voter just exits the system at this point as per typical logic.
            } else {
                if (logger != null) logger.logVoterState(voterId, "ID_VALID", "Proceeding to vote.");
                // 3. Vote (randomly A or B for this example)
                boolean votedForA = Math.random() < 0.5;
                if (votedForA) {
                    pollingStation.voteA(voterId);
                    // logger.logVoterState(voterId, "VOTED_A_ATTEMPT", ""); // Logged by MPollingStation
                } else {
                    pollingStation.voteB(voterId);
                    // logger.logVoterState(voterId, "VOTED_B_ATTEMPT", ""); // Logged by MPollingStation
                }

                // 4. Exit Polling Station (implicitly done after voting, now interact with Exit Poll)
                // The decision to respond to exit poll and the actual response is handled within MExitPoll
                // The 'response' parameter to exitPollingStation in IExitPoll_Voter indicates if the voter is even willing to be initially approached.
                // Let's assume all voters are initially willing to be approached for simplicity here.
                boolean willingToRespondInitially = true; 
                exitPoll.exitPollingStation(voterId, votedForA, willingToRespondInitially);
                if (logger != null) logger.logVoterState(voterId, "EXIT_POLL_INTERACTION_DONE", "Finished interaction with exit poll.");
            }

            if (logger != null) logger.logVoterState(voterId, "FINISHED", "Lifecycle ended.");

        } catch (RemoteException e) {
            System.err.println("Voter " + voterId + " RemoteException: " + e.getMessage());
            // e.printStackTrace(); // Consider if stack trace is needed for client-side errors
            // Log error to central logger if possible
            try {
                if (logger != null) logger.logGeneral("Voter " + voterId + " encountered RemoteException: " + e.getMessage());
            } catch (RemoteException logEx) {
                System.err.println("Voter " + voterId + ": Critical error - cannot log remote exception: " + logEx.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Voter " + voterId + " Exception: " + e.getMessage());
            // e.printStackTrace();
            try {
                if (logger != null) logger.logGeneral("Voter " + voterId + " encountered Exception: " + e.getMessage());
            } catch (RemoteException logEx) {
                System.err.println("Voter " + voterId + ": Critical error - cannot log exception: " + logEx.getMessage());
            }
        }
    }
}
