package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.PollingStation.IPollingStation_all;

public class TClerk extends Thread {
    private final IPollingStation_all pollingStation;
    private final int numVoters; // To know when all voters have likely finished

    public TClerk(IPollingStation_all pollingStation, int numVoters) {
        this.pollingStation = pollingStation;
        this.numVoters = numVoters; // Needed for a more robust termination condition
    }

    @Override
    public void run() {
        try {

            // 1. Open Polling Station
            pollingStation.openPollingStation();
            // logger.logClerkState("PS_OPENED", "Polling station is now open."); // Logged by MPollingStation

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
                            break; // Exit if station closed and queue is confirmed empty
                        }
                    }
                } else {
                    // Should not happen if loop condition is pollingStation.isOpen() || pollingStation.numberVotersInQueue() > 0
                    break;
                }
                // Add a small delay if desired, or rely on blocking calls
                // Thread.sleep(50); // Example delay
            }

            // 3. Close Polling Station
            pollingStation.closePollingStation();
            // logger.logClerkState("PS_CLOSED", "Polling station is now closed."); // Logged by MPollingStation

            // 4. Print Final Results
            pollingStation.printFinalResults();
            // logger.logClerkState("RESULTS_PRINTED", "Final results have been printed."); // Logged by MPollingStation


        } catch (RemoteException e) {
            System.err.println("Clerk RemoteException: " + e.getMessage());
            // e.printStackTrace();
        } catch (Exception e) { // Catching generic Exception for other unexpected issues
            System.err.println("Clerk Exception: " + e.getMessage());
        }
    }
}
