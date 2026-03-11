package domain;

import java.time.LocalDateTime;
import java.util.List;

public class ReplyMessage extends Message {
    private Message originalMessage; // Mesajul la care se raspunde

    public ReplyMessage(Long id, User from, List<User> to, String message, LocalDateTime date, Message originalMessage) {
        super(id, from, to, message, date);
        this.originalMessage = originalMessage;
    }

    public Message getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(Message originalMessage) {
        this.originalMessage = originalMessage;
    }

    @Override
    public String toString() {
        return super.toString() + " [Reply to ID: " + (originalMessage != null ? originalMessage.getId() : "null") + "]";
    }
}