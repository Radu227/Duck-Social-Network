package repository;

import domain.Card;
import domain.Duck;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDbRepository implements Repository<Long, Card> {
    private String url;
    private String user;
    private String password;
    private Repository<Long, Duck> duckRepo;

    public CardDbRepository(String url, String user, String password, Repository<Long, Duck> duckRepo) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.duckRepo = duckRepo;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Card extractCard(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String numeCard = rs.getString("nume_card");

        Card<Duck> card = new Card<>(id, numeCard);

        String sqlDucks = "SELECT duck_id FROM card_membri WHERE card_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement psDucks = conn.prepareStatement(sqlDucks)) {

            psDucks.setLong(1, id);
            ResultSet rsDucks = psDucks.executeQuery();

            while (rsDucks.next()) {
                Long duckId = rsDucks.getLong("duck_id");
                Duck duck = duckRepo.findOne(duckId);
                if (duck != null) {
                    ((Card<Duck>) card).adaugaRata(duck);
                }
            }
        }
        return card;
    }

    @Override
    public Card save(Card entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cardul nu poate fi null");
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            boolean isNew = entity.getId() == null || findOne(entity.getId()) == null;

            if (isNew) {
                // 1. Card nou: INSERT și preluarea ID-ului generat de BIGSERIAL
                String sqlCard = "INSERT INTO cards (nume_card) VALUES (?)";
                try (PreparedStatement ps = connection.prepareStatement(sqlCard, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, entity.getNumeCard());
                    ps.executeUpdate();

                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Eroare la obținerea ID-ului Cardului nou.");
                    }
                }
            }
            String sqlDeleteDucks = "DELETE FROM card_membri WHERE card_id = ?";
            try (PreparedStatement psDeleteDucks = connection.prepareStatement(sqlDeleteDucks)) {
                psDeleteDucks.setLong(1, entity.getId());
                psDeleteDucks.executeUpdate();
            }

            String sqlInsertDucks = "INSERT INTO card_membri (card_id, duck_id) VALUES (?, ?)";
            try (PreparedStatement psInsertDucks = connection.prepareStatement(sqlInsertDucks)) {
                List<Duck> members = (List<Duck>) entity.getMembri();
                for (Duck duck : members) {
                    if(duck.getId() == null) {
                        System.err.println("Rata cu username " + duck.getUsername() + " nu are ID și nu poate fi salvată în card.");
                        continue;
                    }
                    psInsertDucks.setLong(1, entity.getId());
                    psInsertDucks.setLong(2, duck.getId());
                    psInsertDucks.addBatch();
                }
                psInsertDucks.executeBatch();
            }

            connection.commit();
            return entity;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Card findOne(Long id) {
        String sql = "SELECT * FROM cards WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractCard(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Card> findAll() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cards.add(extractCard(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    @Override
    public Card delete(Long id) {
        Card cardDeSters = findOne(id);
        if (cardDeSters == null) {
            return null;
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            String sqlDeleteCard = "DELETE FROM cards WHERE id = ?";
            try (PreparedStatement psCard = connection.prepareStatement(sqlDeleteCard)) {
                psCard.setLong(1, id);
                int rowsAffected = psCard.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit();
                    return cardDeSters;
                }
            }
            connection.rollback();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}