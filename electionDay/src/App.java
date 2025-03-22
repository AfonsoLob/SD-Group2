import java.util.ArrayList;
import java.util.List;

import GUI.Gui;
import Interfaces.ExitPoll.IExitPoll_Clerk;
import Interfaces.ExitPoll.IExitPoll_Pollster;
import Interfaces.ExitPoll.IExitPoll_Voter;
import Interfaces.ExitPoll.IExitPoll_all;
import Interfaces.GUI.IGUI_all;
import Interfaces.Logger.ILogger_ExitPoll;
import Interfaces.Logger.ILogger_GUI;
import Interfaces.Logger.ILogger_PollingStation;
import Interfaces.Logger.ILogger_all;
import Interfaces.Pollingstation.IPollingStation_Clerk;
import Interfaces.Pollingstation.IPollingStation_Voter;
import Interfaces.Pollingstation.IPollingStation_all;
import Logging.Logger;
import Monitores.MExitPoll;
import Monitores.MPollingStation;
import Threads.TClerk;
import Threads.TPollster;
import Threads.TVoter;


public class App {
    // Move all simulation variables to class level for access across methods
    private static int maxVoters = 5;
    private static int maxCapacity = 2;
    private static int maxVotes = 10;
    private static ILogger_all logger;
    private static IPollingStation_all pollingStation;
    private static IExitPoll_all exitPoll;
    private static IGUI_all gui;
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
        pollingStation = MPollingStation.getInstance(maxCapacity, (ILogger_PollingStation)logger);
        exitPoll = MExitPoll.getInstance(50,(ILogger_ExitPoll)logger);
        gui = Gui.getInstance();
        
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
                // Removed restart callback setup since the restart functionality is not working properly
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
            TVoter voter = TVoter.getInstance(i, 
                                      (IPollingStation_Voter)pollingStation, 
                                      (IExitPoll_Voter)exitPoll);
            voters.add(voter);
        }
        
        // Create clerk
        clerk = TClerk.getInstance(maxVotes, 
                           (IPollingStation_Clerk)pollingStation, 
                           (IExitPoll_Clerk)exitPoll);

        // Create pollster
        pollster = TPollster.getInstance((IExitPoll_Pollster)exitPoll);
        
        // Start the periodic GUI update thread with more comprehensive updates
        guiUpdateThread = new Thread(() -> {
            try {
                while (simulationRunning) {
                    // Update GUI with various information from logger
                    gui.updateFromLogger(
                        ((ILogger_GUI)logger).getVoteCounts(),
                        ((ILogger_GUI)logger).getVotersProcessed(),
                        ((ILogger_GUI)logger).isStationOpen()
                    );
                    gui.updateQueueAndBoothInfo(
                        ((ILogger_GUI)logger).getCurrentQueueSize(),
                        ((ILogger_GUI)logger).getCurrentVoterInBooth()
                    );
                    
                    // Control how often GUI updates happen based on simulation speed:
                    // - Base interval of 500ms is slowed down at slower speeds
                    // - Faster speeds result in more frequent updates
                    // - Minimum interval of 100ms ensures UI doesn't update too frantically
                    float speedFactor = gui.getSimulationSpeed();
                    long updateInterval = Math.round(500 / speedFactor); 
                    Thread.sleep(Math.max(100, updateInterval));
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
                pollingStation.printFinalResults();
                System.out.println("ElectionSimulation finished");
                logger.saveCloseFile();
                
                // Final GUI update after simulation is complete
                gui.updateFromLogger(
                    ((ILogger_GUI)logger).getVoteCounts(),
                    ((ILogger_GUI)logger).getVotersProcessed(),
                    ((ILogger_GUI)logger).isStationOpen()
                );
                gui.updateQueueAndBoothInfo(0, ""); // Clear queue and booth
                
                // Set simulation to not running to trigger log file loading
                Gui.setSimulationRunning(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
