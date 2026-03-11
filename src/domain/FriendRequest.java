package domain;

import java.time.LocalDateTime;

public class FriendRequest {
    private Long id;
    private User from;
    private User to;
    private String status;
    private LocalDateTime date;

    public FriendRequest(Long id, User from, User to, String status, LocalDateTime date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.status = status;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getFrom() { return from; }
    public void setFrom(User from) { this.from = from; }

    public User getTo() { return to; }
    public void setTo(User to) { this.to = to; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString() {
        return from.getUsername() + " (" + date.toLocalDate() + ")";
    }
}