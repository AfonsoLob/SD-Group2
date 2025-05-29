package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.Logger.ILogger;
import serverSide.interfaces.PollingStation.IPollingStation_all;

public class TClerk extends Thread {
    private final IPollingStation_all pollingStation;
    private final ILogger logger;
    private final int numVoters; // To know when all voters have likely finished

    public TClerk(IPollingStation_all pollingStation, ILogger logger, int numVoters) {
        this.pollingStation = pollingStation;
        this.logger = logger;
        this.numVoters = numVoters; // Needed for a more robust termination condition
    }

    @Override
    public void run() {
        try {
            if (logger != null) logger.logClerkState("STARTED", "Lifecycle started.");

            // 1. Open Polling Station
            pollingStation.openPollingStation();
            // logger.logClerkState("PS_OPENED", "Polling station is now open."); // Logged by MPollingStation

            // 2. Process voters
            // The loop condition needs to be robust. 
            // It should continue if the station is open and there might be voters, 
            // or if voters are still in queue even if isOpen is becoming false.
            // A simple check: continue if station is open or queue is not empty.
            // More robust: consider total voters processed or a signal from a central coordinator.
            int votersProcessed = 0; // Keep track of processed voters for a potential exit condition
            // Loop while the polling station is open, or if it's closed but there are still voters in the queue.
            while (pollingStation.isOpen() || pollingStation.numberVotersInQueue() > 0) {
                if (pollingStation.numberVotersInQueue() > 0 || pollingStation.isOpen()) { // Double check to avoid calling on a closed empty station
                    // logger.logClerkState("CALLING_VOTER_ATTEMPT", "Attempting to call next voter."); // Logged by MPollingStation
                    boolean validationResult = pollingStation.callNextVoter(); // This blocks until a voter is available or station closes
                    // callNextVoter internally handles logging of calling and validation result
                    if (validationResult) {
                        // Successfully called and validated a voter
                        votersProcessed++;
                    } else {
                        // This might mean the voter called was not validated, or no voter was effectively called.
                        // If callNextVoter returns false when no voters are left and station is closing, this is fine.
                        // If pollingStation.isOpen() is false and queue is empty, loop will terminate.
                        if (!pollingStation.isOpen() && pollingStation.numberVotersInQueue() == 0) {
                            if (logger != null) logger.logClerkState("INFO", "Station closed and queue empty, preparing to exit loop.");
                            break; // Exit if station closed and queue is confirmed empty
                        }
                    }
                } else {
                    // Should not happen if loop condition is pollingStation.isOpen() || pollingStation.numberVotersInQueue() > 0
                    if (logger != null) logger.logClerkState("IDLE_CHECK", "Station closed and queue empty, exiting loop.");
                    break;
                }
                // Add a small delay if desired, or rely on blocking calls
                // Thread.sleep(50); // Example delay
            }
            if (logger != null) logger.logClerkState("VOTING_PHASE_ENDED", "All voters processed or station closed.");

            // 3. Close Polling Station
            pollingStation.closePollingStation();
            // logger.logClerkState("PS_CLOSED", "Polling station is now closed."); // Logged by MPollingStation

            // 4. Print Final Results
            pollingStation.printFinalResults();
            // logger.logClerkState("RESULTS_PRINTED", "Final results have been printed."); // Logged by MPollingStation

            if (logger != null) logger.logClerkState("FINISHED", "Lifecycle ended.");

        } catch (RemoteException e) {
            System.err.println("Clerk RemoteException: " + e.getMessage());
            // e.printStackTrace();
            try {
                if (logger != null) logger.logGeneral("Clerk encountered RemoteException: " + e.getMessage());
            } catch (RemoteException logEx) {
                System.err.println("Clerk: Critical error - cannot log remote exception: " + logEx.getMessage());
            }
        } catch (Exception e) { // Catching generic Exception for other unexpected issues
            System.err.println("Clerk Exception: " + e.getMessage());
            // e.printStackTrace();
            try {
                if (logger != null) logger.logGeneral("Clerk encountered Exception: " + e.getMessage());
            } catch (RemoteException logEx) {
                System.err.println("Clerk: Critical error - cannot log exception: " + logEx.getMessage());
            }
        }
    }
}

package clientSide.entities;

import clientSide.interfaces.ExitPoll.IExitPoll_Clerk;
import clientSide.interfaces.Pollingstation.IPollingStation_Clerk;

// import Monitores.MPollingStation;


public class TClerk implements Runnable {

    private static TClerk instance;
    private final IPollingStation_Clerk pollingStation;
    private final IExitPoll_Clerk exitPoll;
    private final int maxVotes;
    // private final IGUI_Common gui;

    

    private TClerk(int maxVotes, IPollingStation_Clerk pollingStation, IExitPoll_Clerk exitPoll) {
        this.pollingStation = pollingStation;
        this.maxVotes = maxVotes;
        this.exitPoll = exitPoll;
        // this.gui = Gui.getInstance();
    }

    @Override
    public void run() {
        pollingStation.openPollingStation();
        int votes = 0;
        while (votes < maxVotes) {
            // instance.openStation();
            try {
                System.out.println("Votes remaining: " + (maxVotes - votes));
                System.out.println("Clerk calling next voter");
                boolean response = pollingStation.callNextVoter();
                // System.out.println(response);
                
                if (response) {votes++;}
                
                // Apply speed factor - slower speed = longer wait time
                // float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round((Math.random() * 5 + 5) / 1);
                // long waitTime = Math.round((Math.random() * 5 + 5) / speedFactor);
                Thread.sleep(waitTime);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pollingStation.closePollingStation();

        int stillVotersInQueue = pollingStation.numberVotersInQueue(); 

        System.out.println("Day ended but there are still " + stillVotersInQueue + " voters inside");

        exitPoll.closeIn(stillVotersInQueue);

        for (int i = 0; i < stillVotersInQueue; i++) {
            try {
                System.out.println("Clerk calling next voter");
                pollingStation.callNextVoter();
                
                // Apply speed factor here too
                // float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round((Math.random() * 5 + 5) / 1);
                // long waitTime = Math.round((Math.random() * 5 + 5) / speedFactor);
                Thread.sleep(waitTime);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Clerk terminated");
    }

    public static TClerk getInstance(int maxVotes, IPollingStation_Clerk pollingStation, IExitPoll_Clerk exitPoll) {
        if (instance == null) {
            instance = new TClerk(maxVotes, pollingStation, exitPoll);
        }
        return instance;
    }
}
