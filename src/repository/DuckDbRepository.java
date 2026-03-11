package repository;

import domain.*;
import repository.Page;
import repository.Pageable;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class DuckDbRepository implements Repository<Long, Duck> {
    private String url;
    private String user;
    private String password;

    public DuckDbRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    @Override
    public Duck save(Duck entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Rata nu poate fi null");
        }

        String sql = "INSERT INTO ducks (username, email, password, viteza, rezistenta, tip_rata, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPassword());
            ps.setDouble(4, entity.getViteza());
            ps.setDouble(5, entity.getRezistenta());
            ps.setString(6, entity.getTip().name());
            ps.setString(7, entity.getImagePath());

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                entity.setId(generatedKeys.getLong(1));
            }
            return entity;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Duck findOne(Long id) {
        String sql = "SELECT * FROM ducks WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractDuck(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Duck> findAll() {
        List<Duck> ducks = new ArrayList<>();
        String sql = "SELECT * FROM ducks";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ducks.add(extractDuck(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ducks;
    }

    @Override
    public Duck delete(Long id) {
        Duck duckDeSters = findOne(id);
        if (duckDeSters == null) {
            return null; // Nu există
        }

        String sql = "DELETE FROM ducks WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                return duckDeSters;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Duck extractDuck(ResultSet rs) throws SQLException {

        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String parola = rs.getString("password");
        double viteza = rs.getDouble("viteza");
        double rezistenta = rs.getDouble("rezistenta");
        String tipRataText = rs.getString("tip_rata");
        Duck.TipRata tip = Duck.TipRata.valueOf(tipRataText);
        Duck d = null;
        switch (tip) {
            case Flying: d = new FlyingDuck(id, username, email, parola, viteza, rezistenta); break;
            case Swimming: d = new SwimmingDuck(id, username, email, parola, viteza, rezistenta); break;
            case Flying_and_Swimming: d = new FlyingAndSwimmingDuck(id, username, email, parola, viteza, rezistenta); break;
        }
        if (d != null) d.setImagePath(rs.getString("image_path"));
        return d;
    }

    public Page<Duck> findAllOnPage(Pageable pageable, String tipRataFilter) {
        List<Duck> ducks = new ArrayList<>();

        try (Connection connection = getConnection()) {
            int offset = pageable.getPageNumber() * pageable.getPageSize();
            int limit = pageable.getPageSize();

            String sqlWhere = "";
            if (tipRataFilter != null && !tipRataFilter.equals("TOATE")) {
                sqlWhere = " WHERE tip_rata = '" + tipRataFilter + "'";
            }

            String sql = "SELECT * FROM ducks" + sqlWhere + " LIMIT ? OFFSET ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, limit);
                ps.setInt(2, offset);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ducks.add(extractDuck(rs));
                    }
                }
            }

            String countSql = "SELECT COUNT(*) AS count FROM ducks" + sqlWhere;
            int total = 0;
            try (PreparedStatement psCount = connection.prepareStatement(countSql);
                 ResultSet rsCount = psCount.executeQuery()) {
                if (rsCount.next()) {
                    total = rsCount.getInt("count");
                }
            }

            return new Page<>(ducks, total);

        } catch (SQLException e) {
            e.printStackTrace();
            return new Page<>(new ArrayList<>(), 0);
        }
    }

    public Duck update(Duck entity) {
        String sql = "UPDATE ducks SET image_path = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getImagePath());
            ps.setLong(2, entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }
}
