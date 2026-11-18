package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

public class InspectionService {
    private static final Logger LOG = Logger.getLogger(InspectionService.class.getName());
    private final InspectionDAO dao = new InspectionDAO();
    private final ExhibitDAO exhibitDAO = new ExhibitDAO();

    public List<Inspection> getAll() throws SQLException { return dao.getAll(); }

    public void add(Inspection i) throws SQLException {
        validate(i);
        requireExhibit(i.getExhibitId());
        dao.add(i);
        LOG.info("Додано огляд експоната №" + i.getExhibitId());
    }

    public void update(Inspection i) throws SQLException {
        validate(i);
        requireExists(i.getId());
        requireExhibit(i.getExhibitId());
        dao.update(i);
        LOG.info("Оновлено огляд №" + i.getId());
    }

    public void delete(long id) throws SQLException {
        requireExists(id);
        try {
            dao.delete(id);
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw reject("Неможливо видалити огляд №" + id + ": на його основі створено заявки на консервацію");
        }
        LOG.info("Видалено огляд №" + id);
    }

    static void validate(Inspection i) {
        if (i == null || i.getExhibitId() <= 0) {
            throw reject("Для огляду потрібно вказати коректний ID експоната");
        }
        if (!isValidDate(i.getInspectionDate())) {
            throw reject("Дата огляду має бути у форматі РРРР-ММ-ДД");
        }
        if (i.getDamageSeverity() < 0 || i.getDamageSeverity() > 100) {
            throw reject("Тяжкість пошкодження має бути в діапазоні 0..100");
        }
    }

    private static boolean isValidDate(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void requireExists(Long id) throws SQLException {
        if (id == null || dao.getById(id) == null) throw reject("Огляд №" + id + " не знайдено");
    }

    private void requireExhibit(long id) throws SQLException {
        if (exhibitDAO.getById(id) == null) throw reject("Експонат №" + id + " не знайдено");
    }

    // Відхилена предметна операція: фіксуємо попередження і повертаємо виняток для викликача.
    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
