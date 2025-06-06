package serverSide.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font; // Added for default font
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

/**
 * Class responsible for creating and managing GUI components
 */
public class GuiComponents {
    // GUI components
    private JPanel stationPanel;
    private JPanel queuePanel;
    private JPanel boothPanel;
    private JPanel resultsPanel;
    private JPanel statsPanel;
    private JLabel scoreALabel;
    private JLabel scoreBLabel;
    private JLabel queueLabel;
    private JLabel boothLabel;
    private DefaultTableModel tableModel;
    private JTable logTable;
    private JButton startButton;
    private JButton restartButton; // Added restartButton field
    private JButton exitButton;
    private JSlider speedSlider;
    private JLabel speedValueLabel;
    
    // Configuration components
    private JTextField numVotersField;
    private JTextField queueSizeField;
    private JTextField votesToCloseField;
    
    // Statistics labels
    private JLabel validationSuccessRateLabel;
    private JLabel validationFailRateLabel;
    private JLabel pollParticipationRateLabel;
    private JLabel pollAccuracyRateLabel;
    private JLabel averageProcessingTimeLabel;
    private JLabel runningTimeLabel;
    
    /**
     * Create the status panel containing station status, queue, booth, and results
     */
    public void createStatusPanel() {
        stationPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        stationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Polling Station Status Panel
        JPanel pollingPanel = new JPanel();
        pollingPanel.setLayout(new BorderLayout());
        pollingPanel.setBorder(GuiStyles.createTitledBorder("Polling Station", TitledBorder.CENTER));
        JLabel stationStatusLabel = new JLabel("CLOSED", JLabel.CENTER);
        stationStatusLabel.setFont(GuiStyles.TITLE_FONT);
        stationStatusLabel.setForeground(GuiStyles.ERROR_COLOR);
        pollingPanel.add(stationStatusLabel, BorderLayout.CENTER);
        
        // Voter Queue Panel
        queuePanel = new JPanel();
        queuePanel.setLayout(new BorderLayout());
        queuePanel.setBorder(GuiStyles.createTitledBorder("Voter Queue", TitledBorder.CENTER));
        queueLabel = new JLabel("Empty", JLabel.CENTER);
        queueLabel.setFont(GuiStyles.CONTENT_FONT);
        queuePanel.add(queueLabel, BorderLayout.CENTER);
        
        // Voting Booth Panel
        boothPanel = new JPanel();
        boothPanel.setLayout(new BorderLayout());
        boothPanel.setBorder(GuiStyles.createTitledBorder("Voting Booth", TitledBorder.CENTER));
        boothLabel = new JLabel("Available", JLabel.CENTER);
        boothLabel.setFont(GuiStyles.CONTENT_FONT);
        boothPanel.add(boothLabel, BorderLayout.CENTER);
        
        // Results Panel
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new GridLayout(3, 1));
        resultsPanel.setBorder(GuiStyles.createTitledBorder("Election Results", TitledBorder.CENTER));
        
        scoreALabel = new JLabel("Candidate A: 0", JLabel.CENTER);
        scoreALabel.setFont(GuiStyles.LABEL_FONT);
        scoreALabel.setForeground(GuiStyles.CANDIDATE_A_COLOR);
        
        scoreBLabel = new JLabel("Candidate B: 0", JLabel.CENTER);
        scoreBLabel.setFont(GuiStyles.LABEL_FONT);
        scoreBLabel.setForeground(GuiStyles.CANDIDATE_B_COLOR);
        
        JLabel processedLabel = new JLabel("Processed: 0", JLabel.CENTER);
        processedLabel.setFont(GuiStyles.CONTENT_FONT);
        
        resultsPanel.add(scoreALabel);
        resultsPanel.add(scoreBLabel);
        resultsPanel.add(processedLabel);
        
        // Add all panels to the status panel
        stationPanel.add(pollingPanel);
        stationPanel.add(queuePanel);
        stationPanel.add(boothPanel);
        stationPanel.add(resultsPanel);
    }
    
    /**
     * Create the control panel with speed slider
     */
    public JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        controlPanel.setBorder(GuiStyles.createTitledBorder("Simulation Control", TitledBorder.CENTER));
            
        // Create speed control slider
        JLabel speedLabel = new JLabel("Simulation Speed: ");
        speedLabel.setFont(GuiStyles.LABEL_FONT);
        
        // Speed slider settings:
        // - Min value 10 (0.1x) = very slow motion for detailed analysis
        // - Max value 300 (3.0x) = triple speed for quick review
        // - Default value 100 (1.0x) = normal speed for comfortable observation
        speedSlider = new JSlider(JSlider.HORIZONTAL, 10, 300, 100);
        speedSlider.setPreferredSize(new Dimension(200, 40));
        speedSlider.setMajorTickSpacing(50);  // Show major ticks at 0.5x, 1.0x, 1.5x, 2.0x, 2.5x, 3.0x
        speedSlider.setMinorTickSpacing(10);   // Show minor ticks every 0.1x
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        speedValueLabel = new JLabel("1.0x", JLabel.LEFT);
        speedValueLabel.setFont(GuiStyles.LABEL_FONT);
        speedValueLabel.setPreferredSize(new Dimension(50, 20));
        
        // Update simulation speed whenever slider changes
        speedSlider.addChangeListener((ChangeEvent e) -> {
            // Convert slider value (10-300) to simulation speed (0.1x-3.0x)
            float speed = speedSlider.getValue() / 100.0f;
            Gui.setSpeedValue(speed);
            speedValueLabel.setText(String.format("%.1fx", speed));
        });
        
        // Quick reset button to return to normal speed (optimal for observation)
        JButton resetSpeedButton = new JButton("Normal Speed");
        resetSpeedButton.addActionListener((ActionEvent e) -> speedSlider.setValue(100)); // Set to 1.0x speed
        
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(speedValueLabel);
        controlPanel.add(resetSpeedButton);
        
        return controlPanel;
    }
    
    /**
     * Create configuration panel for simulation parameters
     */
    public JPanel createConfigPanel() {
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        configPanel.setBorder(GuiStyles.createTitledBorder("Simulation Configuration", TitledBorder.CENTER));
        
        // Number of voters field (3-10)
        JLabel votersLabel = new JLabel("Number of Voters (3-10): ");
        votersLabel.setFont(GuiStyles.LABEL_FONT);
        
        numVotersField = new JTextField("5", 3);
        numVotersField.setFont(GuiStyles.CONTENT_FONT);
        
        // Queue size field (2-5)
        JLabel queueSizeLabel = new JLabel("Queue Size (2-5): ");
        queueSizeLabel.setFont(GuiStyles.LABEL_FONT);
        
        queueSizeField = new JTextField("3", 3);
        queueSizeField.setFont(GuiStyles.CONTENT_FONT);
        
        // Votes to close field
        JLabel votesToCloseLabel = new JLabel("Votes to Close: ");
        votesToCloseLabel.setFont(GuiStyles.LABEL_FONT);
        
        votesToCloseField = new JTextField("12", 3);
        votesToCloseField.setFont(GuiStyles.CONTENT_FONT);
        
        // Add components to panel
        configPanel.add(votersLabel);
        configPanel.add(numVotersField);
        configPanel.add(Box.createHorizontalStrut(20));
        configPanel.add(queueSizeLabel);
        configPanel.add(queueSizeField);
        configPanel.add(Box.createHorizontalStrut(20));
        configPanel.add(votesToCloseLabel);
        configPanel.add(votesToCloseField);
        
        return configPanel;
    }
    
    /**
     * Create the statistics panel
     */
    public JPanel createStatsPanel() {
        // Create statistics panel
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create section for validation statistics
        JPanel validationStatsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        validationStatsPanel.setBorder(GuiStyles.createTitledBorder("Validation Statistics", TitledBorder.LEFT));
            
        validationSuccessRateLabel = new JLabel("Validation Success Rate: 0%");
        validationSuccessRateLabel.setFont(GuiStyles.CONTENT_FONT);
        
        validationFailRateLabel = new JLabel("Validation Failure Rate: 0%");
        validationFailRateLabel.setFont(GuiStyles.CONTENT_FONT);
        
        validationStatsPanel.add(validationSuccessRateLabel);
        validationStatsPanel.add(validationFailRateLabel);
        
        // Create section for exit poll statistics
        JPanel pollStatsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        pollStatsPanel.setBorder(GuiStyles.createTitledBorder("Exit Poll Statistics", TitledBorder.LEFT));
            
        pollParticipationRateLabel = new JLabel("Poll Participation Rate: 0%");
        pollParticipationRateLabel.setFont(GuiStyles.CONTENT_FONT);
        
        pollAccuracyRateLabel = new JLabel("Poll Accuracy Rate: 0%");
        pollAccuracyRateLabel.setFont(GuiStyles.CONTENT_FONT);
        
        pollStatsPanel.add(pollParticipationRateLabel);
        pollStatsPanel.add(pollAccuracyRateLabel);
        
        // Create section for timing statistics
        JPanel timingStatsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        timingStatsPanel.setBorder(GuiStyles.createTitledBorder("Timing Statistics", TitledBorder.LEFT));
            
        averageProcessingTimeLabel = new JLabel("Average Processing Time: 0 ms");
        averageProcessingTimeLabel.setFont(GuiStyles.CONTENT_FONT);
        
        runningTimeLabel = new JLabel("Simulation Running Time: 00:00:00");
        runningTimeLabel.setFont(GuiStyles.CONTENT_FONT);
        
        timingStatsPanel.add(averageProcessingTimeLabel);
        timingStatsPanel.add(runningTimeLabel);
        
        // Add all sections to the stats panel
        statsPanel.add(validationStatsPanel);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(pollStatsPanel);
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(timingStatsPanel);
        
        // Add some space at the bottom
        statsPanel.add(Box.createVerticalGlue());
        
        return statsPanel;
    }
    
    /**
     * Create the table panel for log display
     */
    public void createTablePanel() {
        // Create columns for the log table
        String[] columns = {"Door", "Voter", "Clerk", "Validation", "Booth", "ScoreA", "ScoreB", "Exit"};
        tableModel = new DefaultTableModel(columns, 0);
        logTable = new JTable(tableModel);
        logTable.setFillsViewportHeight(true);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    
    /**
     * Create the button panel with control buttons
     */
    public JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(GuiStyles.createTitledBorder("Controls", TitledBorder.CENTER));

        startButton = new JButton("Start Server");
        startButton.setFont(new Font("Arial", Font.BOLD, 12)); // Using default font
        startButton.addActionListener(e -> {
            if (!Gui.isSimulationRunning() && Gui.getSimulationStarter() != null) {
                if (validateConfigInputs()) {
                    try {
                        new Thread(Gui.getSimulationStarter()).start();
                        startButton.setEnabled(false);
                        startButton.setText("Server Running...");
                        restartButton.setEnabled(false); // Disable restart until server is fully started
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Gui.getFrame(), 
                            "Error starting server: " + ex.getMessage(),
                            "Start Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        buttonPanel.add(startButton);

        restartButton = new JButton("Restart Server");
        restartButton.setFont(new Font("Arial", Font.BOLD, 12)); // Using default font
        restartButton.setEnabled(false); // Initially disabled
        restartButton.addActionListener(e -> {
            if (Gui.getSimulationRestarter() != null) {
                // Show confirmation dialog for restart
                int confirmed = JOptionPane.showConfirmDialog(Gui.getFrame(),
                    "Are you sure you want to restart the server?\n\n" +
                    "This will:\n" +
                    "• Stop the current simulation\n" +
                    "• Clean up RMI services\n" +
                    "• Reset all GUI state\n" +
                    "• Allow new parameters to be entered",
                    "Confirm Restart", JOptionPane.YES_NO_OPTION);
                
                if (confirmed == JOptionPane.YES_OPTION) {
                    try {
                        // Update UI immediately for user feedback
                        startButton.setText("Restarting Server...");
                        startButton.setEnabled(false);
                        restartButton.setEnabled(false);
                        
                        // Run restart in background thread
                        new Thread(() -> {
                            try {
                                Gui.getSimulationRestarter().run();
                                // UI updates will be handled by the restart method
                            } catch (Exception ex) {
                                // Re-enable buttons on error
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    startButton.setText("Start Server");
                                    startButton.setEnabled(true);
                                    restartButton.setEnabled(true);
                                });
                                throw ex;
                            }
                        }).start();
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Gui.getFrame(), 
                            "Error restarting server: " + ex.getMessage(),
                            "Restart Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        buttonPanel.add(restartButton);

        // Add Load Log File button
        JButton loadLogButton = new JButton("Load Log File");
        loadLogButton.setFont(new Font("Arial", Font.BOLD, 12));
        loadLogButton.addActionListener(e -> {
            try {
                loadLogFile();
                JOptionPane.showMessageDialog(Gui.getFrame(), 
                    "Log file loaded successfully!", 
                    "Load Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Gui.getFrame(), 
                    "Error loading log file: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(loadLogButton);

        // Add Refresh Log button
        JButton refreshLogButton = new JButton("Refresh Log");
        refreshLogButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshLogButton.addActionListener(e -> {
            try {
                loadLogFile();
                System.out.println("Log file refreshed manually");
            } catch (Exception ex) {
                System.err.println("Error refreshing log file: " + ex.getMessage());
            }
        });
        buttonPanel.add(refreshLogButton);

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 12)); // Using default font
        exitButton.setForeground(GuiStyles.ERROR_COLOR);
        exitButton.addActionListener(e -> {
            int confirmed = JOptionPane.showConfirmDialog(Gui.getFrame(), 
                "Are you sure you want to exit the program?\n\nThis will properly shutdown all RMI services.", 
                "Exit Program", JOptionPane.YES_NO_OPTION);
            
            if (confirmed == JOptionPane.YES_OPTION) {
                // Perform proper shutdown sequence
                Gui.performShutdownSequence();
            }
        });
        buttonPanel.add(exitButton);

        return buttonPanel;
    }
    
    /**
     * Validate configuration inputs
     * @return true if all inputs are valid
     */
    private boolean validateConfigInputs() {
        try {
            // Validate number of voters (3-10)
            int numVoters = Integer.parseInt(numVotersField.getText());
            if (numVoters < 3 || numVoters > 10) {
                JOptionPane.showMessageDialog(Gui.getFrame(),
                    "Number of voters must be between 3 and 10.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate queue size (2-5)
            int queueSize = Integer.parseInt(queueSizeField.getText());
            if (queueSize < 2 || queueSize > 5) {
                JOptionPane.showMessageDialog(Gui.getFrame(),
                    "Queue size must be between 2 and 5.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate votes to close (must be > 0)
            int votesToClose = Integer.parseInt(votesToCloseField.getText());
            if (votesToClose <= 0) {
                JOptionPane.showMessageDialog(Gui.getFrame(),
                    "Votes to close must be greater than 0.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Set the configuration values for the simulation
            Gui.setConfigValues(numVoters, queueSize, votesToClose);
            
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(Gui.getFrame(),
                "All fields must contain valid numbers.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Update the UI with current values
     */
    public void updateUI(int scoreA, int scoreB, int votersProcessed, 
                         boolean stationOpen, int queueSize, String currentVoterInBooth) {
        // Get the first component from stationPanel (which should be the status label)
        JPanel pollingPanel = (JPanel) stationPanel.getComponent(0);
        JLabel stationStatusLabel = (JLabel) pollingPanel.getComponent(0);
        
        if (stationOpen) {
            stationStatusLabel.setText("OPEN");
            stationStatusLabel.setForeground(GuiStyles.SUCCESS_COLOR);
        } else {
            stationStatusLabel.setText("CLOSED");
            stationStatusLabel.setForeground(GuiStyles.ERROR_COLOR);
            
            // If station is closed and we were running a simulation, update the button
            if (Gui.isSimulationRunning()) {
                startButton.setText("Simulation Finished");
                restartButton.setEnabled(true);  // Enable restart button when finished
                Gui.setSimulationRunning(false);
                
                // Automatically load the log file when the simulation ends
                loadLogFile();
            }
        }
        
        // Update queue status
        if (queueSize > 0) {
            queueLabel.setText("Voters: " + queueSize);
        } else {
            queueLabel.setText("Empty");
        }
        
        // Update booth status
        if (currentVoterInBooth.isEmpty()) {
            boothLabel.setText("Available");
        } else {
            boothLabel.setText("In use by: " + currentVoterInBooth);
        }
        
        // Update scores
        scoreALabel.setText("Candidate A: " + scoreA);
        scoreBLabel.setText("Candidate B: " + scoreB);
        
        // Update processed voters count
        ((JLabel)resultsPanel.getComponent(2)).setText("Processed: " + votersProcessed);
    }
    
    /**
     * Reset UI components after simulation restart
     */
    public void resetUI() {
        // Reset status labels
        JPanel pollingPanel = (JPanel) stationPanel.getComponent(0);
        JLabel stationStatusLabel = (JLabel) pollingPanel.getComponent(0);
        stationStatusLabel.setText("CLOSED");
        stationStatusLabel.setForeground(GuiStyles.ERROR_COLOR);
        
        // Reset queue and booth
        queueLabel.setText("Empty");
        boothLabel.setText("Available");
        
        // Reset scores
        scoreALabel.setText("Candidate A: 0");
        scoreBLabel.setText("Candidate B: 0");
        ((JLabel)resultsPanel.getComponent(2)).setText("Processed: 0");
        
        // Reset statistics
        validationSuccessRateLabel.setText("Validation Success Rate: 0%");
        validationFailRateLabel.setText("Validation Failure Rate: 0%");
        pollParticipationRateLabel.setText("Poll Participation Rate: 0%");
        pollAccuracyRateLabel.setText("Poll Accuracy Rate: 0%");
        averageProcessingTimeLabel.setText("Average Processing Time: 0 ms");
        runningTimeLabel.setText("Simulation Running Time: 00:00:00");
        
        // Clear log table
        tableModel.setRowCount(0);

        // Reset button states for restart
        if (startButton != null) {
            startButton.setEnabled(true);
            startButton.setText("Start Server");
        }
        if (restartButton != null) {
            restartButton.setEnabled(false); // Disabled until server starts again
        }
        
        System.out.println("GuiComponents: UI reset completed - ready for new configuration");
    }
    
    /**
     * Update statistics on the stats panel
     */
    public void updateStats(int validationSuccess, int validationFail, 
                         int pollParticipants, int pollTotal,
                         int pollAccurate, int pollResponses,
                         long avgProcessingTime) {
        try {
            // Calculate and update validation statistics
            int totalValidations = validationSuccess + validationFail;
            if (totalValidations > 0) {
                int successRate = (validationSuccess * 100) / totalValidations;
                int failRate = (validationFail * 100) / totalValidations;
                validationSuccessRateLabel.setText("Validation Success Rate: " + successRate + "%");
                validationFailRateLabel.setText("Validation Failure Rate: " + failRate + "%");
            }
            
            // Calculate and update exit poll statistics
            if (pollTotal > 0) {
                int participationRate = (pollParticipants * 100) / pollTotal;
                pollParticipationRateLabel.setText("Poll Participation Rate: " + participationRate + "%");
            }
            
            if (pollResponses > 0) {
                int accuracyRate = (pollAccurate * 100) / pollResponses;
                pollAccuracyRateLabel.setText("Poll Accuracy Rate: " + accuracyRate + "%");
            }
            
            // Update timing statistics
            averageProcessingTimeLabel.setText("Average Processing Time: " + avgProcessingTime + " ms");
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
    
    /**
     * Update running time on the stats panel
     */
    public void updateRunningTime(long elapsedTime) {
        // Format as HH:MM:SS
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String formattedTime = sdf.format(new Date(elapsedTime));
        
        runningTimeLabel.setText("Simulation Running Time: " + formattedTime);
    }
    
    /**
     * Load the log file into the table with improved error handling and real-time updates
     */
    public void loadLogFile() {
        try {
            // Ensure table model is initialized before loading
            if (tableModel == null) {
                createTablePanel();
            }
            
            // Find the log file
            File logFile = findBestLogFile();
            if (logFile == null) {
                // Don't show error dialogs during automatic refresh
                return;
            }
            
            // Check if the file is empty or still being written
            if (logFile.length() == 0) {
                return; // Silently skip empty files during monitoring
            }
            
            // Remember current row count to avoid unnecessary clearing
            int previousRowCount = tableModel.getRowCount();
            
            // Read the log file
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                // Clear current table data
                tableModel.setRowCount(0);
                
                String line;
                int entriesAdded = 0;
                
                // Read and parse log entries
                while ((line = reader.readLine()) != null) {
                    // Skip header lines and formatting
                    if (line.contains("Door") && line.contains("Voter") && line.contains("Clerk")) {
                        continue;
                    }
                    if (line.startsWith("+") || line.startsWith("-") || line.trim().isEmpty()) {
                        continue;
                    }
                    if (line.contains("Total of Voters:") || line.contains("Votes to end:")) {
                        continue;
                    }
                    
                    if (parseLine(line)) {
                        entriesAdded++;
                    }
                }
                
                // Only log successful updates when entries are actually added
                if (entriesAdded > 0 && entriesAdded != previousRowCount) {
                    System.out.println("Log display updated: " + entriesAdded + " entries");
                }
            }
            
        } catch (IOException e) {
            // Only show error dialogs for manual refresh, not automatic monitoring
            if (java.awt.EventQueue.isDispatchThread()) {
                System.err.println("Error reading log file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Find the best available log file from multiple possible locations
     */
    private File findBestLogFile() {
        File[] possibleFiles = {
            new File("log.txt"),
            new File("../../log.txt"),
            new File("electionDay/log.txt"),
            new File("src/log.txt"),
            new File("../log.txt")
        };
        
        for (File file : possibleFiles) {
            if (file.exists() && file.canRead() && file.length() > 0) {
                return file;
            }
        }
        
        // Return the most likely location even if it doesn't exist yet
        return new File("log.txt");
    }
    
    /**
     * Parse a line from the log file
     * @return true if line was successfully parsed and added to the table
     */
    private boolean parseLine(String line) {
        if (line == null || line.trim().isEmpty()) return false;
        
        String[] columns = new String[8];
        line = line.trim();
        
        // Skip lines that don't match our expected format
        if (!line.startsWith("|")) return false;
        
        try {
            // Split the line by the pipe character and extract each column
            String[] parts = line.split("\\|");
            if (parts.length >= 9) {
                for (int i = 1; i < Math.min(parts.length, 9); i++) {
                    columns[i-1] = parts[i].trim();
                }
                
                // Add the row to the table model
                tableModel.addRow(columns);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            e.printStackTrace();
        }
        return false;
    }
    
    // Getters for components
    public JPanel getStationPanel() { return stationPanel; }
    public JTable getLogTable() { return logTable; }
    
    // Getters for configuration fields
    public int getNumVoters() { 
        try {
            return Integer.parseInt(numVotersField.getText()); 
        } catch (NumberFormatException e) {
            return 5; // Default value
        }
    }
    
    public int getQueueSize() { 
        try {
            return Integer.parseInt(queueSizeField.getText()); 
        } catch (NumberFormatException e) {
            return 3; // Default value
        }
    }
    
    public int getVotesToClose() { 
        try {
            return Integer.parseInt(votesToCloseField.getText()); 
        } catch (NumberFormatException e) {
            return 3; // Default value
        }
    }

    // Added getters for buttons to be controlled by Gui.java if needed
    public JButton getStartButton() {
        return startButton;
    }

    public JButton getRestartButton() {
        return restartButton;
    }
}
