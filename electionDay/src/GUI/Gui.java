package GUI;

import Logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class Gui {
    // Log file path
    private static final String LOG_FILE = "log.txt";
    
    // GUI components
    private static JFrame frame;
    private static JPanel stationPanel;
    private static JPanel queuePanel;
    private static JPanel boothPanel;
    private static JPanel resultsPanel;
    private static JLabel scoreALabel;
    private static JLabel scoreBLabel;
    private static JLabel queueLabel;
    private static JLabel boothLabel;
    private static DefaultTableModel tableModel;
    private static JTable logTable;
    private static JButton startButton;
    private static JButton exitButton;
    
    // Simulation state
    private static int scoreA = 0;
    private static int scoreB = 0;
    private static int votersProcessed = 0;
    private static boolean stationOpen = false;
    private static boolean simulationRunning = false;
    private static int queueSize = 0;
    private static String currentVoterInBooth = "";
    
    // Callback for when Start Simulation button is pressed
    private static Runnable simulationStarter;
    
    // Create a new GUI window
    public static void window() {
        // Create the main window
        frame = new JFrame("Election Day Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());
        
        // Create the components
        createStatusPanel();
        createTablePanel();
        createButtonPanel();
        
        // Display the window
        frame.setVisible(true);
    }
    
    // Method to set the simulation starter callback
    public static void setSimulationStarter(Runnable starter) {
        simulationStarter = starter;
    }
    
    private static void createStatusPanel() {
        JPanel statusPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Polling Station Status Panel
        stationPanel = new JPanel();
        stationPanel.setLayout(new BorderLayout());
        stationPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Polling Station", 
            TitledBorder.CENTER, TitledBorder.TOP));
        JLabel stationStatusLabel = new JLabel("CLOSED", JLabel.CENTER);
        stationStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        stationStatusLabel.setForeground(Color.RED);
        stationPanel.add(stationStatusLabel, BorderLayout.CENTER);
        
        // Voter Queue Panel
        queuePanel = new JPanel();
        queuePanel.setLayout(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Voter Queue", 
            TitledBorder.CENTER, TitledBorder.TOP));
        queueLabel = new JLabel("Empty", JLabel.CENTER);
        queueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        queuePanel.add(queueLabel, BorderLayout.CENTER);
        
        // Voting Booth Panel
        boothPanel = new JPanel();
        boothPanel.setLayout(new BorderLayout());
        boothPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Voting Booth", 
            TitledBorder.CENTER, TitledBorder.TOP));
        boothLabel = new JLabel("Available", JLabel.CENTER);
        boothLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        boothPanel.add(boothLabel, BorderLayout.CENTER);
        
        // Results Panel
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new GridLayout(3, 1));
        resultsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Election Results", 
            TitledBorder.CENTER, TitledBorder.TOP));
        
        scoreALabel = new JLabel("Candidate A: 0", JLabel.CENTER);
        scoreALabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreALabel.setForeground(new Color(0, 102, 204));
        
        scoreBLabel = new JLabel("Candidate B: 0", JLabel.CENTER);
        scoreBLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreBLabel.setForeground(new Color(204, 0, 0));
        
        JLabel processedLabel = new JLabel("Processed: 0", JLabel.CENTER);
        processedLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        resultsPanel.add(scoreALabel);
        resultsPanel.add(scoreBLabel);
        resultsPanel.add(processedLabel);
        
        // Add all panels to the status panel
        statusPanel.add(stationPanel);
        statusPanel.add(queuePanel);
        statusPanel.add(boothPanel);
        statusPanel.add(resultsPanel);
        
        // Add status panel to the main frame
        frame.add(statusPanel, BorderLayout.NORTH);
    }
    
    private static void createTablePanel() {
        // Create columns for the log table
        String[] columns = {"Door", "Voter", "Clerk", "Validation", "Booth", "ScoreA", "ScoreB", "Exit"};
        tableModel = new DefaultTableModel(columns, 0);
        logTable = new JTable(tableModel);
        logTable.setFillsViewportHeight(true);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Simulation Log", 
            TitledBorder.CENTER, TitledBorder.TOP));
        
        frame.add(scrollPane, BorderLayout.CENTER);
    }
    
    private static void createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // Add Start Simulation button
        startButton = new JButton("Start Simulation");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!simulationRunning && simulationStarter != null) {
                    simulationRunning = true;
                    startButton.setEnabled(false);
                    startButton.setText("Simulation Running...");
                    new Thread(() -> simulationStarter.run()).start();
                }
            }
        });
        
        // Add Exit Program button
        exitButton = new JButton("Exit Program");
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setForeground(new Color(204, 0, 0));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to exit the program?", "Exit Program",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirmed == JOptionPane.YES_OPTION) {
                    // Save any unsaved data if necessary
                    System.exit(0);
                }
            }
        });
        
        JButton loadButton = new JButton("Load Log File");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLogFile();
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLogFile();
            }
        });
        
        buttonPanel.add(startButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exitButton);
        
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private static void loadLogFile() {
        try {
            // Clear current table data
            tableModel.setRowCount(0);
            
            // Read the log file
            BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE));
            String line;
            
            // Read header info (first line)
            if ((line = reader.readLine()) != null) {
                parseHeaderInfo(line);
            }
            
            // Skip the column headers and delimiter lines
            reader.readLine(); // column headers
            reader.readLine(); // delimiter
            
            // Read and parse log entries
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
            
            reader.close();
            
            // Update the UI with the latest data
            updateUI();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, 
                "Error loading log file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void parseHeaderInfo(String headerLine) {
        // Parse header info like "Total of Voters:5, Total Voters in Stations:2, Votes to end: 10 voters"
        // This information could be displayed in the UI
    }
    
    private static void parseLine(String line) {
        // Parse each line of the log file table
        if (line.trim().isEmpty()) return;
        
        String[] columns = new String[8];
        line = line.trim();
        
        // Skip lines that don't match our expected format
        if (!line.startsWith("|")) return;
        
        // Split the line by the pipe character and extract each column
        String[] parts = line.split("\\|");
        if (parts.length >= 9) {
            for (int i = 1; i < 9; i++) {
                columns[i-1] = parts[i].trim();
            }
            
            // Add the row to the table model
            tableModel.addRow(columns);
            
            // Update state based on the line
            updateStateFromLine(columns);
        }
    }
    
    private static void updateStateFromLine(String[] columns) {
        // Door status
        if ("Op".equals(columns[0])) {
            stationOpen = true;
        } else if ("Cl".equals(columns[0])) {
            stationOpen = false;
        }
        
        // Update queue info
        if (!columns[1].isEmpty()) {
            // Voter in queue or entering
            queueSize++;
        }
        
        // Update booth info
        if (!columns[4].isEmpty()) {
            currentVoterInBooth = columns[4];
        }
        
        // Score updates
        if (!columns[5].isEmpty()) {
            try {
                scoreA = Integer.parseInt(columns[5]);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        
        if (!columns[6].isEmpty()) {
            try {
                scoreB = Integer.parseInt(columns[6]);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        
        // Count processed voters
        if (!columns[7].isEmpty()) {
            votersProcessed++;
            // A voter exited, decrease queue size
            queueSize = Math.max(0, queueSize - 1);
        }
    }
    
    private static void updateUI() {
        // Update station status
        JLabel stationStatusLabel = (JLabel)stationPanel.getComponent(0);
        if (stationOpen) {
            stationStatusLabel.setText("OPEN");
            stationStatusLabel.setForeground(new Color(0, 153, 0));
        } else {
            stationStatusLabel.setText("CLOSED");
            stationStatusLabel.setForeground(Color.RED);
            
            // If station is closed and we were running a simulation, update the button
            if (simulationRunning) {
                startButton.setText("Simulation Finished");
                // Keep button disabled since simulation is complete
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
    
    // Method to update the UI with specific queue and booth information
    public static void updateQueueAndBoothInfo(int queueCount, String voterInBooth) {
        queueSize = queueCount;
        currentVoterInBooth = voterInBooth;
        updateUI();
    }
    
    // Method to link with the Logger for real-time updates
    public static void updateFromLogger(Logger logger) {
        if (logger != null) {
            try {
                scoreA = Integer.parseInt(logger.getVoteCounts().split(", ")[0].split(": ")[1]);
                scoreB = Integer.parseInt(logger.getVoteCounts().split(", ")[1].split(": ")[1]);
                votersProcessed = logger.getVotersProcessed();
                stationOpen = logger.isStationOpen();
                updateUI();
            } catch (Exception e) {
                // Handle any parsing errors silently
                System.err.println("Error updating GUI from logger: " + e.getMessage());
            }
        }
    }
}
