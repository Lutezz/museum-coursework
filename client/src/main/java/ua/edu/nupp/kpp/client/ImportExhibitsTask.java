package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фоновий імпорт каталогу експонатів із CSV.
 * Правило обробки: рядок з помилкою формату або відхилений сервером (HTTP 4xx) пропускається
 * з WARNING у журналі; помилка файлу, заголовка, недоступність сервера чи HTTP 5xx — перериває імпорт.
 */
public class ImportExhibitsTask extends Task<Integer> {
    private static final Logger LOG = Logger.getLogger(ImportExhibitsTask.class.getName());
    static final String HEADER = "inventory_number;name;material;material_sensitivity;exposure_significance";
    static final String[] COLUMNS = HEADER.split(";");

    private final Path file;
    private final HttpMuseumClient api;

    public ImportExhibitsTask(Path file, HttpMuseumClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected Integer call() throws Exception {
        LOG.info("Початок CSV-імпорту: " + file.getFileName());
        List<String> lines = readLines();
        checkHeader(lines);

        // Номери непорожніх рядків даних (заголовок — рядок з індексом 0 — не рахується).
        List<Integer> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) rows.add(i);
        }
        int total = rows.size();
        if (total == 0) {
            updateProgress(1, 1);
            updateMessage("Немає рядків для імпорту");
            LOG.info("CSV-імпорт завершено: у файлі немає рядків даних");
            return 0;
        }

        int processed = 0, imported = 0, skipped = 0;
        for (int index : rows) {
            if (isCancelled()) break;
            int lineNo = index + 1;
            try {
                api.addExhibit(parse(lines.get(index)));
                imported++;
            } catch (ApiException e) {
                if (!e.isRejected()) {
                    LOG.log(Level.SEVERE, "Імпорт перервано на рядку " + lineNo, e);
                    throw new IOException(e.getMessage() + ". Імпорт перервано на рядку " + lineNo
                            + "; до цього імпортовано записів: " + imported, e);
                }
                skipped++;
                LOG.warning("Рядок " + lineNo + " відхилено сервером: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                skipped++;
                LOG.warning("Рядок " + lineNo + " пропущено: " + e.getMessage());
            }
            processed++;
            updateProgress(processed, total);
            updateMessage("Опрацьовано " + processed + " із " + total
                    + " (імпортовано: " + imported + ", пропущено: " + skipped + ")");
        }
        String result = "Імпортовано: " + imported + ", пропущено: " + skipped + " із " + total;
        updateMessage("Готово. " + result + (skipped > 0 ? ". Причини пропусків — у журналі" : ""));
        LOG.info("CSV-імпорт завершено. " + result);
        return imported;
    }

    private List<String> readLines() throws IOException {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            LOG.warning("CSV-файл не знайдено: " + file.getFileName());
            throw new IOException("CSV-файл не знайдено: " + file.getFileName(), e);
        } catch (MalformedInputException e) {
            LOG.warning("CSV-файл не в кодуванні UTF-8: " + file.getFileName());
            throw new IOException("CSV-файл має бути в кодуванні UTF-8", e);
        }
    }

    // Заголовок перевіряється ДО першого POST, щоб неправильний файл не залишив у БД частину записів.
    static void checkHeader(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV-файл порожній: немає рядка заголовка");
        }
        String header = lines.get(0).replace("﻿", "").trim();
        if (!HEADER.equals(header)) {
            LOG.warning("Неправильний заголовок CSV: " + header);
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: " + HEADER);
        }
    }

    // Лише технічні перевірки CSV; діапазон 0–100 і унікальність номера перевіряє ExhibitService на сервері.
    static Exhibit parse(String line) {
        String[] f = line.split(";", -1);
        if (f.length != COLUMNS.length) {
            throw new IllegalArgumentException("очікується " + COLUMNS.length + " полів, отримано " + f.length);
        }
        for (int i = 0; i < 3; i++) {
            if (f[i].isBlank()) throw new IllegalArgumentException("порожнє поле " + COLUMNS[i]);
        }
        return new Exhibit(null, f[0].trim(), f[1].trim(), f[2].trim(), number(f[3], COLUMNS[3]), number(f[4], COLUMNS[4]));
    }

    private static double number(String value, String column) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("значення «" + value.trim() + "» у полі " + column + " не є числом");
        }
    }
}
