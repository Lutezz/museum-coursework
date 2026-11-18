package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

public class ExhibitService {
    private static final Logger LOG = Logger.getLogger(ExhibitService.class.getName());
    private final ExhibitDAO dao = new ExhibitDAO();

    public List<Exhibit> getAll() throws SQLException { return dao.getAll(); }

    public void add(Exhibit e) throws SQLException {
        validate(e);
        requireUniqueInventoryNumber(e.getInventoryNumber(), null);
        dao.add(e);
        LOG.info("Додано експонат " + e.getInventoryNumber());
    }

    public void update(Exhibit e) throws SQLException {
        validate(e);
        requireExists(e.getId());
        requireUniqueInventoryNumber(e.getInventoryNumber(), e.getId());
        dao.update(e);
        LOG.info("Оновлено експонат №" + e.getId());
    }

    public void delete(long id) throws SQLException {
        requireExists(id);
        try {
            dao.delete(id);
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw reject("Неможливо видалити експонат №" + id + ": для нього є огляди");
        }
        LOG.info("Видалено експонат №" + id);
    }

    static void validate(Exhibit e) {
        if (e == null || blank(e.getInventoryNumber()) || blank(e.getName()) || blank(e.getMaterial())) {
            throw reject("Інвентарний номер, назва та матеріал експоната є обов'язковими");
        }
        if (e.getMaterialSensitivity() < 0 || e.getMaterialSensitivity() > 100
                || e.getExposureSignificance() < 0 || e.getExposureSignificance() > 100) {
            throw reject("Чутливість матеріалу та значущість експонування мають бути в діапазоні 0..100");
        }
    }

    private void requireExists(Long id) throws SQLException {
        if (id == null || dao.getById(id) == null) throw reject("Експонат №" + id + " не знайдено");
    }

    private void requireUniqueInventoryNumber(String inventoryNumber, Long ownId) throws SQLException {
        Exhibit existing = dao.getByInventoryNumber(inventoryNumber);
        if (existing != null && !existing.getId().equals(ownId)) {
            throw reject("Експонат з інвентарним номером " + inventoryNumber + " уже існує");
        }
    }

    // Відхилена предметна операція: фіксуємо попередження і повертаємо виняток для викликача.
    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }

    private static boolean blank(String v) { return v == null || v.isBlank(); }
}
