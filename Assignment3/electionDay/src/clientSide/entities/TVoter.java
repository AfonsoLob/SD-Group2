package clientSide.entities;

import interfaces.ExitPoll.IExitPoll_Voter;
import interfaces.PollingStation.IPollingStation_Voter;
import java.rmi.RemoteException;

public class TVoter extends Thread {
    private int voterId;
    private final IPollingStation_Voter pollingStation;
    private final IExitPoll_Voter exitPoll;


    private TVoter(int voterId, IPollingStation_Voter pollingStation, IExitPoll_Voter exitPoll) {
        this.voterId = voterId;
        this.pollingStation = pollingStation;
        this.exitPoll = exitPoll;

    }

    public static TVoter getInstance(int voterId, IPollingStation_Voter pollingStation, IExitPoll_Voter exitPoll) {
        return new TVoter(voterId, pollingStation, exitPoll);
    }

    @Override
    public void run() {
        do {
            try {

                if (!pollingStation.enterPollingStation(voterId)) {
                    break;
                }

                boolean idValidated = pollingStation.waitIdValidation(voterId);

                boolean votedForA = false;
                if (!idValidated) {

                } else {
                    votedForA = Math.random() < 0.5;
                    if (votedForA) {
                        pollingStation.voteA(voterId);
                    } else {
                        pollingStation.voteB(voterId);
                    }
                }

                boolean willingToRespondInitially = true; 
                exitPoll.exitPollingStation(voterId, votedForA, willingToRespondInitially);

                try {
                    Thread.sleep(1800);
                } catch (InterruptedException e) {
                    break;
                }

                if (Math.random() < 0.5) {
                    int newId = voterId + 1000; 
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
