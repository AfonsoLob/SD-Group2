package Threads;
import java.util.HashSet;


public class TClerk implements Runnable {

    private static TClerk instance;
    private MPollingStation pollingStation;
    private HashSet<VoterRecord> validatedIDs = new HashSet<>();
    
    // Class to store voter ID and validation status
    private static class VoterRecord {
        private final int voterId;
        private boolean isValidated;
        
        public VoterRecord(int voterId, boolean isValidated) {
            this.voterId = voterId;
            this.isValidated = isValidated;
        }

        public void toggleValididy() {
            isValidated = !isValidated;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            VoterRecord that = (VoterRecord) obj;
            return voterId == that.voterId;
        }
        
        @Override
        public int hashCode() {
            return Integer.hashCode(voterId);
        }
    }

    @Override
    public void run() {
        System.out.println("Clerk running");
        
        while (true) {
            // instance.openStation();
            try {
                int voterId = pollingStation.callNextVoter();
                instance.validateID(voterId);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // instance.closeStation();
        }
    }

    private TClerk() {
        
    }

    public static TClerk getInstance() {
        return new TClerk();
    }

    public void validateID(MPollingStation pollingStation, TVoter voter) {
        // check if voterid is in hashset, if not add it and mark it as positive, and if yes mark it as negative
        VoterRecord voterRecord= new VoterRecord(voter.getID(), false);
        if (validatedIDs.contains(voterRecord)) {
            voterRecord.toggleValididy();
        } else {
            validatedIDs.add(voterRecord);
        }
        pollingStation.sendSignal();
    }

    // public void countVote(MPollingStation pollingStation, TVoter voter) {
    //     pollingStation
    // }

    public void closeStation( MPollingStation pollingStation) {
        pollingStation.closePollingStation();
    }

    public void openStation( MPollingStation pollingStation) {
        pollingStation.openPollingStation();
    }


}
