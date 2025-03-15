package Threads;
import java.util.HashSet;

import Interfaces.IPollingStation;
// import Monitores.MPollingStation;


public class TClerk implements Runnable {

    // private static TClerk instance;
    // private MPollingStation pollingStation;
    private final IPollingStation pollingStation;
    private HashSet<Integer> validatedIDs;
    private final int maxVotes;
    
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

    public TClerk(int maxVotes, IPollingStation pollingStation) {
        this.pollingStation = pollingStation;
        this.validatedIDs = new HashSet<>();
        this.maxVotes = maxVotes;
    }

    @Override
    public void run() {
        System.out.println("Clerk running");
        pollingStation.openPollingStation();
        System.out.println("Clerk opened polling station");
        int votes = 0;
        while (votes < maxVotes) {
            // instance.openStation();
            try {
                System.out.println("Clerk calling next voter");
                int voterId = pollingStation.callNextVoter();
                boolean response = validateID(pollingStation, voterId);
                if (response) {votes++;}
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pollingStation.closePollingStation();
        System.out.println("Clerk closed polling station");

        while (pollingStation.stillVotersInQueue()) {
            try {
                System.out.println("Clerk calling next voter");
                int voterId = pollingStation.callNextVoter();
                validateID(pollingStation, voterId);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Clerk terminated");
    }

    // public static TClerk getInstance() {
    //     return new TClerk();
    // }

    private boolean validateID(IPollingStation pollingStation, int voterId) {
        // check if voterid is in hashset, if not add it and mark it as positive, and if yes mark it as negative
        // VoterRecord voterRecord= new VoterRecord(voterId, false);
        boolean response = false;
        if (!validatedIDs.contains(voterId)) {
            response = true;
            validatedIDs.add(voterId);
        }
        // System.out.println("Clerk validated voter " + voterId + " with response " + response);
        pollingStation.sendSignal(response);
        return response;
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
