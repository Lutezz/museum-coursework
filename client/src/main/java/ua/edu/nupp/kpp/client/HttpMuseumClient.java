package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class HttpMuseumClient {
    private static final String BASE = "http://localhost:8080/museum-server";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Exhibit> getExhibits() { return Arrays.asList(read(get("/exhibits"), Exhibit[].class)); }
    public void addExhibit(Exhibit e) { post("/exhibits", exhibitForm("add", e)); }
    public void updateExhibit(Exhibit e) { post("/exhibits", exhibitForm("update", e) + "&id=" + e.getId()); }
    public void deleteExhibit(long id) { post("/exhibits", "action=delete&id=" + id); }

    public List<Inspection> getInspections() { return Arrays.asList(read(get("/inspections"), Inspection[].class)); }
    public void addInspection(Inspection i) { post("/inspections", inspectionForm("add", i)); }
    public void updateInspection(Inspection i) { post("/inspections", inspectionForm("update", i) + "&id=" + i.getId()); }
    public void deleteInspection(long id) { post("/inspections", "action=delete&id=" + id); }

    public List<ConservationRequest> getRequests() { return Arrays.asList(read(get("/requests"), ConservationRequest[].class)); }
    public void addRequest(long inspectionId, long specialistId) { post("/requests", "action=add&inspectionId=" + inspectionId + "&specialistId=" + specialistId); }
    public void updateRequest(long id, long inspectionId, long specialistId) { post("/requests", "action=update&id=" + id + "&inspectionId=" + inspectionId + "&specialistId=" + specialistId); }
    public void changeRequestState(long id, RequestState state) { post("/requests", "action=transition&id=" + id + "&state=" + state.name()); }
    public void deleteRequest(long id) { post("/requests", "action=delete&id=" + id); }

    public List<Specialist> getSpecialists() { return Arrays.asList(read(get("/specialists"), Specialist[].class)); }
    public void addSpecialist(Specialist s) { post("/specialists", specialistForm("add", s)); }
    public void updateSpecialist(Specialist s) { post("/specialists", specialistForm("update", s) + "&id=" + s.getId()); }
    public void deleteSpecialist(long id) { post("/specialists", "action=delete&id=" + id); }

    private String exhibitForm(String action, Exhibit e) {
        return "action=" + action + "&inventoryNumber=" + enc(e.getInventoryNumber()) + "&name=" + enc(e.getName())
            + "&material=" + enc(e.getMaterial()) + "&materialSensitivity=" + e.getMaterialSensitivity()
            + "&exposureSignificance=" + e.getExposureSignificance();
    }
    private String inspectionForm(String action, Inspection i) {
        return "action=" + action + "&exhibitId=" + i.getExhibitId() + "&inspectionDate=" + enc(i.getInspectionDate())
            + "&damageSeverity=" + i.getDamageSeverity();
    }
    private String specialistForm(String action, Specialist s) {
        return "action=" + action + "&fullName=" + enc(s.getFullName()) + "&specialization=" + enc(s.getSpecialization());
    }
    private String get(String path) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).GET().build();
        return send(req);
    }
    private String post(String path, String form) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        return send(req);
    }
    private String send(HttpRequest req) {
        try {
            HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() >= 400) throw new RuntimeException(r.body());
            return r.body();
        } catch (Exception e) {
            throw new RuntimeException("Помилка HTTP: " + e.getMessage(), e);
        }
    }
    private <T> T read(String json, Class<T> type) {
        try { return mapper.readValue(json, type); }
        catch (Exception e) { throw new RuntimeException("Помилка JSON", e); }
    }
    private String enc(String s) { return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8); }
}
