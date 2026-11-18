package ua.edu.nupp.kpp.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConservationRequestDAO {
    public void add(ConservationRequest x) throws SQLException {
        String sql = "INSERT INTO conservation_request(inspection_id,specialist_id,state,f1,f2,f3,integral_index) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.executeUpdate();
        }
    }

    public List<ConservationRequest> getAll() throws SQLException {
        List<ConservationRequest> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM conservation_request ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    public ConservationRequest getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM conservation_request WHERE id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(ConservationRequest x) throws SQLException {
        String sql = "UPDATE conservation_request SET inspection_id=?,specialist_id=?,state=?,f1=?,f2=?,f3=?,integral_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, x);
            p.setLong(8, x.getId());
            p.executeUpdate();
        }
    }

    public void updateState(long id, RequestState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE conservation_request SET state=? WHERE id=?")) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM conservation_request WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, ConservationRequest x) throws SQLException {
        p.setLong(1, x.getInspectionId());
        p.setLong(2, x.getSpecialistId());
        p.setString(3, x.getState().name());
        p.setDouble(4, x.getF1());
        p.setDouble(5, x.getF2());
        p.setDouble(6, x.getF3());
        p.setDouble(7, x.getIntegralIndex());
    }

    private ConservationRequest map(ResultSet r) throws SQLException {
        return new ConservationRequest(r.getLong("id"), r.getLong("inspection_id"), r.getLong("specialist_id"), RequestState.valueOf(r.getString("state")), r.getDouble("f1"), r.getDouble("f2"), r.getDouble("f3"), r.getDouble("integral_index"));
    }
}
