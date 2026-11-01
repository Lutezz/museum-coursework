package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Logger;

public class ImportExhibitsTask extends Task<Integer> {
    private static final Logger LOG = Logger.getLogger(ImportExhibitsTask.class.getName());
    private final Path file;
    private final HttpMuseumClient api;

    public ImportExhibitsTask(Path file, HttpMuseumClient api) { this.file = file; this.api = api; }

    @Override protected Integer call() throws Exception {
        List<String> lines = Files.readAllLines(file);
        int total = Math.max(1, lines.size() - 1), imported = 0, skipped = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(";");
            try {
                if (p.length != 5) throw new IllegalArgumentException("Невірна кількість полів");
                Exhibit e = new Exhibit(null, p[0].trim(), p[1].trim(), p[2].trim(),
                        Double.parseDouble(p[3].trim()), Double.parseDouble(p[4].trim()));
                api.addExhibit(e);
                imported++;
            } catch (Exception ex) {
                skipped++;
                LOG.warning("Пропущено рядок " + (i + 1) + ": " + ex.getMessage());
            }
            updateProgress(i, total);
            updateMessage("Імпортовано: " + imported + ", пропущено: " + skipped + " (оброблено " + i + " із " + total + ")");
        }
        return imported;
    }
}
