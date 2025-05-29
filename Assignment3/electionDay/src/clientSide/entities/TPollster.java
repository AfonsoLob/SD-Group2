package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.ExitPoll.IExitPoll_all;

public class TPollster extends Thread {
    private final IExitPoll_all exitPoll;
    // numVoters could be used to estimate when to stop, but relying on exitPoll.isOpen() is more direct.

    public TPollster(IExitPoll_all exitPoll) {
        this.exitPoll = exitPoll;
    }

    @Override
    public void run() {
        try {


            while (exitPoll.isOpen()) { // Condition might need to be more robust depending on MExitPoll behavior
                // logger.logPollsterState("INQUIRING", "Attempting to inquire next vote."); // Logged by MExitPoll
                exitPoll.inquire(); // This method handles the logic of waiting and processing a vote
                                  // It will also handle its own logging for vote registration.
                // If inquire() throws an exception when poll closes while waiting, it will be caught below.
            }
            
            // After the loop, it means the exit poll is no longer open for new voters.
            // The pollster should then print the final results from the exit poll.
            exitPoll.printExitPollResults();


        } catch (RemoteException e) {
            // Specific handling if exitPoll.isOpen() or inquire() throws RemoteException when poll is closing
            if (e.getMessage() != null && e.getMessage().contains("closed")) { // Example check
            } else {
                System.err.println("Pollster RemoteException: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Pollster Exception: " + e.getMessage());
        }
    }
}
