package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

public class SpecialistService {
    private static final Logger LOG = Logger.getLogger(SpecialistService.class.getName());
    private final SpecialistDAO dao = new SpecialistDAO();

    public List<Specialist> getAll() throws SQLException { return dao.getAll(); }

    public void add(Specialist s) throws SQLException {
        validate(s);
        dao.add(s);
        LOG.info("Додано фахівця " + s.getFullName());
    }

    public void update(Specialist s) throws SQLException {
        validate(s);
        requireExists(s.getId());
        dao.update(s);
        LOG.info("Оновлено фахівця №" + s.getId());
    }

    public void delete(long id) throws SQLException {
        requireExists(id);
        try {
            dao.delete(id);
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw reject("Неможливо видалити фахівця №" + id + ": за ним закріплено заявки на консервацію");
        }
        LOG.info("Видалено фахівця №" + id);
    }

    static void validate(Specialist s) {
        if (s == null || blank(s.getFullName()) || blank(s.getSpecialization())) {
            throw reject("ПІБ і спеціалізація фахівця є обов'язковими");
        }
    }

    private void requireExists(Long id) throws SQLException {
        if (id == null || dao.getById(id) == null) throw reject("Фахівця №" + id + " не знайдено");
    }

    // Відхилена предметна операція: фіксуємо попередження і повертаємо виняток для викликача.
    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }

    private static boolean blank(String v) { return v == null || v.isBlank(); }
}
