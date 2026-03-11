package domain;

import java.util.ArrayList;
import java.util.List;

public abstract class Event {
    protected Long id;
    protected String nume;
    protected List<User> subscribers;

    public Event(String nume) {
        this.nume = nume;
        subscribers = new ArrayList<>();
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public void subscribe(User user) {
        if(!subscribers.contains(user))
            subscribers.add(user);
    }

    public void unsubscribe(User user) {
        subscribers.remove(user);
    }

    public void notifySubscribers(String mesaj) {
        for(User user : subscribers)
            user.receiveMessage("Sistem", mesaj);
    }

    public String getNume() {
        return nume;
    }

    public List<User> getSubscribers() {
        return subscribers;
    }
}
