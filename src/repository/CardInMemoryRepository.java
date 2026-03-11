package repository;

import domain.Card;
import java.util.HashMap;
import java.util.Map;

public class CardInMemoryRepository implements Repository<Long, Card> {

    private Map<Long, Card> carduri = new HashMap<>();

    @Override
    public Card findOne(Long id) {
        return carduri.get(id);
    }

    @Override
    public Iterable<Card> findAll() {
        return carduri.values();
    }

    @Override
    public Card save(Card entity) {
        if (entity == null) throw new IllegalArgumentException("Cardul nu poate fi null");
        carduri.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Card delete(Long id) {
        return carduri.remove(id);
    }
}