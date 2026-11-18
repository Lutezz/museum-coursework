package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Спільна основа всіх Servlet: формування JSON-відповіді, JSON-помилки
 * та технічне перетворення HTTP-параметрів (String → long / double).
 * Предметних правил тут немає — вони залишаються в Service.
 */
public abstract class JsonServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    protected final ObjectMapper mapper = new ObjectMapper();

    protected void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    // IllegalArgumentException — помилка у вхідних даних або предметному правилі → 400.
    // Будь-який інший виняток — непередбачена помилка сервера → 500 без технічних деталей.
    protected void writeError(HttpServletResponse resp, Exception e) throws IOException {
        boolean clientError = e instanceof IllegalArgumentException;
        if (!clientError) {
            LOG.log(Level.SEVERE, "Помилка обробки запиту в " + getClass().getSimpleName(), e);
        }
        String message = clientError
            ? (e.getMessage() == null ? "Некоректний запит" : e.getMessage())
            : "Внутрішня помилка сервера";
        resp.setStatus(clientError
            ? HttpServletResponse.SC_BAD_REQUEST
            : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writeJson(resp, Map.of("error", message));
    }

    protected String required(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Не задано поле «" + name + "»");
        }
        return value.trim();
    }

    protected long longParam(HttpServletRequest req, String name) {
        String value = required(req, name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути цілим числом");
        }
    }

    protected double doubleParam(HttpServletRequest req, String name) {
        String value = required(req, name).replace(',', '.');
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути числом");
        }
    }

    protected long id(HttpServletRequest req) {
        return longParam(req, "id");
    }
}
