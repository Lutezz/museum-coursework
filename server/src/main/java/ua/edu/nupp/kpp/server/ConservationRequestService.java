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
        ConservationRequest request = build(null, inspectionId, specialistId, RequestState.NEW);
        dao.add(request);
        LOG.info("Створено заявку на консервацію за оглядом №" + inspectionId
                + ", I = " + request.getIntegralIndex());
    }

    public void update(long id, long inspectionId, long specialistId) throws SQLException {
        ConservationRequest old = requireExists(id);
        ConservationRequest request = build(id, inspectionId, specialistId, old.getState());
        dao.update(request);
        LOG.info("Оновлено заявку на консервацію №" + id + ", I = " + request.getIntegralIndex());
    }

    public void changeState(long id, RequestState next) throws SQLException {
        ConservationRequest current = requireExists(id);
        if (!isAllowedTransition(current.getState(), next)) {
            throw reject("Недопустимий перехід стану заявки №" + id + ": " + current.getState() + " → " + next);
        }
        dao.updateState(id, next);
        LOG.info("Заявку на консервацію №" + id + " переведено " + current.getState() + " → " + next);
    }

    public void delete(long id) throws SQLException {
        requireExists(id);
        dao.delete(id);
        LOG.info("Видалено заявку на консервацію №" + id);
    }

    public static boolean isAllowedTransition(RequestState from, RequestState to) {
        return (from == RequestState.NEW && to == RequestState.ASSESSED)
            || (from == RequestState.ASSESSED && to == RequestState.WORKS)
            || (from == RequestState.WORKS && to == RequestState.COMPLETED);
    }

    private ConservationRequest requireExists(long id) throws SQLException {
        ConservationRequest request = dao.getById(id);
        if (request == null) throw reject("Заявку на консервацію №" + id + " не знайдено");
        return request;
    }

    // F1 і F3 беруться з експоната, F2 — з огляду; I розраховується сервером, а не вводиться вручну.
    private ConservationRequest build(Long id, long inspectionId, long specialistId, RequestState state) throws SQLException {
        Inspection inspection = inspectionDAO.getById(inspectionId);
        if (inspection == null) throw reject("Огляд №" + inspectionId + " не знайдено");
        if (specialistDAO.getById(specialistId) == null) throw reject("Фахівця №" + specialistId + " не знайдено");
        Exhibit exhibit = exhibitDAO.getById(inspection.getExhibitId());
        if (exhibit == null) throw reject("Експонат №" + inspection.getExhibitId() + " не знайдено");
        double f1 = exhibit.getMaterialSensitivity();
        double f2 = inspection.getDamageSeverity();
        double f3 = exhibit.getExposureSignificance();
        return new ConservationRequest(id, inspectionId, specialistId, state, f1, f2, f3,
                IndexCalculator.calculate(f1, f2, f3));
    }

    // Відхилена предметна операція: фіксуємо попередження і повертаємо виняток для викликача.
    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
