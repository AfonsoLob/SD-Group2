package Logging;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    // Constants for column headers
    private static final String[] HEADERS = {"Door", "Voter", "Clerk", "Validation", "Booth", "ScoreA", "ScoreB", "Exit"};
    private static final int NUM_COLUMNS = HEADERS.length;
    private PrintWriter writer;
    
    // Store the state of the system
    private List<String[]> logEntries;
    
    // Counters
    private Integer votersProcessed;
    private Integer scoreA;
    private Integer scoreB;

    // Election properties
    private int totalVoters;
    private int totalInStation;
    private int finishAmount;
    
    // Delimiter for the table
    private static final String DELIMITER = "------------------------------------------------";
    
    public Logger(int totalVoters, int totalInStation, int finishAmount) {
        this.logEntries = new ArrayList<>();
        this.votersProcessed = 0;
        this.scoreA = 0;
        this.scoreB = 0;
        this.totalVoters = totalVoters;
        this.totalInStation = totalInStation;
        this.finishAmount = finishAmount;
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
    public synchronized void addEntry(String door, String voter, String clerk, String validation, 
                                     String booth, String exit) {
        String[] entry = new String[NUM_COLUMNS];
        entry[0] = door;
        entry[1] = voter;
        entry[2] = clerk;
        entry[3] = validation;
        entry[4] = booth;
        entry[5] = String.valueOf(scoreA);
        entry[6] = String.valueOf(scoreB);
        entry[7] = exit;
        
        logEntries.add(entry);
    }
    
    public void incrementScoreA() {
        scoreA++;
    }
    
    public void incrementScoreB() {
        scoreB++;
    }
    
    public synchronized void voterAtDoor(int voterId) {
        addEntry("C" + voterId, "", "", "", "", "");
    }
    
    public synchronized void voterEnteringQueue(int voterId) {
        addEntry("", String.valueOf(voterId), "", "", "", "");
    }
    
    public synchronized void clerkProcessingVoter(int voterId) {
        addEntry("", "", String.valueOf(voterId), "", "", "");
    }
    
    public synchronized void validatingVoter(int voterId, boolean valid) {
        String validationStr = String.valueOf(voterId) + (valid ? "+" : "-");
        addEntry("", "", "", validationStr, "", "");
    }
    
    public synchronized void voterInBooth(int voterId, boolean voteA) {
        String boothStr = String.valueOf(voterId) + (voteA ? "A" : "B");
        addEntry("", "", "", "", boothStr, "");
        
        // Update scores
        if (voteA) {
            incrementScoreA();
        } else {
            incrementScoreB();
        }
    }
    
    public synchronized void voterExiting(int voterId, boolean exitPoll) {
        String exitStr = exitPoll ? "P" + voterId : String.valueOf(voterId);
        addEntry("", "", "", "", "", exitStr);
        votersProcessed++;
    }
    
    public synchronized void stationOpening() {
        addEntry("Op", "", "", "", "", "");
    }
    
    public synchronized void stationClosing() {
        addEntry("Cl", "", "", "", "", "");
    }
    
    public synchronized void printState() {
        // Print the header information
        System.out.println("Total of Voters:" + totalVoters + 
                          ", Total Voters in Stations:" + totalInStation + 
                          ", Votes to end: " + finishAmount + " voters");
        
        // Print column headers
        StringBuilder headerBuilder = new StringBuilder("|");
        for (String header : HEADERS) {
            headerBuilder.append(String.format(" %-10s |", header));
        }
        System.out.println(headerBuilder.toString());
        
        // Print delimiter
        System.out.println(DELIMITER);
        
        // Print each log entry
        for (String[] entry : logEntries) {
            StringBuilder rowBuilder = new StringBuilder("|");
            for (String field : entry) {
                rowBuilder.append(String.format(" %-10s |", field != null ? field : ""));
            }
            System.out.println(rowBuilder.toString());
        }
        
        System.out.println(); // Add a blank line for readability
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
}