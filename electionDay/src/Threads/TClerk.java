package Threads;
import java.util.HashSet;

import Interfaces.IPollingStation;
// import Monitores.MPollingStation;


public class TClerk implements Runnable {

    // private static TClerk instance;
    // private MPollingStation pollingStation;
    private final IPollingStation pollingStation;
    private HashSet<Integer> validatedIDs;
    
    // Class to store voter ID and validation status
    // private static class VoterRecord {
    //     private final int voterId;
    //     private boolean isValidated;
        
    //     public VoterRecord(int voterId, boolean isValidated) {
    //         this.voterId = voterId;
    //         this.isValidated = isValidated;
    //     }

    //     public void toggleValididy() {
    //         isValidated = !isValidated;
    //     }
        
    //     @Override
    //     public boolean equals(Object obj) {
    //         if (this == obj) return true;
    //         if (obj == null || getClass() != obj.getClass()) return false;
    //         VoterRecord that = (VoterRecord) obj;
    //         return voterId == that.voterId;
    //     }
        
    //     @Override
    //     public int hashCode() {
    //         return Integer.hashCode(voterId);
    //     }
    // }

    public TClerk(int id, IPollingStation pollingStation) {
        this.pollingStation = pollingStation;
        this.validatedIDs = new HashSet<>();
    }

    @Override
    public void run() {
        System.out.println("Clerk running");
        pollingStation.openPollingStation();
        System.out.println("Clerk opened polling station");
        while (true) {
            // instance.openStation();
            try {
                System.out.println("Clerk calling next voter");
                int voterId = pollingStation.callNextVoter();
                validateID(pollingStation, voterId);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // instance.closeStation();
        }
    }

    // public static TClerk getInstance() {
    //     return new TClerk();
    // }

    private void validateID(IPollingStation pollingStation, int voterId) {
        // check if voterid is in hashset, if not add it and mark it as positive, and if yes mark it as negative
        // VoterRecord voterRecord= new VoterRecord(voterId, false);
        boolean response = false;
        if (!validatedIDs.contains(voterId)) {
            response = true;
            validatedIDs.add(voterId);
        }
        System.out.println("Clerk validated voter " + voterId + " with response " + response);
        pollingStation.sendSignal(response);
    }

    // public void countVote(MPollingStation pollingStation, TVoter voter) {
    //     pollingStation
    // }

    // public void closeStation( MPollingStation pollingStation) {
    //     pollingStation.closePollingStation();
    // }

    // public void openStation( MPollingStation pollingStation) {
    //     pollingStation.openPollingStation();
    // }


}
