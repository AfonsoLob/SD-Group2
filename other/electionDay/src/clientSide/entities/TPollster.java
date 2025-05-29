package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.ExitPoll.IExitPoll_all;
import serverSide.interfaces.Logger.ILogger;

public class TPollster extends Thread {
    private final IExitPoll_all exitPoll;
    private final ILogger logger;
    // numVoters could be used to estimate when to stop, but relying on exitPoll.isOpen() is more direct.

    public TPollster(IExitPoll_all exitPoll, ILogger logger) {
        this.exitPoll = exitPoll;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            if (logger != null) logger.logPollsterState("STARTED", "Lifecycle started.");

            // The pollster continues to inquire as long as the exit poll is open 
            // or potentially if there are still voters being processed by it.
            // MExitPoll.inquire() should block until a vote is registered or the poll closes.
            while (exitPoll.isOpen()) { // Condition might need to be more robust depending on MExitPoll behavior
                // logger.logPollsterState("INQUIRING", "Attempting to inquire next vote."); // Logged by MExitPoll
                exitPoll.inquire(); // This method handles the logic of waiting and processing a vote
                                  // It will also handle its own logging for vote registration.
                // If inquire() throws an exception when poll closes while waiting, it will be caught below.
            }
            
            if (logger != null) logger.logPollsterState("INQUIRY_PHASE_ENDED", "Exit poll closed or no more voters.");

            // After the loop, it means the exit poll is no longer open for new voters.
            // The pollster should then print the final results from the exit poll.
            exitPoll.printExitPollResults();
            // logger.logPollsterState("RESULTS_PRINTED", "Final exit poll results printed."); // Logged by MExitPoll

            if (logger != null) logger.logPollsterState("FINISHED", "Lifecycle ended.");

        } catch (RemoteException e) {
            // Specific handling if exitPoll.isOpen() or inquire() throws RemoteException when poll is closing
            if (e.getMessage() != null && e.getMessage().contains("closed")) { // Example check
                if (logger != null) try { logger.logPollsterState("INFO", "Exit poll closed during operation: " + e.getMessage()); } catch (RemoteException logEx) { /* ignore */ }
            } else {
                System.err.println("Pollster RemoteException: " + e.getMessage());
                // e.printStackTrace();
                try {
                    if (logger != null) logger.logGeneral("Pollster encountered RemoteException: " + e.getMessage());
                } catch (RemoteException logEx) {
                    System.err.println("Pollster: Critical error - cannot log remote exception: " + logEx.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Pollster Exception: " + e.getMessage());
            // e.printStackTrace();
            try {
                if (logger != null) logger.logGeneral("Pollster encountered Exception: " + e.getMessage());
            } catch (RemoteException logEx) {
                System.err.println("Pollster: Critical error - cannot log exception: " + logEx.getMessage());
            }
        }
    }
}
