package repository;

import domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDbRepository implements Repository<Long, Event> {
    private String url;
    private String user;
    private String password;
    private Repository<Long, Persoana> persoanaRepo;
    private Repository<Long, Duck> duckRepo;

    public EventDbRepository(String url, String user, String password,
                             Repository<Long, Persoana> persoanaRepo,
                             Repository<Long, Duck> duckRepo) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.persoanaRepo = persoanaRepo;
        this.duckRepo = duckRepo;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Event extractEvent(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String nume = rs.getString("nume");
        String tipEveniment = rs.getString("tip_eveniment");

        Event event;

        if ("RaceEvent".equals(tipEveniment)) {
            List<Double> balize = getRaceEventBalize(id);
            event = new RaceEvent(nume, balize);
        } else {
            throw new IllegalStateException("Tip de eveniment necunoscut: " + tipEveniment);
        }

        event.setId(id);

        List<User> subscribers = getSubscribersForEvent(id);
        for(User u : subscribers) {
            event.subscribe(u);
        }

        return event;
    }

    private List<Double> getRaceEventBalize(Long eventId) throws SQLException {
        List<Double> balize = new ArrayList<>();
        String sql = "SELECT distanta FROM race_event_details WHERE event_id = ? ORDER BY index_baliza";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                balize.add(rs.getDouble("distanta"));
            }
        }
        return balize;
    }

    private List<User> getSubscribersForEvent(Long eventId) throws SQLException {
        List<User> subscribers = new ArrayList<>();
        String sql = "SELECT user_id, user_type FROM event_subscriptions WHERE event_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                String userType = rs.getString("user_type");

                User user = null;
                if ("PERSOANA".equals(userType)) {
                    user = persoanaRepo.findOne(userId);
                } else if ("RATA".equals(userType)) {
                    user = duckRepo.findOne(userId);
                }

                if (user != null) {
                    subscribers.add(user);
                }
            }
        }
        return subscribers;
    }

    private void insertRaceEventDetails(Connection connection, Long eventId, List<Double> balize) throws SQLException {
        String sql = "INSERT INTO race_event_details (event_id, index_baliza, distanta) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < balize.size(); i++) {
                ps.setLong(1, eventId);
                ps.setInt(2, i + 1);
                ps.setDouble(3, balize.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Event save(Event entity) {
        if (entity == null) throw new IllegalArgumentException("Evenimentul nu poate fi null");

        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            boolean isNew = entity.getId() == null;

            if (isNew) {
                String sqlEvent = "INSERT INTO events (nume, tip_eveniment) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sqlEvent, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, entity.getNume());
                    ps.setString(2, entity.getClass().getSimpleName());
                    ps.executeUpdate();

                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Eroare la obținerea ID-ului Eventului nou.");
                    }
                }

                if (entity instanceof RaceEvent re) {
                    if (re.getBalize() != null && !re.getBalize().isEmpty()) {
                        insertRaceEventDetails(connection, entity.getId(), re.getBalize());
                    }
                }
            } else {
                String sqlUpdate = "UPDATE events SET nume = ? WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                    ps.setString(1, entity.getNume());
                    ps.setLong(2, entity.getId());
                    ps.executeUpdate();
                }
            }

            String sqlDeleteSubs = "DELETE FROM event_subscriptions WHERE event_id = ?";
            try (PreparedStatement psDeleteSubs = connection.prepareStatement(sqlDeleteSubs)) {
                psDeleteSubs.setLong(1, entity.getId());
                psDeleteSubs.executeUpdate();
            }

            String sqlInsertSubs = "INSERT INTO event_subscriptions (event_id, user_id, user_type) VALUES (?, ?, ?)";
            try (PreparedStatement psInsertSubs = connection.prepareStatement(sqlInsertSubs)) {
                for (User u : entity.getSubscribers()) {
                    if(u.getId() == null) continue;

                    psInsertSubs.setLong(1, entity.getId());
                    psInsertSubs.setLong(2, u.getId());

                    String userType = (u instanceof Persoana) ? "PERSOANA" : "RATA";
                    psInsertSubs.setString(3, userType);

                    psInsertSubs.addBatch();
                }
                psInsertSubs.executeBatch();
            }

            connection.commit();
            return entity;

        } catch (SQLException e) {
            System.err.println("EROARE SALVARE EVENIMENT IN BAZA DE DATE:");
            e.printStackTrace();

            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    @Override
    public Event findOne(Long id) {
        String sql = "SELECT * FROM events WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractEvent(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Event> findAll() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    events.add(extractEvent(rs));
                } catch (IllegalStateException ignored) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public Event delete(Long id) {
        Event eventDeSters = findOne(id);
        if (eventDeSters == null) {
            return null;
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            String sqlDeleteEvent = "DELETE FROM events WHERE id = ?";
            try (PreparedStatement psEvent = connection.prepareStatement(sqlDeleteEvent)) {
                psEvent.setLong(1, id);
                psEvent.executeUpdate();
            }

            connection.commit();
            return eventDeSters;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}