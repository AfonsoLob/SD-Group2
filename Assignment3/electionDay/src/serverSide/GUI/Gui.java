package serverSide.GUI;

import java.awt.BorderLayout;
import java.io.File;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import serverSide.main.LoggerServer;
import serverSide.sharedRegions.Logger;

/**
 * Main GUI class for the Election Day simulation - Assignment 3 (RMI).
 * This class uses a singleton pattern to ensure a single GUI instance.
 * The Logger component communicates directly with this GUI without interface abstractions.
 */
public class Gui {
    // Log file path
    private static final String LOG_FILE = "../../log.txt"; // Relative path to the log file
    
    // Simulation state
    private static int scoreA = 0;
    private static int scoreB = 0;
    private static int votersProcessed = 0;
    private static boolean stationOpen = false;
    private static boolean simulationRunning = false;
    private static int queueSize = 0;
    private static String currentVoterInBooth = "";
    
    // Configuration settings
    private static int configNumVoters = 5;
    private static int configQueueSize = 3;
    private static int configVotesToClose = 15;
    
    // Speed control
    /**
     * simulationSpeed controls the pace of the entire simulation:
     * - 1.0f is normal speed (real-time)
     * - Values < 1.0 slow down the simulation (e.g., 0.5f = half speed)
     * - Values > 1.0 speed up the simulation (e.g., 2.0f = double speed)
     */
    private static float simulationSpeed = 1.0f;
    
    // Singleton instance
    private static final Gui INSTANCE = new Gui();
    
    // UI components
    private static JFrame frame;
    private static GuiComponents components;
    private static GuiAnimation animation;
    
    // Callback for simulation starter and restarter
    private static Runnable simulationStarter;
    private static Runnable simulationRestarter;
    
    // Private constructor for singleton pattern
    private Gui() {
        // Empty constructor - initialization happens in window()
    }
    
    // Get the singleton instance
    public static Gui getInstance() {
        return INSTANCE;
    }
    
    // Create a new GUI window
    public static void window() {
        // Create the main window
        frame = new JFrame("Election Day Simulation - Assignment 3 (RMI)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());
        
        // Initialize components
        components = new GuiComponents();
        
        // Create the components
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create a tabbed pane for different views
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Main simulation view with animation
        JPanel simulationTab = new JPanel(new BorderLayout());
        
        components.createStatusPanel();
        JPanel configPanel = components.createConfigPanel();
        JPanel controlPanel = components.createControlPanel();
        JPanel buttonPanel = components.createButtonPanel();
        
        // Create top panel with status, config, controls, and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(components.getStationPanel(), BorderLayout.NORTH);
        
        // Combine config and control panels
        JPanel configControlPanel = new JPanel(new BorderLayout());
        configControlPanel.add(configPanel, BorderLayout.CENTER);
        configControlPanel.add(controlPanel, BorderLayout.SOUTH);
        topPanel.add(configControlPanel, BorderLayout.CENTER);
        
        // Add button panel at the bottom
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        simulationTab.add(topPanel, BorderLayout.NORTH);
        
        // Create bottom panel for animation
        animation = new GuiAnimation();
        JPanel animationPanel = animation.getAnimationPanel();
        simulationTab.add(animationPanel, BorderLayout.CENTER);
        
        tabbedPane.addTab("Simulation", simulationTab);
        
        // Tab 2: Log file viewer
        JPanel logPanel = createLogViewerPanel();
        tabbedPane.addTab("Log Viewer", logPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        frame.add(mainPanel);
        
        // Set up the simulation starter (called when the Start button is clicked)
        setSimulationStarter(() -> {
            try {
                // Set simulation state to running
                simulationRunning = true;
                
                // Get configuration parameters from GuiComponents
                int numVoters = components.getNumVoters();
                int localQueueSize = components.getQueueSize();
                int votesToClose = components.getVotesToClose();
                
                // Update configuration values
                setConfigValues(numVoters, localQueueSize, votesToClose);
                
                // Create Logger instance and notify LoggerServer
                try {
                    Logger loggerInstance = Logger.getInstance(numVoters, localQueueSize, votesToClose);
                    LoggerServer.initializeLoggerService(loggerInstance, true);
                } catch (RemoteException e) {
                    System.err.println("Error initializing Logger: " + e.getMessage());
                }
                
                System.out.println("GUI: Logger instance created and parameters set -> Voters: " + numVoters + 
                                   ", Queue Capacity: " + localQueueSize + ", Votes to Close: " + votesToClose);
                
                // Update UI to reflect that parameters are set
                components.getStartButton().setEnabled(false);
                components.getStartButton().setText("Logger Service Started - Start External Clients");
                
            } catch (NullPointerException e) {
                System.err.println("Error starting Logger service: " + e.getMessage());
                components.resetUI(); // Reset UI on error
                simulationRunning = false;
                if (frame != null) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error starting Logger service: " + e.getMessage(),
                        "Service Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Set up the simulation restarter (called when the Restart button is clicked)
        setSimulationRestarter(() -> {
            try {
                System.out.println("GUI: Restart requested - performing cleanup and reset...");
                
                // Clean up RMI services if simulation is running
                if (simulationRunning) {
                    System.out.println("GUI: Cleaning up RMI services for restart...");
                    try {
                        LoggerServer.performCleanShutdown();
                        // Give time for cleanup to complete
                        Thread.sleep(1500);
                    } catch (Exception cleanupError) {
                        System.err.println("GUI: Error during RMI cleanup: " + cleanupError.getMessage());
                        // Continue with restart even if cleanup failed
                    }
                }
                
                // Reset the GUI and allow re-configuration
                resetForRestart();
                
                String resetMessage = """
                System reset complete. RMI services have been cleaned up.
                You can now enter new parameters and restart the Logger service.
                """;
                System.out.println("GUI: Restart complete - ready for new parameters");
                JOptionPane.showMessageDialog(frame, 
                    resetMessage,
                    "Restart Complete", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                System.err.println("Error during restart: " + e.getMessage());
                e.printStackTrace();
                if (frame != null) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error during restart: " + e.getMessage() + 
                        "\n\nThe system may need to be manually restarted.",
                        "Restart Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Show the window
        frame.setVisible(true);
        
        // Start the update timer
        Timer updateTimer = new Timer(100, e -> INSTANCE.updateGui());
        updateTimer.start();
        
        // Start log file monitoring
        startLogFileMonitoring();
    }
    
    private static JPanel createLogViewerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Log File Viewer"));
        
        // Initialize the table before loading the log file
        components.createTablePanel();
        
        // Use the GuiComponents log loading functionality
        components.loadLogFile();
        
        // For now, create a simple display using the log table
        JScrollPane scrollPane = new JScrollPane(components.getLogTable());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static Timer logMonitorTimer;
    private static long lastLogFileSize = 0;
    private static long lastLogModified = 0;
    
    private static void startLogFileMonitoring() {
        // Start a background timer to monitor the log file for real-time updates
        logMonitorTimer = new Timer(500, e -> {
            if (components != null) {
                checkAndRefreshLogFile();
            }
        });
        logMonitorTimer.start();
        System.out.println("GUI: Started real-time log file monitoring");
    }
    
    private static void checkAndRefreshLogFile() {
        try {
            File logFile = findLogFile();
            if (logFile != null && logFile.exists()) {
                long currentSize = logFile.length();
                long currentModified = logFile.lastModified();
                
                // Check if file has been modified or grown
                if (currentSize != lastLogFileSize || currentModified != lastLogModified) {
                    lastLogFileSize = currentSize;
                    lastLogModified = currentModified;
                    
                    // Refresh the log display in the GUI
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        components.loadLogFile();
                    });
                }
            }
        } catch (Exception e) {
            // Silently handle errors to avoid disrupting the GUI
            System.err.println("Error monitoring log file: " + e.getMessage());
        }
    }
    
    private static File findLogFile() {
        // Try multiple possible log file locations
        File[] possibleFiles = {
            new File("log.txt"),
            new File(LOG_FILE),
            new File("electionDay/log.txt"),
            new File("src/log.txt"),
            new File("../log.txt"),
            new File("../../log.txt")
        };
        
        for (File file : possibleFiles) {
            if (file.exists() && file.canRead()) {
                return file;
            }
        }
        return null;
    }
    
    private static void updateDisplays() {
        if (components != null) {
            components.updateUI(scoreA, scoreB, votersProcessed, stationOpen, queueSize, currentVoterInBooth);
            
            // Animation update is handled internally by GuiAnimation
            if (animation != null) {
                // Animation updates itself through the individual method calls
            }
        }
    }
    
    private static void resetForRestart() {
        // Reset simulation state
        simulationRunning = false;
        scoreA = 0;
        scoreB = 0;
        votersProcessed = 0;
        stationOpen = false;
        queueSize = 0;
        currentVoterInBooth = "";
        
        // Reset UI components
        if (components != null) {
            components.resetUI();
        }
        
        // Reset animation
        if (animation != null) {
            animation.resetAnimation();
        }
    }
    
    // Getter and setter for simulation callbacks
    public static Runnable getSimulationStarter() {
        return simulationStarter;
    }
    
    public static void setSimulationStarter(Runnable starter) {
        simulationStarter = starter;
        // Components will access the starter through getSimulationStarter()
    }
    
    public static Runnable getSimulationRestarter() {
        return simulationRestarter;
    }
    
    public static void setSimulationRestarter(Runnable restarter) {
        simulationRestarter = restarter;
        // Components will access the restarter through getRestarter()
    }
    
    // Configuration getters and setters
    public static void setConfigValues(int numVoters, int queueSize, int votesToClose) {
        configNumVoters = numVoters;
        configQueueSize = queueSize;
        configVotesToClose = votesToClose;
    }
    
    public static int getConfigNumVoters() { return configNumVoters; }
    public static int getConfigQueueSize() { return configQueueSize; }
    public static int getConfigVotesToClose() { return configVotesToClose; }
    
    // Speed control
    public static float getSimulationSpeed() { return simulationSpeed; }
    public static void setSimulationSpeed(float speed) { 
        simulationSpeed = Math.max(0.1f, Math.min(5.0f, speed)); 
    }
    
    /**
     * Get the current simulation speed for use by animation components.
     * This is an alias for getSimulationSpeed() for compatibility.
     * @return The speed factor where 1.0f is normal speed
     */
    public static float getStaticSimulationSpeed() {
        return simulationSpeed;
    }
    
    public static boolean isSimulationRunning() {
        return simulationRunning;
    }
    
    public static void setSpeedValue(float speed) {
        setSimulationSpeed(speed);
    }
    
    // ==============================================
    // Getter methods for compatibility with GuiComponents
    // ==============================================
    
    public static JFrame getFrame() {
        return frame;
    }
    
    public static Runnable getRestarter() {
        return simulationRestarter;
    }
    
    public static void setSimulationRunning(boolean running) {
        simulationRunning = running;
    }
    
    // ==============================================
    // Methods called by Logger (direct GUI communication without interfaces)
    // ==============================================
    
    /**
     * Called when a voter arrives at the polling station
     */
    public void voterArrived(int voterId) {
        if (animation != null) {
            animation.voterArrived(voterId);
        }
    }
    
    /**
     * Called when a voter enters the queue
     */
    public void voterEnteringQueue(int voterId) {
        queueSize++;
        if (animation != null) {
            animation.voterEnteringQueue(voterId);
        }
    }
    
    /**
     * Called when a voter's ID is validated
     */
    public void voterValidated(int voterId, int valid) {
        updateGui();
    }
    
    /**
     * Called when a voter is voting
     */
    public void voterVoting(int voterId, boolean voteA) {
        currentVoterInBooth = "Voter " + voterId;
        if (voteA) {
            scoreA++;
        } else {
            scoreB++;
        }
        if (animation != null) {
            animation.voterVoting(voterId, voteA);
        }
    }
    
    /**
     * Called when a voter participates in the exit poll
     */
    public void voterExitPoll(int voterId, String vote) {
        votersProcessed++;
        queueSize = Math.max(0, queueSize - 1);
        currentVoterInBooth = "";
        if (animation != null) {
            animation.voterExitPoll(voterId, vote);
        }
    }
    
    /**
     * Called when a voter is reborn (gets a new ID)
     */
    public void voterReborn(int originalId, int newId) {
        if (animation != null) {
            animation.voterReborn(originalId, newId);
        }
    }
    
    /**
     * Called when the polling station opens
     */
    public void stationOpening() {
        stationOpen = true;
        System.out.println("GUI: Polling station opened");
        // chage the panel were the polling station status is displayed

        updateDisplays(); // Update the GUI display
    }
    
    /**
     * Called when the polling station closes
     */
    public void stationClosing() {
        stationOpen = false;
        System.out.println("GUI: Polling station closed");
        updateDisplays(); // Update the GUI display
    }
    
    /**
     * Update statistics display
     */
    public void updateStats(int validationSuccess, int validationFail, int pollParticipants, 
                          int pollTotal, int pollAccurate, int pollResponses, long avgProcessingTime) {
        if (components != null) {
            components.updateStats(validationSuccess, validationFail, pollParticipants, 
                                 pollTotal, pollAccurate, pollResponses, avgProcessingTime);
        }
    }
    
    /**
     * Update display from logger information
     */
    public void updateFromLogger(String voteCounts, int votersProcessed, boolean stationOpen) {
        System.out.println("Gui.updateFromLogger called - VoteCounts: " + voteCounts + 
                          ", Processed: " + votersProcessed + ", Open: " + stationOpen);
        // Update local state based on logger information
        Gui.votersProcessed = votersProcessed;
        Gui.stationOpen = stationOpen;
        
        // Parse vote counts if provided
        if (voteCounts != null && voteCounts.contains("Candidate A:")) {
            try {
                String[] parts = voteCounts.split(",");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("Candidate A:")) {
                        scoreA = Integer.parseInt(part.substring(12).trim());
                    } else if (part.startsWith("Candidate B:")) {
                        scoreB = Integer.parseInt(part.substring(12).trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing vote counts: " + e.getMessage());
            }
        }
    }
    
    /**
     * Update GUI components with the latest statistics
     */
    public void updateGui() {
        System.out.println("Updating GUI: Voters Processed=" + votersProcessed + ", Queue Size=" + queueSize + ", ScoreA=" + scoreA + ", ScoreB=" + scoreB);
        
        // Actually update the display components
        updateDisplays();
    }
    
    /**
     * Perform proper shutdown sequence with RMI cleanup
     */
    public static void performShutdownSequence() {
        System.out.println("GUI: Starting shutdown sequence...");
        
        try {
            // First notify the LoggerServer to clean up its RMI bindings
            if (simulationRunning) {
                System.out.println("GUI: Simulation is running, performing RMI cleanup...");
                LoggerServer.performCleanShutdown();
            }
            
            // Give a moment for RMI cleanup to complete
            Thread.sleep(1000);
            
        } catch (Exception e) {
            System.err.println("GUI: Error during shutdown sequence: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always exit after attempting cleanup
            System.out.println("GUI: Shutdown sequence complete. Exiting application.");
            System.exit(0);
        }
    }
}
