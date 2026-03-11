package domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Message {
    private Long id;
    private User from;           // Expeditorul
    private List<User> to;       // Lista de destinatari
    private String message;
    private LocalDateTime date;

    public Message(Long id, User from, List<User> to, String message, LocalDateTime date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getFrom() { return from; }
    public void setFrom(User from) { this.from = from; }

    public List<User> getTo() { return to; }
    public void setTo(List<User> to) { this.to = to; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", from=" + from.getUsername() +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message1 = (Message) o;
        return Objects.equals(id, message1.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}