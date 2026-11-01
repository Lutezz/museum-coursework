package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InspectionDAO {
    public void add(Inspection x) throws SQLException {
        String sql = "INSERT INTO inspection(exhibit_id,inspection_date,damage_severity) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.executeUpdate();
        }
    }

    public List<Inspection> getAll() throws SQLException {
        List<Inspection> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM inspection ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    public Inspection getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM inspection WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Inspection x) throws SQLException {
        String sql = "UPDATE inspection SET exhibit_id=?,inspection_date=?,damage_severity=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.setLong(4, x.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM inspection WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Inspection x) throws SQLException {
        
p.setLong(1,x.getExhibitId());        p.setString(2,x.getInspectionDate());        p.setDouble(3,x.getDamageSeverity());
    }

    private Inspection map(ResultSet r) throws SQLException {
        return new Inspection(r.getLong("id"), r.getLong("exhibit_id"), r.getString("inspection_date"), r.getDouble("damage_severity"));
    }
}
