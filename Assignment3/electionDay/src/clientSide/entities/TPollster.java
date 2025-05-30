package clientSide.entities;

import java.rmi.RemoteException;
import interfaces.ExitPoll.IExitPoll_all;

public class TPollster extends Thread {
    private final IExitPoll_all exitPoll;
    // numVoters could be used to estimate when to stop, but relying on exitPoll.isOpen() is more direct.

    public TPollster(IExitPoll_all exitPoll) {
        this.exitPoll = exitPoll;
    }

    @Override
    public void run() {
        try {
            while (exitPoll.isOpen()) {
                exitPoll.inquire();
                Thread.sleep(1000); // Wait a bit before next inquiry
            }
            exitPoll.printExitPollResults();
        } catch (Exception e) {
            System.err.println("TPollster: Error in run: " + e.getMessage());
        }
    }
}
