package commInfra;

import java.io.Serializable;
/**
 * Message class for communication between components in the Election Day simulation.
 * This class implements Serializable to allow objects to be transmitted over TCP sockets.
 */

public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private MessageType type;        // Type of message
    private int id;                  // ID of the entity (voter or clerk)
    private int votingOption;        // The voting option chosen (if applicable)
    private boolean myVote;         // The voter's choice in the exit poll (if applicable)
    private boolean truthful;        // Whether exit poll response is truthful (if applicable)
    private Object content;          // Additional content or data payload
    
    /**
     * Constructor for creating a simple message with just a type
     * @param type The message type
     */
    public Message(MessageType type) {
        this.type = type;
    }
    
    /**
     * Constructor for creating a message with type and voter ID
     * @param type The message type
     * @param voterId The voter's ID
     */
    public Message(MessageType type, int id) {
        this.type = type;
        this.id = id;
    }
    
    /**
     * Constructor for creating a full message
     * @param type The message type
     * @param voterId The voter's ID
     * @param votingOption The voting option (candidate choice)
     * @param content Additional content or data
     */
    public Message(MessageType type, int Id, int votingOption, Object content) {
        this.type = type;
        this.id = Id;
        this.votingOption = votingOption;
        this.content = content;
    }
    
    /**
     * Constructor for creating an exit poll response message
     * @param type The message type
     * @param voterId The voter's ID
     * @param myVote The voter's choice in the exit poll
     * @param truthful Whether the response is truthful
     */
    public Message(MessageType type, int Id, boolean myVote, boolean truthful) {
        this.type = type;
        this.id = Id;
        this.myVote = myVote;
        this.truthful = truthful;
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
    
    public int getVotingOption() {
        return votingOption;
    }
    
    public void setVotingOption(int votingOption) {
        this.votingOption = votingOption;
    }
    
    public boolean isTruthful() {
        return truthful;
    }
    
    public void setTruthful(boolean truthful) {
        this.truthful = truthful;
    }
    
    public Object getContent() {
        return content;
    }
    
    public void setContent(Object content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "Message [type=" + type + ", Id=" + id + ", votingOption=" + votingOption + "]";
    }
}
