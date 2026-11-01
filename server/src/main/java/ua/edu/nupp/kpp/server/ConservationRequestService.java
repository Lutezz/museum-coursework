package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ConservationRequestService {
    private static final Logger LOG = Logger.getLogger(ConservationRequestService.class.getName());
    private final ConservationRequestDAO dao = new ConservationRequestDAO();
    private final InspectionDAO inspectionDAO = new InspectionDAO();
    private final ExhibitDAO exhibitDAO = new ExhibitDAO();
    private final SpecialistDAO specialistDAO = new SpecialistDAO();

    public List<ConservationRequest> getAll() throws SQLException { return dao.getAll(); }

    public void add(long inspectionId, long specialistId) throws SQLException {
        dao.add(build(null, inspectionId, specialistId, RequestState.NEW));
        LOG.info("Створено заявку на консервацію");
    }

    public void update(long id, long inspectionId, long specialistId) throws SQLException {
        ConservationRequest old = dao.getById(id);
        if (old == null) throw new IllegalArgumentException("Заявку не знайдено");
        dao.update(build(id, inspectionId, specialistId, old.getState()));
        LOG.info("Оновлено заявку на консервацію");
    }

    public void changeState(long id, RequestState next) throws SQLException {
        ConservationRequest current = dao.getById(id);
        if (current == null) throw new IllegalArgumentException("Заявку не знайдено");
        if (!isAllowedTransition(current.getState(), next)) {
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан заявки на консервацію");
    }

    public void delete(long id) throws SQLException { dao.delete(id); LOG.info("Видалено заявку на консервацію"); }

    public static boolean isAllowedTransition(RequestState from, RequestState to) {
        return (from == RequestState.NEW && to == RequestState.ASSESSED)
            || (from == RequestState.ASSESSED && to == RequestState.WORKS)
            || (from == RequestState.WORKS && to == RequestState.COMPLETED);
    }

    private ConservationRequest build(Long id, long inspectionId, long specialistId, RequestState state) throws SQLException {
        Inspection inspection = inspectionDAO.getById(inspectionId);
        if (inspection == null) throw new IllegalArgumentException("Огляд не знайдено");
        if (specialistDAO.getById(specialistId) == null) throw new IllegalArgumentException("Фахівця не знайдено");
        Exhibit exhibit = exhibitDAO.getById(inspection.getExhibitId());
        if (exhibit == null) throw new IllegalArgumentException("Експонат не знайдено");
        double f1 = exhibit.getMaterialSensitivity();
        double f2 = inspection.getDamageSeverity();
        double f3 = exhibit.getExposureSignificance();
        return new ConservationRequest(id, inspectionId, specialistId, state, f1, f2, f3,
                IndexCalculator.calculate(f1, f2, f3));
    }
}
