package clientSide.entities;
import clientSide.interfaces.ExitPoll.IExitPoll_Clerk;
import clientSide.interfaces.GUI.IGUI_Common;
import clientSide.interfaces.Pollingstation.IPollingStation_Clerk;
import serverSide.GUI.Gui;

// import Monitores.MPollingStation;


public class TClerk implements Runnable {

    private static TClerk instance;
    private final IPollingStation_Clerk pollingStation;
    private final IExitPoll_Clerk exitPoll;
    private final int maxVotes;
    private final IGUI_Common gui;

    

    private TClerk(int maxVotes, IPollingStation_Clerk pollingStation, IExitPoll_Clerk exitPoll) {
        this.pollingStation = pollingStation;
        this.maxVotes = maxVotes;
        this.exitPoll = exitPoll;
        this.gui = Gui.getInstance();
    }

    @Override
    public void run() {
                pollingStation.openPollingStation();
        int votes = 0;
        while (votes < maxVotes) {
            // instance.openStation();
            try {
                System.out.println("Clerk calling next voter");
                boolean response = pollingStation.callNextVoter();
                if (response) {votes++;}
                
                // Apply speed factor - slower speed = longer wait time
                float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round((Math.random() * 5 + 5) / speedFactor);
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
                float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round((Math.random() * 5 + 5) / speedFactor);
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
