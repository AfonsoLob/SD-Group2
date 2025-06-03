package clientSide.entities;

import java.rmi.RemoteException;
import serverSide.interfaces.PollingStation.IPollingStation_Clerk;
import serverSide.interfaces.ExitPoll.IExitPoll_Clerk;

public class TClerk extends Thread {
    private final IPollingStation_Clerk pollingStation;

    private TClerk(IPollingStation_Clerk pollingStation) {
        this.pollingStation = pollingStation;
    }

    public static TClerk getInstance(IPollingStation_Clerk pollingStation) {
        return new TClerk(pollingStation);
    }

    @Override
    public void run() {
        try {
            // 1. Open Polling Station
            pollingStation.openPollingStation();
            
            // Get maxVotes from the polling station
            int maxVotes = pollingStation.getMaxVotes();
            
            int votes = 0;
            // Main voting loop - process votes until maxVotes limit is reached
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
                    e.printStackTrace();
                }
            }
            
            // 2. Close Polling Station after reaching vote limit
            pollingStation.closePollingStation();
            
            // 3. Handle remaining voters in queue after closing
            int stillVotersInQueue = pollingStation.numberVotersInQueue();
            System.out.println("Day ended but there are still " + stillVotersInQueue + " voters inside");
            

            
            // Process remaining voters that were already in the queue
            for (int i = 0; i < stillVotersInQueue; i++) {
                try {
                    System.out.println("Clerk calling next voter (after closing)");
                    pollingStation.callNextVoter();
                    
                    Thread.sleep(100);
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 4. Print Final Results
            
            System.out.println("Clerk terminated");

        } catch (RemoteException e) {
            System.err.println("Clerk RemoteException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Clerk Exception: " + e.getMessage());
        }
    }
}
