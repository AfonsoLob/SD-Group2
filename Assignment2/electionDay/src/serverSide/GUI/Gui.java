package serverSide.GUI;

import serverSide.interfaces.GUI.IGUI_all;
import serverSide.interfaces.Logger.ILogger_GUI;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

/**
 * Main GUI class implementing the IGUI_all interface.
 * This class uses a singleton pattern to ensure a single GUI instance.
 */
public class Gui implements IGUI_all {
    // Log file path
    private static final String LOG_FILE = "log.txt";
    
    // Simulation state
    private static int scoreA = 0;
    private static int scoreB = 0;
    private static int votersProcessed = 0;
    private static boolean stationOpen = false;
    private static boolean simulationRunning = false;
    private static int queueSize = 0;
    private static String currentVoterInBooth = "";
    
    // Speed control
    /**
     * simulationSpeed controls the pace of the entire simulation:
     * - 1.0f is normal speed (real-time)
     * - Values < 1.0 slow down the simulation (e.g., 0.5f = half speed)
     * - Values > 1.0 speed up the simulation (e.g., 2.0f = double speed)
     * 
     * The speed factor is used in various places:
     * 1. Animation timing and frame rate
     * 2. Voter movement and decision timing
     * 3. Clerk processing rate
     * 4. UI update frequency
     */
    private static float simulationSpeed = 1.0f; // Default speed
    
    // Callback for simulation control
    private static Runnable simulationStarter;
    
    // Main GUI components
    private static JFrame frame;
    private static long simulationStartTime;
    
    // UI Components - reference to containers
    private static GuiComponents components;
    
    // Animation components
    private static GuiAnimation animation;
    
    // Singleton instance
    private static final Gui INSTANCE = new Gui();
    
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
        frame = new JFrame("Election Day Simulation");
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
        JPanel controlPanel = components.createControlPanel();
        
        // Create top panel with status and controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(components.getStationPanel(), BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);
        simulationTab.add(topPanel, BorderLayout.NORTH);
        
        // Add animation panel as the main content
        animation = new GuiAnimation();
        simulationTab.add(animation.getAnimationPanel(), BorderLayout.CENTER);
        
        tabbedPane.addTab("Simulation", simulationTab);
        
        // Tab 2: Log view
        components.createTablePanel();
        JScrollPane scrollPane = new JScrollPane(components.getLogTable());
        scrollPane.setBorder(GuiStyles.createTitledBorder("Simulation Log", TitledBorder.CENTER));
        tabbedPane.addTab("Log", scrollPane);
        
        // Tab 3: Statistics
        JPanel statsTab = components.createStatsPanel();
        tabbedPane.addTab("Statistics", statsTab);
        
        // Start stats update timer
        Timer statsUpdateTimer = new Timer(1000, e -> updateStatistics());
        statsUpdateTimer.start();
        
        // Add tabbed pane to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Add button panel at the bottom
        JPanel buttonPanel = components.createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        
        // Display the window
        frame.setVisible(true);
        
        // Debug folder location
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        File logFile = new File(LOG_FILE);
        System.out.println("Log file exists: " + logFile.exists());
        System.out.println("Log file path: " + logFile.getAbsolutePath());
    }
    
    private static void updateStatistics() {
        if (!simulationRunning) return;
        
        // Update running time
        if (simulationStartTime > 0) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - simulationStartTime;
            components.updateRunningTime(elapsedTime);
        }
    }
    
    // Method to set the simulation starter callback
    public static void setSimulationStarter(Runnable starter) {
        simulationStarter = starter;
    }
    
    // Static method for backward compatibility
    /**
     * Get the current simulation speed factor.
     * Used by interface components to adjust their timing.
     * @return The speed factor where 1.0f is normal speed
     */
    public static float getStaticSimulationSpeed() {
        return simulationSpeed;
    }
    
    public static boolean isSimulationRunning() {
        return simulationRunning;
    }
    
    public static void setSimulationRunning(boolean running) {
        simulationRunning = running;
        if (running) {
            simulationStartTime = System.currentTimeMillis();
        }
    }
    
    // Methods to access internal state
    /**
     * Set a new simulation speed factor.
     * This affects the timing of all animated elements and thread sleeps.
     * 
     * @param speed The new speed factor:
     *        - 0.05 to 0.5: Slow motion for detailed analysis
     *        - 0.5 to 1.0: Comfortable pace for observation
     *        - 1.0 to 2.0: Accelerated simulation for quick results
     */
    public static void setSpeedValue(float speed) {
        simulationSpeed = speed;
        // Note: Each component will check this value when making timing decisions
    }
    
    public static Runnable getSimulationStarter() {
        return simulationStarter;
    }
    
    public static JFrame getFrame() {
        return frame;
    }
    
    // IGUI_Common interface implementation
    /**
     * Implementation of interface method to access simulation speed.
     * Provides a non-static way for components to get the current speed factor.
     */
    @Override
    public float getSimulationSpeed() {
        return simulationSpeed;
    }
    
    @Override
    public void updateFromLogger(String voteCounts, int processedVoters, boolean isStationOpen) {
        try {
            scoreA = Integer.parseInt(voteCounts.split(", ")[0].split(": ")[1]);
            scoreB = Integer.parseInt(voteCounts.split(", ")[1].split(": ")[1]);
            votersProcessed = processedVoters;
            stationOpen = isStationOpen;
            components.updateUI(scoreA, scoreB, votersProcessed, stationOpen, queueSize, currentVoterInBooth);
        } catch (Exception e) {
            System.err.println("Error updating GUI from logger: " + e.getMessage());
        }
    }
    
    @Override
    public void updateQueueAndBoothInfo(int queueCount, String voterInBooth) {
        queueSize = queueCount;
        currentVoterInBooth = voterInBooth;
        components.updateUI(scoreA, scoreB, votersProcessed, stationOpen, queueSize, currentVoterInBooth);
    }
    
    // IGUI_Statistics interface implementation
    @Override
    public void updateStats(int validationSuccess, int validationFail, 
                            int pollParticipants, int pollTotal,
                            int pollAccurate, int pollResponses,
                            long avgProcessingTime) {
        components.updateStats(validationSuccess, validationFail,
                               pollParticipants, pollTotal,
                               pollAccurate, pollResponses,
                               avgProcessingTime);
    }
    
    // IGUI_Voter interface implementation
    @Override
    public void voterArrived(int voterId) {
        if (animation != null) {
            animation.voterArrived(voterId);
        }
    }
    
    @Override
    public void voterEnteringQueue(int voterId) {
        if (animation != null) {
            animation.voterEnteringQueue(voterId);
        }
    }
    
    @Override
    public void voterValidated(int voterId, boolean valid) {
        if (animation != null) {
            animation.voterValidated(voterId, valid);
        }
    }
    
    @Override
    public void voterVoting(int voterId, boolean voteA) {
        if (animation != null) {
            animation.voterVoting(voterId, voteA);
        }
    }
    
    @Override
    public void voterExitPoll(int voterId, String vote) {
        if (animation != null) {
            animation.voterExitPoll(voterId, vote);
        }
    }
    
    @Override
    public void voterReborn(int oldId, int newId) {
        if (animation != null) {
            animation.voterReborn(oldId, newId);
        }
    }
    
    // Renamed static bridge methods to avoid conflicts
    public static void staticUpdateFromLogger(ILogger_GUI logger) {
        if (logger != null) {
            getInstance().updateFromLogger(
                logger.getVoteCounts(),
                logger.getVotersProcessed(),
                logger.isStationOpen()
            );
        }
    }
    
    public static void staticUpdateQueueAndBoothInfo(int queueCount, String voterInBooth) {
        getInstance().updateQueueAndBoothInfo(queueCount, voterInBooth);
    }
    
    public static void staticUpdateStats(int validationSuccess, int validationFail, 
                                  int pollParticipants, int pollTotal,
                                  int pollAccurate, int pollResponses,
                                  long avgProcessingTime) {
        getInstance().updateStats(
            validationSuccess, validationFail, 
            pollParticipants, pollTotal,
            pollAccurate, pollResponses,
            avgProcessingTime
        );
    }
    
    public static void staticVoterArrived(int voterId) {
        getInstance().voterArrived(voterId);
    }
    
    public static void staticVoterEnteringQueue(int voterId) {
        getInstance().voterEnteringQueue(voterId);
    }
    
    public static void staticVoterValidated(int voterId, boolean valid) {
        getInstance().voterValidated(voterId, valid);
    }
    
    public static void staticVoterVoting(int voterId, boolean voteA) {
        getInstance().voterVoting(voterId, voteA);
    }
    
    public static void staticVoterExitPoll(int voterId, String vote) {
        getInstance().voterExitPoll(voterId, vote);
    }
    
    public static void staticVoterReborn(int oldId, int newId) {
        getInstance().voterReborn(oldId, newId);
    }
}
