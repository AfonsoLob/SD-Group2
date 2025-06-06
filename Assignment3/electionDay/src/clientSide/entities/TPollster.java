package clientSide.entities;

import interfaces.ExitPoll.IExitPoll_Pollster;
import java.rmi.RemoteException;

public class TPollster extends Thread {
    private final IExitPoll_Pollster exitPoll;

    private TPollster(IExitPoll_Pollster exitPoll) {
        this.exitPoll = exitPoll;
    }

    public static TPollster getInstance(IExitPoll_Pollster exitPoll) {
        return new TPollster(exitPoll);
    }

    @Override
    public void run() {
        try {


            while (exitPoll.isOpen()) {
                exitPoll.inquire();
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
