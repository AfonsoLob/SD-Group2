import GUI.Gui;
import Interfaces.IExitPoll;
import Interfaces.IPollingStation;
import Interfaces.IVotingBooth;
import Logging.Logger;
import Monitores.MPollingStation;
import Monitores.MVotingBooth;
import Monitores.MExitPoll;
import Threads.TClerk;
import Threads.TPollster;
import Threads.TVoter;
import java.util.ArrayList;
import java.util.List;


public class App {
    // Move all simulation variables to class level for access across methods
    private static int maxVoters = 5;
    private static int maxCapacity = 2;
    private static int maxVotes = 10;
    private static Logger logger;
    private static IPollingStation pollingStation;
    private static IVotingBooth votingBooth;
    private static IExitPoll exitPoll;
    private static List<TVoter> voters;
    private static List<Thread> voterThreads;
    private static TClerk clerk;
    private static Thread clerkThread;
    private static TPollster pollster;
    private static Thread pollsterThread;
    private static Thread guiUpdateThread;
    private static boolean simulationRunning = false;
    
    public static void main(String[] args) throws Exception {
        System.out.println("Election Simulation initialized");
        
        // Initialize components
        logger = Logger.getInstance(maxVoters, maxCapacity, maxVotes);
        pollingStation = MPollingStation.getInstance(maxCapacity, logger);
        votingBooth = MVotingBooth.getInstance();
        exitPoll = MExitPoll.getInstance(50,logger);
        
        // Launch the GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Gui.window();
                // Set up the callback for the start button
                Gui.setSimulationStarter(() -> {
                    try {
                        startSimulation();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    
    private static void startSimulation() throws Exception {
        System.out.println("ElectionSimulation started");
        simulationRunning = true;
        
        // Create voters
        voters = new ArrayList<>();
        voterThreads = new ArrayList<>();
        
        for (int i = 0; i < maxVoters; i++) {
            TVoter voter = TVoter.getInstance(i, pollingStation, votingBooth, exitPoll, logger);
            voters.add(voter);
        }
        
        // Create clerk
        clerk = TClerk.getInstance(maxVotes, pollingStation, exitPoll,logger);

        // Create pollster
        pollster = TPollster.getInstance(exitPoll);
        
        // Start the periodic GUI update thread with more comprehensive updates
        guiUpdateThread = new Thread(() -> {
            try {
                while (simulationRunning) {
                    // Update GUI with various information from logger
                    Gui.updateFromLogger(logger);
                    Gui.updateQueueAndBoothInfo(
                        logger.getCurrentQueueSize(),
                        logger.getCurrentVoterInBooth()
                    );
                    Thread.sleep(200); // Update every 200ms for more responsive UI
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        guiUpdateThread.setDaemon(true);
        guiUpdateThread.start();
        
        // Run threads
        clerkThread = new Thread(clerk);
        clerkThread.start();

        pollsterThread = new Thread(pollster);
        pollsterThread.start();
        
        for (TVoter voter : voters) {
            Thread voterThread = new Thread(voter);
            voterThreads.add(voterThread);
            voterThread.start();
        }
        
        // Use a separate thread to wait for completion
        new Thread(() -> {
            try {
                clerkThread.join();
                pollsterThread.join();
                for (Thread thread : voterThreads) {
                    thread.join();
                }
                
                // Simulation completed
                simulationRunning = false;
                votingBooth.printFinalResults();
                System.out.println("ElectionSimulation finished");
                logger.saveCloseFile();
                
                // Final GUI update after simulation is complete
                Gui.updateFromLogger(logger);
                Gui.updateQueueAndBoothInfo(0, ""); // Clear queue and booth
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
