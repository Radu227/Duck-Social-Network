package repository;

import domain.Event;
import java.util.HashMap;
import java.util.Map;

public class EventInMemoryRepository implements Repository<Long, Event> {

    private Map<Long, Event> evenimente = new HashMap<>();
    private long currentId = 1;

    @Override
    public Event findOne(Long id) {
        return evenimente.get(id);
    }

    @Override
    public Iterable<Event> findAll() {
        return evenimente.values();
    }

    @Override
    public Event save(Event entity) {
        if (entity == null) throw new IllegalArgumentException("Evenimentul nu poate fi null");
        long id = currentId++;
        evenimente.put(id, entity);
        return entity;
    }

    @Override
    public Event delete(Long id) {
        return evenimente.remove(id);
    }
}