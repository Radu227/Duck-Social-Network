package repository;

import domain.Duck;
import domain.Persoana;
import domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserFriendshipDbRepository {
    private String url;
    private String user;
    private String password;

    private Repository<Long, Persoana> persoanaRepo;
    private Repository<Long, Duck> duckRepo;

    public UserFriendshipDbRepository(String url, String user, String password,
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

    private String getUserType(User u) {
        if (u instanceof Persoana) {
            return "PERSOANA";
        }
        if (u instanceof Duck) {
            return "RATA";
        }
        throw new IllegalArgumentException("Tip de user necunoscut!");
    }

    public User findUserByIdAndType(Long id, String type) {
        if ("PERSOANA".equals(type)) {
            return persoanaRepo.findOne(id);
        } else if ("RATA".equals(type)) {
            return duckRepo.findOne(id);
        }
        return null;
    }

    public List<User> getFriendsOf(Long userId, String userType) {
        List<User> friends = new ArrayList<>();

        String sql =
                "SELECT user1_id AS friend_id, user1_type AS friend_type " +
                        "FROM friendships_universal WHERE user2_id = ? AND user2_type = ? " +
                        "UNION " +
                        "SELECT user2_id AS friend_id, user2_type AS friend_type " +
                        "FROM friendships_universal WHERE user1_id = ? AND user1_type = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, userType);
            ps.setLong(3, userId);
            ps.setString(4, userType);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Long friendId = rs.getLong("friend_id");
                String friendType = rs.getString("friend_type");

                User friend = findUserByIdAndType(friendId, friendType);
                if (friend != null) friends.add(friend);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public void addFriendship(User u1, User u2) {
        Long id1 = u1.getId();
        String type1 = getUserType(u1);
        Long id2 = u2.getId();
        String type2 = getUserType(u2);

        User userA, userB;
        String typeA, typeB;

        if (id1 < id2 || (id1.equals(id2) && type1.compareTo(type2) < 0)) {
            userA = u1;
            typeA = type1;
            userB = u2;
            typeB = type2;
        } else {
            userA = u2;
            typeA = type2;
            userB = u1;
            typeB = type1;
        }

        String sql = "INSERT INTO friendships_universal (user1_id, user1_type, user2_id, user2_type) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (user1_id, user1_type, user2_id, user2_type) DO NOTHING";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userA.getId());
            ps.setString(2, typeA);
            ps.setLong(3, userB.getId());
            ps.setString(4, typeB);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFriendship(User u1, User u2) {
        Long id1 = u1.getId();
        String type1 = getUserType(u1);
        Long id2 = u2.getId();
        String type2 = getUserType(u2);

        User userA, userB;
        String typeA, typeB;

        if (id1 < id2 || (id1.equals(id2) && type1.compareTo(type2) < 0)) {
            userA = u1;
            typeA = type1;
            userB = u2;
            typeB = type2;
        } else {
            userA = u2;
            typeA = type2;
            userB = u1;
            typeB = type1;
        }

        String sql = "DELETE FROM friendships_universal WHERE user1_id = ? AND user1_type = ? AND user2_id = ? AND user2_type = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userA.getId());
            ps.setString(2, typeA);
            ps.setLong(3, userB.getId());
            ps.setString(4, typeB);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Object[]> findAll() {
        List<Object[]> friendships = new ArrayList<>();

        String sql = "SELECT user1_id, user1_type, user2_id, user2_type FROM friendships_universal";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id1 = rs.getLong("user1_id");
                String type1 = rs.getString("user1_type");
                Long id2 = rs.getLong("user2_id");
                String type2 = rs.getString("user2_type");

                friendships.add(new Object[]{id1, type1, id2, type2});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendships;
    }
}