package serverSide.GUI;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import serverSide.interfaces.GUI.IGUI_all;
import serverSide.interfaces.Logger.ILogger_GUI;
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
    
    // Configuration settings
    private static int configNumVoters = 5;
    private static int configQueueSize = 3;
    private static int configVotesToClose = 3;
    
    // Speed control
    /**
     * simulationSpeed controls the pace of the entire simulation:
     * - 1.0f is normal speed (real-time)
     * - Values < 1.0 slow down the simulation (e.g., 0.5f = half speed)
     * - Values > 1.0 speed up the simulation (e.g., 2.0f = double speed)
     */
    private static float simulationSpeed = 0.5f;
    
    // Timing measurement
    private static long simulationStartTime = 0;
    
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
        JPanel configPanel = components.createConfigPanel();
        JPanel controlPanel = components.createControlPanel();
        
        // Create top panel with status, config, and controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(components.getStationPanel(), BorderLayout.NORTH);
        topPanel.add(configPanel, BorderLayout.CENTER);
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
        
        // Set up the simulation starter (called when the Start button is clicked)
        setSimulationStarter(() -> {
            try {
                // Set simulation state to running
                simulationRunning = true;
                
                // Get configuration parameters from GuiComponents
                int numVoters = components.getNumVoters();
                int queueSize = components.getQueueSize();
                int votesToClose = components.getVotesToClose();
                
                // Update configuration values
                setConfigValues(numVoters, queueSize, votesToClose);
                
                // Call ServerLogger's static method to start the server logic
                serverSide.main.ServerLogger.startServerLogic(numVoters, queueSize, votesToClose);
            } catch (Exception e) {
                System.err.println("Error starting server: " + e.getMessage());
                components.resetUI(); // Reset UI on error
                simulationRunning = false;
                if (frame != null) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error starting server: " + e.getMessage(),
                        "Server Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Set up the simulation restarter (called when the Restart button is clicked)
        setSimulationRestarter(() -> {
            try {
                // First, shut down the current server instance
                serverSide.main.ServerLogger.shutdown();
                
                // Allow a brief pause for shutdown to complete
                try {
                    Thread.sleep(500);  // 500ms pause
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                // Reset the GUI
                resetForRestart();
                
                // Get the current configuration values
                int numVoters = components.getNumVoters();
                int queueSize = components.getQueueSize();
                int votesToClose = components.getVotesToClose();
                
                // Update configuration values
                setConfigValues(numVoters, queueSize, votesToClose);
                
                // Start a new server instance
                serverSide.main.ServerLogger.startServerLogic(numVoters, queueSize, votesToClose);
            } catch (Exception e) {
                System.err.println("Error restarting server: " + e.getMessage());
                simulationRunning = false;
                if (frame != null) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error restarting server: " + e.getMessage(),
                        "Server Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
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
    
    // Method to set the simulation restarter callback
    public static void setSimulationRestarter(Runnable restarter) {
        simulationRestarter = restarter;
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
    
    
    @Override
    public void setSimulationRunning(boolean running) {
        Gui.simulationRunning = running; // Update the static field
        if (running) {
            Gui.simulationStartTime = System.currentTimeMillis();
        }
        // Update GUI components, e.g., enable/disable start button
        if (components != null && components.getStartButton() != null) { // Assuming GuiComponents has getStartButton()
            components.getStartButton().setEnabled(!running);
            components.getStartButton().setText(running ? "Server Running..." : "Start Server");
        }
        if (components != null && components.getRestartButton() != null) { // Assuming GuiComponents has getRestartButton()
             components.getRestartButton().setEnabled(running); // Enable restart only when running or after it ran
        }
    }

    // The existing static method can remain or be called by the instance method
    public static void setStaticSimulationRunning(boolean running) {
        simulationRunning = running;
        if (running) {
            simulationStartTime = System.currentTimeMillis();
        }
    }
    
    public static void setSpeedValue(float speed) {
        simulationSpeed = speed;
    }
    
    // Methods to access simulation starter
    public static Runnable getSimulationStarter() {
        return simulationStarter;
    }
    
    // Method to access simulation restarter
    public static Runnable getRestarter() {
        return simulationRestarter;
    }
    
    // Alternative name for the same functionality (for backward compatibility)
    public static Runnable getSimulationRestarter() {
        return simulationRestarter;
    }
    
    // Method to set config values from UI
    public static void setConfigValues(int numVoters, int queueSize, int votesToClose) {
        configNumVoters = numVoters;
        configQueueSize = queueSize;
        configVotesToClose = votesToClose;
    }
    
    // Methods to access config values
    public static int getConfigNumVoters() {
        return configNumVoters;
    }
    
    public static int getConfigQueueSize() {
        return configQueueSize;
    }
    
    public static int getConfigVotesToClose() {
        return configVotesToClose;
    }
    
    // Method to reset UI for restart
    public static void resetForRestart() {
        scoreA = 0;
        scoreB = 0;
        votersProcessed = 0;
        stationOpen = false;
        queueSize = 0;
        currentVoterInBooth = "";
        simulationStartTime = System.currentTimeMillis();
        simulationRunning = true;  // Set to true as we're restarting the simulation
        
        // Reset the components
        components.resetUI();
        
        // Reset the animation
        if (animation != null) {
            animation.resetAnimation();
        }
    }
    
    // Method to get frame for dialog context
    public static JFrame getFrame() {
        return frame;
    }
    
    /**
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
    public void displayMessage(String message) {
        if (frame != null) {
            JOptionPane.showMessageDialog(frame, message, "Server Message", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Fallback if frame is not available or for headless mode/testing
            System.out.println("GUI Message (frame not available): " + message);
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
