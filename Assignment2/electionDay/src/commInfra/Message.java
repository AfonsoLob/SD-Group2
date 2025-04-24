package commInfra;

import java.io.Serializable;
/**
 * Message class for communication between components in the Election Day simulation.
 * This class implements Serializable to allow objects to be transmitted over TCP sockets.
 */

public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private MessageType type;        // Type of message
    private int voterId;             // ID of the voter (if applicable)
    private int votingOption;        // The voting option chosen (if applicable)
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
    public Message(MessageType type, int voterId) {
        this.type = type;
        this.voterId = voterId;
    }
    
    /**
     * Constructor for creating a full message
     * @param type The message type
     * @param voterId The voter's ID
     * @param votingOption The voting option (candidate choice)
     * @param content Additional content or data
     */
    public Message(MessageType type, int voterId, int votingOption, Object content) {
        this.type = type;
        this.voterId = voterId;
        this.votingOption = votingOption;
        this.content = content;
    }
    
    /**
     * Constructor for creating an exit poll response message
     * @param type The message type
     * @param voterId The voter's ID
     * @param votingOption The voting option claimed in poll
     * @param truthful Whether the response is truthful
     */
    public Message(MessageType type, int voterId, int votingOption, boolean truthful) {
        this.type = type;
        this.voterId = voterId;
        this.votingOption = votingOption;
        this.truthful = truthful;
    }
    
    // Getters and setters
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public int getVoterId() {
        return voterId;
    }
    
    public void setVoterId(int voterId) {
        this.voterId = voterId;
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
        return "Message [type=" + type + ", voterId=" + voterId + ", votingOption=" + votingOption + "]";
    }
}
