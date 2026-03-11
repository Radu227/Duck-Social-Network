package repository;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDbRepository implements Repository<Long, FriendRequest> {
    private String url;
    private String user;
    private String password;
    private Repository<Long, Persoana> persoanaRepo;
    private Repository<Long, Duck> duckRepo;

    public FriendRequestDbRepository(String url, String user, String password,
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

    private User findUser(Long id, String type) {
        if ("PERSOANA".equals(type)) return persoanaRepo.findOne(id);
        if ("RATA".equals(type)) return duckRepo.findOne(id);
        return null;
    }

    @Override
    public FriendRequest save(FriendRequest entity) {
        String sql = "INSERT INTO friend_requests (from_id, from_type, to_id, to_type, status, date_sent) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, entity.getFrom().getId());
            ps.setString(2, (entity.getFrom() instanceof Persoana) ? "PERSOANA" : "RATA");
            ps.setLong(3, entity.getTo().getId());
            ps.setString(4, (entity.getTo() instanceof Persoana) ? "PERSOANA" : "RATA");
            ps.setString(5, entity.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(entity.getDate()));

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) entity.setId(rs.getLong(1));
            return entity;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metoda necesara pentru accept (schimbam statusul in approved)
    public void update(FriendRequest entity) {
        String sql = "UPDATE friend_requests SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getStatus());
            ps.setLong(2, entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FriendRequest delete(Long id) {
        String sql = "DELETE FROM friend_requests WHERE id = ?";
        try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch(SQLException e){ e.printStackTrace(); }
        return null;
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        List<FriendRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                User u1 = findUser(rs.getLong("from_id"), rs.getString("from_type"));
                User u2 = findUser(rs.getLong("to_id"), rs.getString("to_type"));

                if (u1 != null && u2 != null) {
                    list.add(new FriendRequest(id, u1, u2, rs.getString("status"), rs.getTimestamp("date_sent").toLocalDateTime()));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override public FriendRequest findOne(Long id) { return null; }
}