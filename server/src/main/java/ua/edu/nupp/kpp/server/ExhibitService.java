package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ExhibitService {
    private static final Logger LOG = Logger.getLogger(ExhibitService.class.getName());
    private final ExhibitDAO dao = new ExhibitDAO();

    public List<Exhibit> getAll() throws SQLException { return dao.getAll(); }
    public void add(Exhibit e) throws SQLException { validate(e); dao.add(e); LOG.info("Додано експонат"); }
    public void update(Exhibit e) throws SQLException { validate(e); dao.update(e); LOG.info("Оновлено експонат"); }
    public void delete(long id) throws SQLException { dao.delete(id); LOG.info("Видалено експонат"); }

    static void validate(Exhibit e) {
        if (e == null || blank(e.getInventoryNumber()) || blank(e.getName()) || blank(e.getMaterial())) {
            throw new IllegalArgumentException("Інвентарний номер, назва та матеріал експоната є обов'язковими");
        }
        if (e.getMaterialSensitivity() < 0 || e.getMaterialSensitivity() > 100
                || e.getExposureSignificance() < 0 || e.getExposureSignificance() > 100) {
            throw new IllegalArgumentException("Чутливість матеріалу та значущість експонування мають бути в діапазоні 0..100");
        }
    }

    private static boolean blank(String v) { return v == null || v.isBlank(); }
}
