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
        // - Min value 5 (0.05x) = very slow motion for detailed analysis
        // - Max value 200 (2.0x) = double speed for quick review
        // - Default value 50 (0.5x) = half speed for comfortable observation
        speedSlider = new JSlider(JSlider.HORIZONTAL, 5, 200, 50);
        speedSlider.setPreferredSize(new Dimension(200, 40));
        speedSlider.setMajorTickSpacing(50);  // Show major ticks at 0.5x, 1.0x, 1.5x, 2.0x
        speedSlider.setMinorTickSpacing(5);   // Show minor ticks every 0.05x
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        speedValueLabel = new JLabel("0.5x", JLabel.LEFT);
        speedValueLabel.setFont(GuiStyles.LABEL_FONT);
        speedValueLabel.setPreferredSize(new Dimension(50, 20));
        
        // Update simulation speed whenever slider changes
        speedSlider.addChangeListener((ChangeEvent e) -> {
            // Convert slider value (5-200) to simulation speed (0.05x-2.0x)
            float speed = speedSlider.getValue() / 100.0f;
            Gui.setSpeedValue(speed);
            speedValueLabel.setText(String.format("%.1fx", speed));
        });
        
        // Quick reset button to return to half-speed (optimal for analysis)
        JButton resetSpeedButton = new JButton("Normal Speed");
        resetSpeedButton.addActionListener((ActionEvent e) -> speedSlider.setValue(50)); // Set to 0.5x speed
        
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
        
        votesToCloseField = new JTextField("3", 3);
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
            if (Gui.getRestarter() != null) {
                try {
                    new Thread(Gui.getRestarter()).start();
                    // Update button state immediately for better user feedback
                    startButton.setText("Server Restarting...");
                    restartButton.setEnabled(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Gui.getFrame(), 
                        "Error restarting server: " + ex.getMessage(),
                        "Restart Error", JOptionPane.ERROR_MESSAGE);
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
                "Are you sure you want to exit the program?", "Exit Program",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmed == JOptionPane.YES_OPTION) {
                System.exit(0);
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

        if (startButton != null) {
            startButton.setEnabled(true);
            startButton.setText("Start Server");
        }
        if (restartButton != null) {
            restartButton.setEnabled(false);
        }
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
     * Load the log file into the table
     */
    public void loadLogFile() {
        try {
            // Ensure table model is initialized before loading
            if (tableModel == null) {
                System.out.println("Table model not initialized yet - initializing now");
                createTablePanel();
            }
            
            // Clear current table data
            tableModel.setRowCount(0);
            
            // Try multiple times with a delay if the file doesn't exist immediately
            // This addresses the case where the log file is created during the simulation
            File logFile = new File("log.txt");
            int retryCount = 0;
            int maxRetries = 5;
            
            while (!logFile.exists() && retryCount < maxRetries) {
                System.out.println("Log file not found yet, waiting... (Attempt " + (retryCount + 1) + "/" + maxRetries + ")");
                retryCount++;
                
                // Wait before retrying
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                // Check again
                logFile = new File("log.txt");
            }
            
            if (!logFile.exists()) {
                // File still doesn't exist after retries, look for alternatives
                File currentDir = new File(".");
                System.out.println("Looking for log.txt in: " + currentDir.getAbsolutePath());
                
                // Check if there's a log file with a different name or in a different location
                File[] files = currentDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        System.out.println("Found text file: " + f.getName());
                    }
                    
                    // Ask user if they want to use one of the found files
                    if (files.length == 1) {
                        int response = JOptionPane.showConfirmDialog(
                            Gui.getFrame(),
                            "Log file not found. Use " + files[0].getName() + " instead?",
                            "File Not Found",
                            JOptionPane.YES_NO_OPTION
                        );
                        
                        if (response == JOptionPane.YES_OPTION) {
                            logFile = files[0];
                        } else {
                            return;
                        }
                    } else {
                        // If there are multiple files, let the user choose
                        String[] options = new String[files.length];
                        for (int i = 0; i < files.length; i++) {
                            options[i] = files[i].getName();
                        }
                        
                        String selectedFile = (String) JOptionPane.showInputDialog(
                            Gui.getFrame(),
                            "Log.txt not found. Select a text file to use:",
                            "Choose Log File",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                        );
                        
                        if (selectedFile != null) {
                            for (File f : files) {
                                if (f.getName().equals(selectedFile)) {
                                    logFile = f;
                                    break;
                                }
                            }
                        } else {
                            return;
                        }
                    }
                } else {
                    // Log file not found - silently handle without showing error dialog
                    System.out.println("Log file not found at startup: " + logFile.getAbsolutePath());
                    System.out.println("Will attempt to load log file again later when simulation starts");
                    return;
                }
            }
            
            // Check if the file is empty or still being written
            if (logFile.length() == 0) {
                System.out.println("Log file exists but is empty. Waiting for content...");
                try {
                    Thread.sleep(1000); // Wait a moment for content to appear
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                if (logFile.length() == 0) {
                    // Log file exists but is empty - silently handle without dialog
                    System.out.println("Log file exists but is empty. Will check again later when simulation generates data.");
                    return;
                }
            }
            
            System.out.println("Loading log file: " + logFile.getAbsolutePath());
            
            // Read the log file
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            
            // Read and parse log entries
            int entriesAdded = 0;
            while ((line = reader.readLine()) != null) {
                // Skip header lines
                if (line.contains("Door") && line.contains("Voter") && line.contains("Clerk")) {
                    continue;
                }
                // Skip delimiter lines
                if (line.startsWith("+") || line.startsWith("-")) {
                    continue;
                }
                // Skip the simulation parameters line
                if (line.contains("Total of Voters:") || line.contains("Votes to end:")) {
                    continue;
                }
                
                if (parseLine(line)) {
                    entriesAdded++;
                }
            }
            
            reader.close();
            
            // Notify user about loaded entries
            if (entriesAdded == 0) {
                JOptionPane.showMessageDialog(Gui.getFrame(), 
                    "No valid log entries found in log file.",
                    "Empty Log", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("Successfully loaded " + entriesAdded + " log entries");
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Gui.getFrame(), 
                "Error loading log file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
