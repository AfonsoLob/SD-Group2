package Logging;

import GUI.Gui;
import Interfaces.GUI.IGUI_Statistics;
import Interfaces.GUI.IGUI_Voter;
import Interfaces.Logger.ILogger_all;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Logger implements ILogger_all {
    // Constants for column headers
    private static final String[] HEADERS = {"Door", "Voter", "Clerk", "Validation", "Booth", "ScoreA", "ScoreB", "Exit", "ExitPollA", "ExitPollB"};
    private static final int NUM_COLUMNS = HEADERS.length;
    private PrintWriter writer;
    
    // Locks for thread safety
    private final ReentrantLock logLock = new ReentrantLock();
    private final ReentrantLock stateLock = new ReentrantLock();
    
    // Store the state of the system
    private List<String[]> logEntries;
    
    // Counters
    private Integer votersProcessed;
    private Integer scoreA;
    private Integer scoreB;
    private Integer exitPollScoreA;
    private Integer exitPollScoreB;

    // Election properties
    private int maxVoters;
    private int maxCapacity;
    private int maxVotes;
    
    // Delimiter for the table
    private static final String DELIMITER = "-----------------------------------------------------------------------------------------------------------------------------------";
    
    // Additional state tracking
    private boolean isStationOpen = false;
    private String currentVoterInBooth = "";
    private int currentQueueSize = 0;
    
    // Map to track voter rebirths
    private java.util.Map<Integer, Integer> voterRebirthMap = new java.util.HashMap<>();
    
    // Map to track voter last activity for rebirth detection
    private Map<Integer, Long> lastVoterActivity = new HashMap<>();
    
    // Statistics tracking
    private int validationSuccess = 0;
    private int validationFail = 0;
    private int pollParticipants = 0;
    private int pollTotal = 0;
    private int pollAccurate = 0;
    private int pollResponses = 0;
    
    // Timing statistics
    private Map<Integer, Long> voterEntryTimes = new HashMap<>();
    private Map<Integer, Long> voterExitTimes = new HashMap<>();
    private long totalProcessingTime = 0;
    private int processedVotersWithTime = 0;
    
    private IGUI_Voter guiVoter;
    private IGUI_Statistics guiStats;
    
    private Logger(int maxVoters, int maxCapacity, int maxVotes) {
        this.logEntries = new ArrayList<>();
        this.votersProcessed = 0;
        this.scoreA = 0;
        this.scoreB = 0;
        this.exitPollScoreA = 0;
        this.exitPollScoreB = 0;
        this.maxVoters = maxVoters;
        this.maxCapacity = maxCapacity;
        this.maxVotes = maxVotes;

        // Get the GUI interfaces
        this.guiVoter = Gui.getInstance();
        this.guiStats = Gui.getInstance();

        // Initiate printWriter
        try {
            // Make sure to use a path that's accessible and show the path for debugging
            File logFile = new File("log.txt");
            System.out.println("Creating log file at: " + logFile.getAbsolutePath());
            
            this.writer = new PrintWriter(logFile);
            // Print the header information
            writer.println("Total of Voters:" + this.maxVoters + 
                  ", Total Voters in Stations:" + this.maxCapacity + 
                  ", Votes to end: " + this.maxVotes + " voters");
            
            // Print column headers
            StringBuilder headerBuilder = new StringBuilder("|");
            for (String header : HEADERS) {
                headerBuilder.append(String.format(" %-10s |", header));
            }
            writer.println(headerBuilder.toString());
            
            // Print delimiter
            writer.println(DELIMITER);
            writer.flush(); // Force flush to ensure data is written
        } catch (Exception e) {
            System.err.println("Error creating log file:");
            e.printStackTrace();
        }
    }
    
    public static Logger getInstance(int maxVoters, int maxCapacity, int maxVotes) {
        return new Logger(maxVoters, maxCapacity, maxVotes);
    }

    /**
     * Add a new log entry
     * @param door activity at the door
     * @param voter voter in the queue
     * @param clerk clerk activity
     * @param validation ID validation activity
     * @param booth voting booth activity
     * @param exit exit poll activity
     */
    private void addEntry(String door, String voter, String clerk, String validation, 
                                     String booth, String exit) {
        logLock.lock();
        try {
            String[] entry = new String[NUM_COLUMNS];
            entry[0] = door;
            entry[1] = voter;
            entry[2] = clerk;
            entry[3] = validation;
            entry[4] = booth;
            if(!booth.isEmpty()) {
                entry[5] = String.format("%02d", scoreA);
                entry[6] = String.format("%02d", scoreB);
            }
            entry[7] = exit;
            if(!exit.isEmpty()) {
                entry[8] = String.format("%02d", exitPollScoreA);
                entry[9] = String.format("%02d", exitPollScoreB);
            }
            
            printStateToFile(entry);
        } finally {
            logLock.unlock();
        }
    }
    
    private void incrementScoreA() {
        stateLock.lock();
        try {
            scoreA++;
        } finally {
            stateLock.unlock();
        }
    }
    
    private void incrementScoreB() {
        stateLock.lock();
        try {
            scoreB++;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void voterAtDoor(int voterId) {
        stateLock.lock();
        try {
            // Record entry time for timing statistics
            voterEntryTimes.put(voterId, System.currentTimeMillis());
            
            // Check if this might be a rebirth (a new voter ID appearing)
            // If this ID is over 1000, it might be a rebirth
            if (voterId >= 1000 && !lastVoterActivity.containsKey(voterId)) {
                // Check who disappeared recently (within last 500ms)
                long currentTime = System.currentTimeMillis();
                int possibleOriginalId = voterId - 1000;
                
                if (lastVoterActivity.containsKey(possibleOriginalId)) {
                    long lastActivity = lastVoterActivity.get(possibleOriginalId);
                    if (currentTime - lastActivity < 500) {
                        // This is likely a rebirth
                        guiVoter.voterReborn(possibleOriginalId, voterId);
                    }
                }
            }
            
            addEntry("", String.valueOf(voterId), "", "", "", "");
            lastVoterActivity.put(voterId, System.currentTimeMillis());
            
            // Use instance method through interface
            guiVoter.voterArrived(voterId);
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void voterEnteringQueue(int voterId) {
        stateLock.lock();
        try {
            currentQueueSize++;
            addEntry("", "", String.valueOf(voterId), "", "", "");
            
            // Use instance method through interface
            guiVoter.voterEnteringQueue(voterId);
        } finally {
            stateLock.unlock();
        }
    }
        
    @Override
    public void validatingVoter(int voterId, boolean valid) {
        stateLock.lock();
        try {
            // Track validation statistics
            if (valid) {
                validationSuccess++;
            } else {
                validationFail++;
            }
            
            String validationStr = String.valueOf(voterId) + (valid ? "+" : "-");
            addEntry("", "", "", validationStr, "", "");
            
            // Use instance method through interface
            guiVoter.voterValidated(voterId, valid);
            
            // Update statistics in the GUI
            updateGuiStatistics();
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void voterInBooth(int voterId, boolean voteA) {
        stateLock.lock();
        try {
            String boothStr = String.valueOf(voterId) + (voteA ? "A" : "B");
            currentVoterInBooth = boothStr;
            
            // Update scores
            if (voteA) {
                incrementScoreA();
            } else {
                incrementScoreB();
            }

            addEntry("", "", "", "", boothStr, "");
            
            // Use instance method through interface
            guiVoter.voterVoting(voterId, voteA);
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void exitPollVote(int voterId, String vote) {
        stateLock.lock();
        try {
            // Record exit time for timing statistics
            long exitTime = System.currentTimeMillis();
            voterExitTimes.put(voterId, exitTime);
            
            // If we have an entry time, calculate processing time
            if (voterEntryTimes.containsKey(voterId)) {
                long entryTime = voterEntryTimes.get(voterId);
                long processingTime = exitTime - entryTime;
                totalProcessingTime += processingTime;
                processedVotersWithTime++;
            }
            
            // Track exit poll statistics
            pollTotal++;
            
            if (!vote.isEmpty()) {
                pollParticipants++;
                pollResponses++;
                
                // Check if the exit poll matches the actual vote
                // Note: This assumes we're tracking the actual vote somewhere
                String actualVote = getActualVote(voterId);
                if (vote.equals(actualVote)) {
                    pollAccurate++;
                }
            }
            
            String exitStr = voterId + vote;
            if (vote.equals("A")) {
                exitPollScoreA++;
            } else if (vote.equals("B")) {
                exitPollScoreB++;
            }
            currentQueueSize = Math.max(0, currentQueueSize - 1);
            currentVoterInBooth = ""; // Clear booth
            addEntry("", "", "", "", "", exitStr);
            votersProcessed++;
            
            // Update last activity time for rebirth detection
            lastVoterActivity.put(voterId, System.currentTimeMillis());
            
            // Use instance method through interface
            guiVoter.voterExitPoll(voterId, vote);
            
            // Update statistics in the GUI
            updateGuiStatistics();
        } finally {
            stateLock.unlock();
        }
    }
    
    // Helper method to get the actual vote for a voter
    // This is a simplification - in a real system, you would track this separately
    private String getActualVote(int voterId) {
        // For this example, we'll use the current score as an approximation
        // In a real implementation, you would track each voter's actual vote
        return (scoreA > scoreB) ? "A" : "B";
    }
    
    // Helper method to update GUI statistics
    private void updateGuiStatistics() {
        try {
            // Calculate average processing time
            long avgProcessingTime = 0;
            if (processedVotersWithTime > 0) {
                avgProcessingTime = totalProcessingTime / processedVotersWithTime;
            }
            
            guiStats.updateStats(
                validationSuccess,
                validationFail,
                pollParticipants,
                pollTotal,
                pollAccurate,
                pollResponses,
                avgProcessingTime
            );
        } catch (Exception e) {
            System.err.println("Error updating GUI stats: " + e.getMessage());
        }
    }
    
    @Override
    public void stationOpening() {
        stateLock.lock();
        try {
            isStationOpen = true;
            addEntry("Op", "", "", "", "", "");
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void stationClosing() {
        stateLock.lock();
        try {
            isStationOpen = false;
            addEntry("Cl", "", "", "", "", "");
        } finally {
            stateLock.unlock();
        }
    }
    
    private void printStateToFile(String[] entry) {
        // Using logLock which is already acquired in addEntry
        // Print each log entry
        StringBuilder rowBuilder = new StringBuilder("|");
        for (String field : entry) {
            rowBuilder.append(String.format(" %-10s |", field != null ? field : ""));
        }
        try {
            writer.println(rowBuilder.toString());     
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }

    @Override
    public void saveCloseFile() {
        logLock.lock();
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                System.out.println("Log file saved and closed");
            }
        } catch (Exception e) {
            System.err.println("Error closing log file: " + e.getMessage());
        } finally {
            logLock.unlock();
        }
    }
    
    @Override
    public void clear() {
        logLock.lock();
        try {
            logEntries.clear();
        } finally {
            logLock.unlock();
        }
    }
    
    @Override
    public String getVoteCounts() {
        stateLock.lock();
        try {
            return "Candidate A: " + scoreA + ", Candidate B: " + scoreB;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public int getVotersProcessed() {
        stateLock.lock();
        try {
            return votersProcessed;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isStationOpen() {
        stateLock.lock();
        try {
            return isStationOpen;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public String getCurrentVoterInBooth() {
        stateLock.lock();
        try {
            return currentVoterInBooth;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public int getCurrentQueueSize() {
        stateLock.lock();
        try {
            return currentQueueSize;
        } finally {
            stateLock.unlock();
        }
    }
}