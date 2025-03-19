package Logging;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    // Constants for column headers
    private static final String[] HEADERS = {"Door", "Voter", "Clerk", "Validation", "Booth", "ScoreA", "ScoreB", "Exit", "ExitPollA", "ExitPollB"};
    private static final int NUM_COLUMNS = HEADERS.length;
    private PrintWriter writer;
    
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

        // Initiate printWriter
        try {
            this.writer = new PrintWriter("log.txt");
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
        } catch (Exception e) {
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
    private synchronized void addEntry(String door, String voter, String clerk, String validation, 
                                     String booth, String exit) {
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
    }
    
    private void incrementScoreA() {
        scoreA++;
    }
    
    private void incrementScoreB() {
        scoreB++;
    }
    
    public synchronized void voterAtDoor(int voterId) {
        addEntry("", String.valueOf(voterId), "", "", "", "");
    }
    
    public synchronized void voterEnteringQueue(int voterId) {
        currentQueueSize++;
        addEntry("", "", String.valueOf(voterId), "", "", "");
    }
        
    public synchronized void validatingVoter(int voterId, boolean valid) {
        String validationStr = String.valueOf(voterId) + (valid ? "+" : "-");
        addEntry("", "", "", validationStr, "", "");
    }
    
    public synchronized void voterInBooth(int voterId, boolean voteA) {
        String boothStr = String.valueOf(voterId) + (voteA ? "A" : "B");
        currentVoterInBooth = boothStr;
        
        // Update scores
        if (voteA) {
            incrementScoreA();
        } else {
            incrementScoreB();
        }

        addEntry("", "", "", "", boothStr, "");
    }
    
    public synchronized void exitPollVote(int voterId, String vote) {
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
    }
    
    public synchronized void stationOpening() {
        isStationOpen = true;
        addEntry("Op", "", "", "", "", "");
    }
    
    public synchronized void stationClosing() {
        isStationOpen = false;
        addEntry("Cl", "", "", "", "", "");
    }
    
    private synchronized void printStateToFile(String[] entry) {
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

    public void saveCloseFile() {
        writer.flush();
        writer.close();
    }
    
    public synchronized void clear() {
        logEntries.clear();
    }
    
    public String getVoteCounts() {
        return "Candidate A: " + scoreA + ", Candidate B: " + scoreB;
    }
    
    public int getVotersProcessed() {
        return votersProcessed;
    }

    public boolean isStationOpen() {
        return isStationOpen;
    }
    
    public String getCurrentVoterInBooth() {
        return currentVoterInBooth;
    }
    
    public int getCurrentQueueSize() {
        return currentQueueSize;
    }
}