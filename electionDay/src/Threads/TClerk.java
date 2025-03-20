package Threads;
import Interfaces.IExitPoll;
import Interfaces.IPollingStation;
import java.util.HashSet;

// import Monitores.MPollingStation;


public class TClerk implements Runnable {

    private static TClerk instance;
    private final IPollingStation pollingStation;
    private final IExitPoll exitPoll;
    private HashSet<Integer> validatedIDs;
    private final int maxVotes;

    

    private TClerk(int maxVotes, IPollingStation pollingStation, IExitPoll exitPoll) {
        this.pollingStation = pollingStation;
        this.validatedIDs = new HashSet<>();
        this.maxVotes = maxVotes;
        this.exitPoll = exitPoll;
    }

    @Override
    public void run() {
        System.out.println("Clerk running");
        pollingStation.openPollingStation();
        int votes = 0;
        while (votes < maxVotes) {
            // instance.openStation();
            try {
                System.out.println("Clerk calling next voter");
                int voterId = pollingStation.callNextVoter();
                boolean response = validateID(pollingStation, voterId);
                if (response) {votes++;}
                Thread.sleep((long) (Math.random() * 5) + 5); // 5-10 ms

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pollingStation.closePollingStation();

        int stillVotersInQueue = pollingStation.numberVotersInQueue(); 

        System.out.println("Day ended but there are still" + stillVotersInQueue + " voters inside");

        exitPoll.closeIn(stillVotersInQueue);

        for (int i = 0; i < stillVotersInQueue; i++) {
            try {
                System.out.println("Clerk calling next voter");
                int voterId = pollingStation.callNextVoter();
                validateID(pollingStation, voterId);
                // System.out.println("Clerk validated ID");
                Thread.sleep((long) (Math.random() * 5) + 5); // 5-10 ms

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Clerk terminated");
    }

    public static TClerk getInstance(int maxVotes, IPollingStation pollingStation, IExitPoll exitPoll) {
        if (instance == null) {
            instance = new TClerk(maxVotes, pollingStation, exitPoll);
        }
        return instance;
    }

    private boolean validateID(IPollingStation pollingStation, int voterId) {
        // check if voterid is in hashset, if not add it and mark it as positive, and if yes mark it as negative
        // VoterRecord voterRecord= new VoterRecord(voterId, false);
        boolean response = false;
        if (!validatedIDs.contains(voterId) && pollingStation.isOpen()) {
            response = true;
            validatedIDs.add(voterId);
        }
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
