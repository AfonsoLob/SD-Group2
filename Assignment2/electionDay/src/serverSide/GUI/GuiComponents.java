package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private JButton exitButton;
    private JButton restartButton;
    private JSlider speedSlider;
    private JLabel speedValueLabel;
    
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
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Convert slider value (5-200) to simulation speed (0.05x-2.0x)
                float speed = speedSlider.getValue() / 100.0f;
                Gui.setSpeedValue(speed);
                speedValueLabel.setText(String.format("%.1fx", speed));
            }
        });
        
        // Quick reset button to return to half-speed (optimal for analysis)
        JButton resetSpeedButton = new JButton("Normal Speed");
        resetSpeedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speedSlider.setValue(50); // Set to 0.5x speed
            }
        });
        
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(speedValueLabel);
        controlPanel.add(resetSpeedButton);
        
        return controlPanel;
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
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // Add Start Simulation button
        startButton = new JButton("Start Simulation");
        startButton.setFont(GuiStyles.LABEL_FONT);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Gui.isSimulationRunning() && Gui.getSimulationStarter() != null) {
                    Gui.setSimulationRunning(true);
                    startButton.setEnabled(false);
                    startButton.setText("Simulation Running...");
                    new Thread(() -> Gui.getSimulationStarter().run()).start();
                }
            }
        });
        
        // Add Exit Program button
        exitButton = new JButton("Exit Program");
        exitButton.setFont(GuiStyles.LABEL_FONT);
        exitButton.setForeground(GuiStyles.ERROR_COLOR);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(Gui.getFrame(), 
                    "Are you sure you want to exit the program?", "Exit Program",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirmed == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        
        // Removed load and refresh buttons
        
        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);
        
        return buttonPanel;
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
                // Keep button disabled since simulation is complete
                
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
            // Clear current table data
            tableModel.setRowCount(0);
            
            // Check if file exists first
            File logFile = new File("log.txt");
            if (!logFile.exists()) {
                JOptionPane.showMessageDialog(Gui.getFrame(), 
                    "Log file not found: " + logFile.getAbsolutePath(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Read the log file
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            
            // Read header info (first line)
            // if ((line = reader.readLine()) != null) {
            //     System.out.println("Header info: " + line);
            // }
            
            // Skip the column headers and delimiter lines
            reader.readLine(); // column headers
            reader.readLine(); // delimiter
            
            // Read and parse log entries
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
            
            reader.close();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(Gui.getFrame(), 
                "Error loading log file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Parse a line from the log file
     */
    private void parseLine(String line) {
        if (line.trim().isEmpty()) return;
        
        String[] columns = new String[8];
        line = line.trim();
        
        // Skip lines that don't match our expected format
        if (!line.startsWith("|")) return;
        
        try {
            // Split the line by the pipe character and extract each column
            String[] parts = line.split("\\|");
            if (parts.length >= 9) {
                for (int i = 1; i < 9; i++) {
                    columns[i-1] = parts[i].trim();
                }
                
                // Add the row to the table model
                tableModel.addRow(columns);
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            e.printStackTrace();
        }
    }
    
    // Getters for components
    public JPanel getStationPanel() { return stationPanel; }
    public JTable getLogTable() { return logTable; }
}
