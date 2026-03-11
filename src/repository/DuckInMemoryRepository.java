package repository;

import domain.Duck;
import java.util.HashMap;
import java.util.Map;

public class DuckInMemoryRepository implements Repository<Long, Duck> {

    private Map<Long, Duck> ducks = new HashMap<>();

    @Override
    public Duck findOne(Long id) {
        return ducks.get(id);
    }

    @Override
    public Iterable<Duck> findAll() {
        return ducks.values();
    }

    @Override
    public Duck save(Duck entity) {
        if (entity == null) throw new IllegalArgumentException("Rata nu poate fi null");
        ducks.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Duck delete(Long id) {
        return ducks.remove(id);
    }
}