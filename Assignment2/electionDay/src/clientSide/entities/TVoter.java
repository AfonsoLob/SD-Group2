package clientSide.entities;
import clientSide.interfaces.ExitPoll.IExitPoll_Voter;
import clientSide.interfaces.Pollingstation.IPollingStation_Voter;
// import serverSide.GUI.Gui;

public class TVoter implements Runnable {
    private int voterId;
    private final IPollingStation_Voter pollingStation;
    private final IExitPoll_Voter exitPoll;
    private boolean myVote;
    // private final IGUI_Common gui;

    private TVoter(int id, IPollingStation_Voter pollingStation, IExitPoll_Voter exitPoll) {
        this.voterId = id;
        this.pollingStation = pollingStation;
        this.exitPoll = exitPoll;
        // this.gui = Gui.getInstance();
    }
    
    public static TVoter getInstance(int id, IPollingStation_Voter pollingStation, IExitPoll_Voter exitPoll) {
        return new TVoter(id, pollingStation, exitPoll);
    }

    @Override
    public void run() {
        do {
            // Try to enter the polling station
            pollingStation.enterPollingStation(voterId);

            //             try {
            //                     // Add delay between entering and validation to make stages more visible
            //     // This gives time to see the voter in the queue stage
            //     float speedFactor = gui.getSimulationSpeed();
            //     long waitTime = Math.round(1500 / speedFactor);  // 1.5 seconds at normal speed
            //     Thread.sleep(waitTime);
            // } catch (InterruptedException e) {
            //     break;
            // }
        
            // Validate ID    
            boolean response = pollingStation.waitIdValidation(voterId);
            
            if (response) {
                try {
                    // Add delay after validation before voting
                    // float speedFactor = gui.getSimulationSpeed();
                    long waitTime = Math.round(1200 / 1);  // 1.2 seconds at normal speed
                    // long waitTime = Math.round(1200 / speedFactor);  // 1.2 seconds at normal speed
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    break;
                }
                
                // Cast vote
// System.out.println("Voter " + voterId + " ID validation correct!");
                
                if (Math.random() < 0.4) {
// System.out.println(voterId + " voted for candidate A");
                    pollingStation.voteA(voterId);
                    this.myVote = true; // 1 for A
                } else {
// System.out.println(voterId + " voted for candidate b");
                    pollingStation.voteB(voterId);
                    this.myVote = false; // 0 for B
                }

                try {
                    // Add delay after voting before exit poll
                    // float speedFactor = gui.getSimulationSpeed();
                    long waitTime = Math.round(1500 / 1);  // 1.5 seconds at normal speed
                    // long waitTime = Math.round(1500 / speedFactor);  // 1.5 seconds at normal speed
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    break;
                }
            }

            // Exit polling and go to exit poll
            exitPoll.exitPollingStation(voterId, myVote, response);

            try {
                // Add delay after exit poll before rebirth/reappearance
                // float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round(1800 / 1);  // 1.8 seconds at normal speed
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                break;
            }

            // Reborn with probability or exit
            if (Math.random() < 0.5) {
                int newId = voterId + 1000; // Simple way to create new ID
// System.out.println("Voter " + voterId + " reborn as " + newId);
                voterId = newId;
            } else {
// System.out.println("Voter " + voterId + " reborn with same ID");
            }

            // Wait before trying again - Apply speed control
            try {
                // float speedFactor = gui.getSimulationSpeed();
                long waitTime = Math.round(2000 / 1);  // Increase to 2 seconds at normal speed
                // long waitTime = Math.round(2000 / speedFactor);  // Increase to 2 seconds at normal speed
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                break;
            }

        } while ((pollingStation.isOpen()));
// System.out.println("Voter " + voterId + " terminated");
    }
}