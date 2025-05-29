package commInfra;

import java.io.Serializable;

// import commInfra.MessageType;
/**
 * Message class for communication between components in the Election Day simulation.
 * This class implements Serializable to allow objects to be transmitted over TCP sockets.
 */

public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private MessageType type;        // Type of message
    private int id;                  // ID of the entity (voter or clerk)
    private boolean votingOption;    // The voting option chosen (if applicable)
    private boolean voted;           // In the exit poll if the voter did not vote it automatically leaves (false)
    private int closeIn;             // Number of voters until the exit poll closes
    private int queueSize;           // Size of the queue (if applicable)
    private boolean isStationOpen;   // Whether the station is open or closed
    private boolean isValidated;      // Whether the voter is validated or not

    private Object content;          // Additional content or data payload
    private String message;         // Message content (if needed)
    
    /**
     * Constructor for creating a simple message with just a type
     * For use with types: VOTING_CLOSED, END_OF_DAY, etc.
     * @param type The message type
     */
    public Message(MessageType type) {
        this.type = type;
    }
    
    /**
     * Constructor for creating a message with type and entity ID
     * For use with types: VOTER_ENTERS, VOTER_LEAVES, REQUEST_TO_VOTE, VOTING_COMPLETED, etc.
     * @param type The message type
     * @param id The entity ID (voter or clerk)
     */
    public Message(MessageType type, int id) {
        this.type = type;
        this.id = id;    
    }

     /**
     * Constructor for creating a poll closing notification message
     * For use with type: POLL_CLOSING
     * @param type The message type (should be POLL_CLOSING)
     * @param message The message content
     */
    public Message(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

     /**
     * Constructor for creating a poll closing notification message
     * For use with type: POLL_CLOSING
     * @param type The message type (should be POLL_CLOSING)
     * @param message The message content
     */
    public Message(MessageType type, Boolean votingOption) {
        this.type = type;
        this.votingOption = votingOption;
    }
    
    /**
     * Constructor for creating an exit poll response message
     * For use with type: EXIT_POLL_ANSWER
     * @param type The message type (should be EXIT_POLL_ANSWER)
     * @param id The voter's ID
     * @param votingOption The voter's choice in the exit poll
     * @param voted Whether the voter actually voted or not
     */
    public Message(MessageType type, int id, boolean votingOption, boolean voted) {
        this.type = type;
        this.id = id;
        this.votingOption = votingOption;
        this.voted = voted;
    }

    /**
     * Constructor for creating a poll closing notification message
     * For use with type: POLL_CLOSING
     * @param type The message type (should be POLL_CLOSING)
     * @param id The entity ID
     * @param closeIn Number of voters until the exit poll closes
     */
    public Message(MessageType type, int id, int closeIn) {
        this.type = type;
        this.id = id;
        this.closeIn = closeIn;
    }

    /**
     * Constructor for creating a poll closing notification message
     * For use with type: POLL_CLOSING
     * @param type The message type (should be POLL_CLOSING)
     * @param id The entity ID
     * @param votingOption The voter's choice in the exit poll
     */
    public Message(MessageType type, int id, boolean votingOption) {
        this.type = type;
        this.id = id;
        this.votingOption = votingOption;
    }

   
    
    // Getters and setters
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int Id) {
        this.id = Id;
    }
    
    public boolean getVotingOption() {
        return votingOption;
    }
    
    public void setVotingOption(boolean votingOption) {
        this.votingOption = votingOption;
    }
    
    public boolean didHeVote() {
        return voted;
    }
    
    public void setVoted(boolean truthful) {
        this.voted = truthful;
    }

    public int getCloseIn() {
        return closeIn;
    }

    public void setCloseIn(int closeIn) {
        this.closeIn = closeIn;
    }
    
    public Object getContent() {
        return content;
    }

    public void setStringVal(String content) {
        this.content = message;
    }

    public String getStringVal() {
        return message;
    }

    
    public void setContent(Object content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "Message [type=" + type + ", Id=" + id + ", votingOption=" + votingOption + "]";
    }

    public boolean getIsStationOpen() {
        return isStationOpen;
    }

    public int getIntVal() {
        return 0;
    }

    public int getQueueSize() {
        
        return queueSize;
    }

    public boolean getIsValidated() {
        return isValidated;
    }

    public void setIsValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }
}
