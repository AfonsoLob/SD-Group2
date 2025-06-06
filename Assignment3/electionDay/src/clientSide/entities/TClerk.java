package clientSide.entities;

import interfaces.PollingStation.IPollingStation_Clerk;
import interfaces.ExitPoll.IExitPoll_Clerk;
import java.rmi.RemoteException;

public class TClerk extends Thread {
    private final IPollingStation_Clerk pollingStation;
    private final IExitPoll_Clerk exitPoll;

    private TClerk(IPollingStation_Clerk pollingStation, IExitPoll_Clerk exitPoll) {
        this.pollingStation = pollingStation;
        this.exitPoll = exitPoll;
    }

    public static TClerk getInstance(IPollingStation_Clerk pollingStation, IExitPoll_Clerk exitPoll) {
        return new TClerk(pollingStation, exitPoll);
    }

    @Override
    public void run() {
        try {
            System.out.println("Clerk: Starting clerk operations...");
            
            // Add retry logic for opening polling station
            int maxRetries = 5; // Increase retries
            boolean success = false;
            
            for (int retry = 0; retry < maxRetries && !success; retry++) {
                try {
                    if (retry > 0) {
                        System.out.println("Clerk: Retrying polling station operations (attempt " + (retry + 1) + ")...");
                        Thread.sleep(2000); // Wait 2 seconds between retries
                    }
                    
                    System.out.println("Clerk: Opening polling station...");
                    pollingStation.openPollingStation();
                    System.out.println("Clerk: Polling station opened successfully");
                    success = true;
                } catch (RemoteException e) {
                    System.err.println("Clerk: Failed to open polling station (attempt " + (retry + 1) + "): " + e.getMessage());
                    if (retry == maxRetries - 1) {
                        throw e; // Re-throw on final attempt
                    }
                    // For "no such object in table" errors, wait longer as it might be a timing issue
                    if (e.getMessage().contains("no such object in table")) {
                        System.out.println("Clerk: Detected stale object reference, waiting longer before retry...");
                        try {
                            Thread.sleep(10000); // Wait 10 seconds for stale object errors
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            System.out.println("Clerk: Getting max votes...");
            int maxVotes = pollingStation.getMaxVotes();
            System.out.println("Clerk: Max votes = " + maxVotes);
            
            int votes = 0;
            while (votes < maxVotes) {
                try {
                    System.out.println("Votes remaining: " + (maxVotes - votes));
                    System.out.println("Clerk calling next voter");
                    boolean response = pollingStation.callNextVoter();
                    
                    if (response) {
                        votes++;
                    }
                    
                    // Add small delay between calls
                    Thread.sleep(100);
                    
                } catch (InterruptedException e) {
                }
            }
            
            pollingStation.closePollingStation();
            
            int stillVotersInQueue = pollingStation.numberVotersInQueue();
            System.out.println("Day ended but there are still " + stillVotersInQueue + " voters inside");
            

            
            for (int i = 0; i < stillVotersInQueue; i++) {
                try {
                    System.out.println("Clerk calling next voter (after closing)");
                    pollingStation.callNextVoter();
                    
                    Thread.sleep(100);
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Close the exit poll after all remaining voters have been processed
            if (exitPoll != null) {
                try {
                    System.out.println("Clerk closing exit poll with " + stillVotersInQueue + " remaining voters");
                    exitPoll.closeIn(stillVotersInQueue);
                    System.out.println("Clerk successfully closed exit poll");
                } catch (RemoteException e) {
                    System.err.println("Clerk failed to close exit poll: " + e.getMessage());
                }
            } else {
                System.err.println("Clerk: exitPoll reference is null, cannot close exit poll");
            }
            
            System.out.println("Clerk terminated");

        } catch (RemoteException e) {
            System.err.println("Clerk RemoteException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Clerk Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
