package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecialistDAO {
    public void add(Specialist x) throws SQLException {
        String sql = "INSERT INTO specialist(full_name,specialization) VALUES(?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.executeUpdate();
        }
    }

    public List<Specialist> getAll() throws SQLException {
        List<Specialist> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM specialist ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    public Specialist getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM specialist WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Specialist x) throws SQLException {
        String sql = "UPDATE specialist SET full_name=?,specialization=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.setLong(3, x.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM specialist WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Specialist x) throws SQLException {
        p.setString(1, x.getFullName());
        p.setString(2, x.getSpecialization());
    }

    private Specialist map(ResultSet r) throws SQLException {
        return new Specialist(r.getLong("id"), r.getString("full_name"), r.getString("specialization"));
    }
}
