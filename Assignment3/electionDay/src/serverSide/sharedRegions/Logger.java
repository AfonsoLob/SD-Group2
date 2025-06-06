package serverSide.sharedRegions;

import java.io.File;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import interfaces.Logger.ILogger_all;
import serverSide.GUI.Gui;

public final class Logger extends UnicastRemoteObject implements ILogger_all {
    private static final long serialVersionUID = 1L;
    
    // Constants for column headers
    private static final String[] HEADERS = {"Door", "Voter", "Clerk", "Validation", "Booth", "ScoreA", "ScoreB", "Exit", "ExitPollA", "ExitPollB"};
    private static final int NUM_COLUMNS = HEADERS.length;
    private transient PrintWriter writer;
    
    // Locks for thread safety (transient as they cannot be serialized)
    private final transient ReentrantLock logLock = new ReentrantLock();
    private final transient ReentrantLock stateLock = new ReentrantLock();
    
    // Store the state of the system
    private transient List<String[]> logEntries;
    
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
    
    // Map to track voter last activity for rebirth detection
    private transient Map<Integer, Long> lastVoterActivity = new HashMap<>();
    
    // Statistics tracking
    private int validationSuccess = 0;
    private int validationFail = 0;
    private int pollParticipants = 0;
    private int pollTotal = 0;
    private int pollAccurate = 0;
    private int pollResponses = 0;
    
    // Timing statistics
    private transient Map<Integer, Long> voterEntryTimes = new HashMap<>();
    private long totalProcessingTime = 0;
    private int processedVotersWithTime = 0;
    
    // Direct reference to GUI instance (transient as GUI cannot be serialized)
    private transient Gui gui;
    
    private Logger(int maxVoters, int maxCapacity, int maxVotes) throws RemoteException {
        super(); // Call to UnicastRemoteObject constructor

        this.logEntries = new ArrayList<>();
        this.votersProcessed = 0;
        this.scoreA = 0;
        this.scoreB = 0;
        this.exitPollScoreA = 0;
        this.exitPollScoreB = 0;
        this.maxVoters = maxVoters;
        this.maxCapacity = maxCapacity;
        this.maxVotes = maxVotes;

        // Get the GUI instance directly
        this.gui = Gui.getInstance();

        // // Set configuration values using static methods from GUI
        // this.maxVotes = Gui.getConfigVotesToClose();
        // this.maxCapacity = Gui.getConfigQueueSize();
        // this.maxVoters = Gui.getConfigNumVoters();

        // Initiate printWriter
        try {
            // Create log file in the current working directory (same as GUI expects)
            File logFile = new File("log.txt");
            System.out.println("Creating log file at: " + logFile.getAbsolutePath());
            
            this.writer = new PrintWriter(logFile);
            // Update log format to match Assignment 2
            writer.println("Total of Voters:" + this.maxVoters + ", Total Voters in Stations:" + this.maxCapacity + ", Votes to end: " + this.maxVotes + " voters");
            writer.println("| Door       | Voter      | Clerk      | Validation | Booth      | ScoreA     | ScoreB     | Exit       | ExitPollA  | ExitPollB  |");
            writer.println(DELIMITER);
            writer.flush(); // Force flush to ensure data is written
        } catch (Exception e) {
            System.err.println("Error creating log file: " + e.getMessage());
            // Don't print stack trace in production code
        }
    }
    
    public static Logger getInstance(int maxVoters, int maxCapacity, int maxVotes) throws RemoteException {
        return new Logger(maxVoters, maxCapacity, maxVotes);
    }

    /**
     * Add a new log entry
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
            // Show ScoreA/ScoreB when booth column has data (when someone votes)
            if(!booth.isEmpty()) {
                entry[5] = String.format("%02d", scoreA);
                entry[6] = String.format("%02d", scoreB);
            } else {
                entry[5] = "";
                entry[6] = "";
            }
            entry[7] = exit;
            // Show ExitPollA/ExitPollB when exit column has data (when someone exits)
            if(!exit.isEmpty()) {
                entry[8] = String.format("%02d", exitPollScoreA);
                entry[9] = String.format("%02d", exitPollScoreB);
            } else {
                entry[8] = "";
                entry[9] = "";
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
    public void voterAtDoor(int voterId) throws RemoteException {
        stateLock.lock();
        try {
            // Record entry time for timing statistics
            voterEntryTimes.put(voterId, System.currentTimeMillis());
            
            // Check if this might be a rebirth (a new voter ID appearing)
            if (voterId >= 1000 && !lastVoterActivity.containsKey(voterId)) {
                long currentTime = System.currentTimeMillis();
                int possibleOriginalId = voterId - 1000;
                
                if (lastVoterActivity.containsKey(possibleOriginalId)) {
                    long lastActivity = lastVoterActivity.get(possibleOriginalId);
                    if (currentTime - lastActivity < 500) {
                        gui.voterReborn(possibleOriginalId, voterId);
                    }
                }
            }
            
            addEntry("", String.valueOf(voterId), "", "", "", "");
            lastVoterActivity.put(voterId, System.currentTimeMillis());
            
            gui.voterArrived(voterId);
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void voterEnteringQueue(int voterId) throws RemoteException {
        stateLock.lock();
        try {
            currentQueueSize++;
            addEntry("", "", String.valueOf(voterId), "", "", "");
            gui.voterEnteringQueue(voterId);
        } finally {
            stateLock.unlock();
        }
    }
        
    @Override
    public void validatingVoter(int voterId, int valid) throws RemoteException {
        stateLock.lock();
        try {
            boolean validp = (valid == 1);
            
            // Track validation statistics
            if (validp) {
                validationSuccess++;
            } else {
                validationFail++;
            }
            
            String validationStr = String.valueOf(voterId) + (validp ? "+" : "-");
            addEntry("", "", "", validationStr, "", "");
            
            gui.voterValidated(voterId, valid);
            updateGuiStatistics();
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void voterInBooth(int voterId, boolean voteA) throws RemoteException {
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
            gui.voterVoting(voterId, voteA);
            updateGuiStatistics();
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void exitPollVote(int voterId, String vote) throws RemoteException {
        stateLock.lock();
        try {
            // Record exit time for timing statistics
            long exitTime = System.currentTimeMillis();
            
            // If we have an entry time, calculate processing time
            if (voterEntryTimes.containsKey(voterId)) {
                long entryTime = voterEntryTimes.get(voterId);
                long processingTime = exitTime - entryTime;
                totalProcessingTime += processingTime;
                processedVotersWithTime++;
                // Remove the entry time to free memory
                voterEntryTimes.remove(voterId);
            }
            
            // Track exit poll statistics
            pollTotal++;
            
            if (!vote.isEmpty()) {
                pollParticipants++;
                pollResponses++;
                
                // Check if the exit poll matches the actual vote
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
            
            gui.voterExitPoll(voterId, vote);
            updateGuiStatistics();
        } finally {
            stateLock.unlock();
        }
    }
    
    // Helper method to get the actual vote for a voter
    private String getActualVote(int voterId) {
        // For this example, we'll use the current score as an approximation
        // In a real implementation, you would track each voter's actual vot
        System.out.println("Getting vote for voter " + voterId); // Using the parameter
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
            
            gui.updateStats(
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
    public void stationOpening() throws RemoteException {
        stateLock.lock();
        try {
            isStationOpen = true;
            addEntry("Op", "", "", "", "", "");
            // Update GUI
            gui.stationOpening();
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public void stationClosing() throws RemoteException {
        stateLock.lock();
        try {
            isStationOpen = false;
            addEntry("Cl", "", "", "", "", "");
            // Update GUI
            gui.stationClosing();
        } finally {
            stateLock.unlock();
        }
    }
    
    private void printStateToFile(String[] entry) {
        // Print each log entry
        StringBuilder rowBuilder = new StringBuilder("|");
        for (String field : entry) {
            rowBuilder.append(String.format(" %-10s |", field != null ? field : ""));
        }
        try {
            writer.println(rowBuilder.toString());     
            writer.flush();
        } catch (Exception e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }   
    }

    @Override
    public void saveCloseFile() throws RemoteException {
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
    public void clear() throws RemoteException {
        logLock.lock();
        try {
            logEntries.clear();
        } finally {
            logLock.unlock();
        }
    }
    
    @Override
    public String getVoteCounts() throws RemoteException {
        stateLock.lock();
        try {
            return "Candidate A: " + scoreA + ", Candidate B: " + scoreB;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public int getVotersProcessed() throws RemoteException {
        stateLock.lock();
        try {
            return votersProcessed;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isStationOpen() throws RemoteException {
        stateLock.lock();
        try {
            return isStationOpen;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public String getCurrentVoterInBooth() throws RemoteException {
        stateLock.lock();
        try {
            return currentVoterInBooth;
        } finally {
            stateLock.unlock();
        }
    }
    
    @Override
    public int getCurrentQueueSize() throws RemoteException {
        stateLock.lock();
        try {
            return currentQueueSize;
        } finally {
            stateLock.unlock();
        }
    }

    // Methods from ILogger_Common
    @Override
    public int getNumVoters() throws RemoteException {
        return maxVoters;
    }

    // Additional methods for ILogger_PollingStation
    @Override
    public int getPollingStationCapacity() throws RemoteException {
        return maxCapacity > 0 ? maxCapacity : 2; // Default capacity of 2
    }

    @Override
    public int getNumberOfVotersConfigured() throws RemoteException {
        return maxVoters;
    }

    @Override
    public int getMaxVotes() throws RemoteException {
        return maxVotes;
    }

    @Override
    public int getTotalVotes() throws RemoteException {
        stateLock.lock();
        try {
            return scoreA + scoreB;
        } finally {
            stateLock.unlock();
        }
    }
}
