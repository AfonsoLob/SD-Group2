package commInfra;

import java.io.*;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object payload;

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}