package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExhibitDAO {
    public void add(Exhibit x) throws SQLException {
        String sql = "INSERT INTO exhibit(inventory_number,name,material,material_sensitivity,exposure_significance) VALUES(?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.executeUpdate();
        }
    }

    public List<Exhibit> getAll() throws SQLException {
        List<Exhibit> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM exhibit ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    public Exhibit getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM exhibit WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public Exhibit getByInventoryNumber(String inventoryNumber) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM exhibit WHERE inventory_number=?")) {
            p.setString(1, inventoryNumber);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Exhibit x) throws SQLException {
        String sql = "UPDATE exhibit SET inventory_number=?,name=?,material=?,material_sensitivity=?,exposure_significance=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.setLong(6, x.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM exhibit WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Exhibit x) throws SQLException {
        p.setString(1, x.getInventoryNumber());
        p.setString(2, x.getName());
        p.setString(3, x.getMaterial());
        p.setDouble(4, x.getMaterialSensitivity());
        p.setDouble(5, x.getExposureSignificance());
    }

    private Exhibit map(ResultSet r) throws SQLException {
        return new Exhibit(r.getLong("id"), r.getString("inventory_number"), r.getString("name"), r.getString("material"), r.getDouble("material_sensitivity"), r.getDouble("exposure_significance"));
    }
}
