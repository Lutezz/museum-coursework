package ua.edu.nupp.kpp.client;

/**
 * Помилка взаємодії з сервером.
 * status = 0 — сервер недоступний; 4xx — сервер відхилив дані; 5xx — внутрішня помилка сервера.
 */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() { return status; }

    /** Сервер працює, але відхилив саме ці дані (валідація, дублікат, неіснуючий зв'язок). */
    public boolean isRejected() { return status >= 400 && status < 500; }
}
