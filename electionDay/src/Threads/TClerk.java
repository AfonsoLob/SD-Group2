package Threads;
import Interfaces.IExitPoll;
import Interfaces.IPollingStation;

// import Monitores.MPollingStation;


public class TClerk implements Runnable {

    private static TClerk instance;
    private final IPollingStation pollingStation;
    private final IExitPoll exitPoll;
    private final int maxVotes;

    

    private TClerk(int maxVotes, IPollingStation pollingStation, IExitPoll exitPoll) {
        this.pollingStation = pollingStation;
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
                boolean response = pollingStation.callNextVoter();
                if (response) {votes++;}
                Thread.sleep((long) (Math.random() * 5) + 5); // 5-10 ms

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
}
