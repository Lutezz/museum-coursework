package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class InspectionService {
    private static final Logger LOG = Logger.getLogger(InspectionService.class.getName());
    private final InspectionDAO dao = new InspectionDAO();
    private final ExhibitDAO exhibitDAO = new ExhibitDAO();

    public List<Inspection> getAll() throws SQLException { return dao.getAll(); }
    public void add(Inspection i) throws SQLException { validate(i); requireExhibit(i.getExhibitId()); dao.add(i); LOG.info("Додано огляд"); }
    public void update(Inspection i) throws SQLException { validate(i); requireExhibit(i.getExhibitId()); dao.update(i); LOG.info("Оновлено огляд"); }
    public void delete(long id) throws SQLException { dao.delete(id); LOG.info("Видалено огляд"); }

    static void validate(Inspection i) {
        if (i == null || i.getExhibitId() <= 0 || i.getInspectionDate() == null || i.getInspectionDate().isBlank()) {
            throw new IllegalArgumentException("Некоректні основні дані огляду");
        }
        if (i.getDamageSeverity() < 0 || i.getDamageSeverity() > 100) {
            throw new IllegalArgumentException("Тяжкість пошкодження має бути в діапазоні 0..100");
        }
    }

    private void requireExhibit(long id) throws SQLException {
        if (exhibitDAO.getById(id) == null) throw new IllegalArgumentException("Експонат не знайдено");
    }
}
