package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConservationRequestServlet extends JsonServlet {
    private final ConservationRequestService service = new ConservationRequestService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("add".equals(action)) {
                service.add(
                    longParam(req, "inspectionId"),
                    longParam(req, "specialistId")
                );
            } else if ("update".equals(action)) {
                service.update(
                    id(req),
                    longParam(req, "inspectionId"),
                    longParam(req, "specialistId")
                );
            } else if ("transition".equals(action)) {
                // Servlet лише перетворює текст у enum; допустимість переходу перевіряє Service.
                service.changeState(id(req), state(req));
            } else if ("delete".equals(action)) {
                service.delete(id(req));
            } else {
                throw new IllegalArgumentException("Невідома операція");
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private RequestState state(HttpServletRequest req) {
        String value = required(req, "state");
        try {
            return RequestState.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан заявки: " + value);
        }
    }
}
