package repository;

import domain.Persoana;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PersoanaDbRepository implements Repository<Long, Persoana> {

    private String url;
    private String user;
    private String password;

    public PersoanaDbRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Persoana findOne(Long id) {
        String sql = "SELECT * FROM persoane WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractPersoana(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Persoana> findAll() {
        List<Persoana> persoane = new ArrayList<>();
        String sql = "SELECT * FROM persoane";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                persoane.add(extractPersoana(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return persoane;
    }

    @Override
    public Persoana save(Persoana entity) {
        String sql = "INSERT INTO persoane (username, email, password, nume, prenume, data_nasterii, ocupatie, nivel_empatie, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPassword());
            ps.setString(4, entity.getNume());
            ps.setString(5, entity.getPrenume());
            ps.setDate(6, java.sql.Date.valueOf(entity.getDataNasterii()));
            ps.setString(7, entity.getOcupatie());
            ps.setDouble(8, entity.getNivelEmpatie());
            ps.setString(9, entity.getImagePath());

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
    public Persoana delete(Long id) {
        Persoana persoanaDeSters = findOne(id);
        if (persoanaDeSters == null) {
            return null;
        }

        String sql = "DELETE FROM persoane WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                return persoanaDeSters;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Persoana extractPersoana(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String pass = rs.getString("password");
        String nume = rs.getString("nume");
        String prenume = rs.getString("prenume");
        LocalDate dataNasterii = rs.getDate("data_nasterii").toLocalDate();
        String ocupatie = rs.getString("ocupatie");
        double empatie = rs.getDouble("nivel_empatie");

        Persoana p = new Persoana(id, username, email, pass, nume, prenume, dataNasterii, ocupatie, empatie);
        p.setImagePath(rs.getString("image_path"));
        return p;
    }

    public Persoana update(Persoana entity) {
        String sql = "UPDATE persoane SET image_path = ? WHERE id = ?";
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