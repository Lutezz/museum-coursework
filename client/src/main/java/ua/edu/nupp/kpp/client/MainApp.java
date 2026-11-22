package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public class MainApp extends Application {
    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private final HttpMuseumClient api = new HttpMuseumClient();
    private final TableView<Exhibit> exhibits = new TableView<>();
    private final TableView<Inspection> inspections = new TableView<>();
    private final TableView<ConservationRequest> requests = new TableView<>();
    private final TableView<Specialist> specialists = new TableView<>();

    @Override public void start(Stage stage) {
        TabPane tabs = new TabPane(
            new Tab("Експонати", exhibitPane()),
            new Tab("Огляди", inspectionPane()),
            new Tab("Заявки на консервацію", requestPane()),
            new Tab("Фахівці", specialistPane())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        stage.setScene(new Scene(tabs, 1100, 680));
        stage.setTitle("Контроль стану музейних експонатів");
        stage.show();
        refreshAll();
    }

    private Pane exhibitPane() {
        exhibits.getColumns().setAll(col("ID", "id"), col("Інв. номер", "inventoryNumber"), col("Назва", "name"),
            col("Матеріал", "material"), col("F1 (чутливість)", "materialSensitivity"), col("F3 (значущість)", "exposureSignificance"));
        TextField inv = new TextField(), name = new TextField(), material = new TextField(), f1 = new TextField(), f3 = new TextField();
        ProgressBar progress = new ProgressBar(0);
        Label status = new Label("CSV не запущено");
        Button add = new Button("Додати"), update = new Button("Змінити"), delete = new Button("Видалити"),
               refresh = new Button("Оновити"), importCsv = new Button("Імпортувати CSV");
        add.setOnAction(e -> safe(() -> { api.addExhibit(readExhibit(null, inv, name, material, f1, f3)); refreshExhibits(); }));
        update.setOnAction(e -> safe(() -> { Exhibit x = selected(exhibits); api.updateExhibit(readExhibit(x.getId(), inv, name, material, f1, f3)); refreshExhibits(); }));
        delete.setOnAction(e -> safe(() -> { api.deleteExhibit(selected(exhibits).getId()); refreshAll(); }));
        refresh.setOnAction(e -> refreshExhibits());
        exhibits.getSelectionModel().selectedItemProperty().addListener((o, a, x) -> {
            if (x != null) { inv.setText(x.getInventoryNumber()); name.setText(x.getName()); material.setText(x.getMaterial());
                f1.setText("" + x.getMaterialSensitivity()); f3.setText("" + x.getExposureSignificance()); }
        });
        importCsv.setOnAction(e -> {
            Path csv = chooseCsv(importCsv);
            if (csv != null) startImport(csv, importCsv, progress, status);
        });
        VBox form = new VBox(6, new Label("Інвентарний номер"), inv, new Label("Назва"), name, new Label("Матеріал"), material,
            new Label("Чутливість матеріалу (F1, 0–100)"), f1, new Label("Значущість експонування (F3, 0–100)"), f3,
            new HBox(8, add, update, delete, refresh), importCsv, progress, status);
        return layout(exhibits, form);
    }

    // Шлях до CSV обирає користувач; діалог відкривається у відносному каталозі sample-data.
    private Path chooseCsv(Button owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Виберіть CSV-файл каталогу експонатів");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV-файли (*.csv)", "*.csv"));
        File sampleData = Path.of("sample-data").toFile();
        if (sampleData.isDirectory()) chooser.setInitialDirectory(sampleData.getAbsoluteFile());
        File selected = chooser.showOpenDialog(owner.getScene().getWindow());
        return selected == null ? null : selected.toPath();
    }

    private void startImport(Path csv, Button importCsv, ProgressBar progress, Label status) {
        ImportExhibitsTask task = new ImportExhibitsTask(csv, api);
        progress.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());
        importCsv.setDisable(true); // повторний запуск заблоковано, поки триває імпорт
        task.setOnSucceeded(x -> {
            progress.progressProperty().unbind();
            status.textProperty().unbind();
            status.setText(task.getMessage());
            importCsv.setDisable(false);
            refreshExhibits();
        });
        task.setOnFailed(x -> {
            progress.progressProperty().unbind();
            status.textProperty().unbind();
            progress.setProgress(0);
            Throwable error = task.getException();
            status.setText("Імпорт не виконано: " + (error == null ? "невідома помилка" : error.getMessage()));
            importCsv.setDisable(false);
            refreshExhibits(); // частина рядків могла бути збережена до помилки
            showError(error);
        });
        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    private Pane inspectionPane() {
        inspections.getColumns().setAll(col("ID", "id"), col("Експонат (ID)", "exhibitId"), col("Дата", "inspectionDate"), col("F2 (тяжкість)", "damageSeverity"));
        TextField exhibitId = new TextField(), date = new TextField(), f2 = new TextField();
        Button add = new Button("Додати"), update = new Button("Змінити"), delete = new Button("Видалити"), refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> { api.addInspection(readInspection(null, exhibitId, date, f2)); refreshInspections(); }));
        update.setOnAction(e -> safe(() -> { Inspection i = selected(inspections); api.updateInspection(readInspection(i.getId(), exhibitId, date, f2)); refreshInspections(); }));
        delete.setOnAction(e -> safe(() -> { api.deleteInspection(selected(inspections).getId()); refreshAll(); }));
        refresh.setOnAction(e -> refreshInspections());
        inspections.getSelectionModel().selectedItemProperty().addListener((o, a, i) -> {
            if (i != null) { exhibitId.setText("" + i.getExhibitId()); date.setText(i.getInspectionDate()); f2.setText("" + i.getDamageSeverity()); }
        });
        return layout(inspections, new VBox(8, new Label("ID експоната"), exhibitId, new Label("Дата огляду (РРРР-ММ-ДД)"), date,
            new Label("Тяжкість пошкодження (F2, 0–100)"), f2, new HBox(8, add, update, delete, refresh)));
    }

    private Pane requestPane() {
        requests.getColumns().setAll(col("ID", "id"), col("Огляд (ID)", "inspectionId"), col("Фахівець (ID)", "specialistId"),
            col("Стан", "state"), col("F1", "f1"), col("F2", "f2"), col("F3", "f3"), col("I", "integralIndex"));
        TextField inspectionId = new TextField(), specialistId = new TextField();
        ComboBox<RequestState> state = new ComboBox<>(FXCollections.observableArrayList(RequestState.values()));
        Button add = new Button("Створити"), update = new Button("Перерахувати"), change = new Button("Змінити стан"),
               delete = new Button("Видалити"), refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> { api.addRequest(wholeNumber(inspectionId, "ID огляду"), wholeNumber(specialistId, "ID фахівця")); refreshRequests(); }));
        update.setOnAction(e -> safe(() -> { api.updateRequest(selected(requests).getId(), wholeNumber(inspectionId, "ID огляду"), wholeNumber(specialistId, "ID фахівця")); refreshRequests(); }));
        change.setOnAction(e -> safe(() -> {
            ConservationRequest r = selected(requests);
            if (state.getValue() == null) throw new IllegalArgumentException("Виберіть новий стан заявки");
            api.changeRequestState(r.getId(), state.getValue());
            refreshRequests();
        }));
        delete.setOnAction(e -> safe(() -> { api.deleteRequest(selected(requests).getId()); refreshRequests(); }));
        refresh.setOnAction(e -> refreshRequests());
        requests.getSelectionModel().selectedItemProperty().addListener((o, a, v) -> {
            if (v != null) { inspectionId.setText("" + v.getInspectionId()); specialistId.setText("" + v.getSpecialistId()); }
        });
        return layout(requests, new VBox(8, new Label("ID огляду"), inspectionId, new Label("ID фахівця"), specialistId,
            new Label("Новий стан"), new HBox(8, state, change), new HBox(8, add, update, delete, refresh)));
    }

    private Pane specialistPane() {
        specialists.getColumns().setAll(col("ID", "id"), col("ПІБ", "fullName"), col("Спеціалізація", "specialization"));
        TextField fullName = new TextField(), specialization = new TextField();
        Button add = new Button("Додати"), update = new Button("Змінити"), delete = new Button("Видалити"), refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> { api.addSpecialist(new Specialist(null, text(fullName, "ПІБ"), text(specialization, "Спеціалізація"))); refreshSpecialists(); }));
        update.setOnAction(e -> safe(() -> { Specialist s = selected(specialists); api.updateSpecialist(new Specialist(s.getId(), text(fullName, "ПІБ"), text(specialization, "Спеціалізація"))); refreshSpecialists(); }));
        delete.setOnAction(e -> safe(() -> { api.deleteSpecialist(selected(specialists).getId()); refreshAll(); }));
        refresh.setOnAction(e -> refreshSpecialists());
        specialists.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s != null) { fullName.setText(s.getFullName()); specialization.setText(s.getSpecialization()); }
        });
        return layout(specialists, new VBox(8, new Label("ПІБ"), fullName, new Label("Спеціалізація"), specialization, new HBox(8, add, update, delete, refresh)));
    }

    private Exhibit readExhibit(Long id, TextField inv, TextField name, TextField material, TextField f1, TextField f3) {
        return new Exhibit(id, text(inv, "Інвентарний номер"), text(name, "Назва"), text(material, "Матеріал"),
            number(f1, "Чутливість матеріалу"), number(f3, "Значущість експонування"));
    }
    private Inspection readInspection(Long id, TextField exhibitId, TextField date, TextField f2) {
        return new Inspection(id, wholeNumber(exhibitId, "ID експоната"), text(date, "Дата огляду"),
            number(f2, "Тяжкість пошкодження"));
    }

    // Клієнтська валідація: лише швидка перевірка заповненості й формату чисел.
    // Предметні правила (діапазон 0–100, існування записів, переходи станів) перевіряє Service на сервері.
    private static String text(TextField field, String label) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) throw new IllegalArgumentException("Заповніть поле «" + label + "»");
        return value;
    }
    private static double number(TextField field, String label) {
        String value = text(field, label).replace(',', '.');
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути числом");
        }
    }
    private static long wholeNumber(TextField field, String label) {
        String value = text(field, label);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    private void refreshAll() { refreshExhibits(); refreshInspections(); refreshRequests(); refreshSpecialists(); }
    private void refreshExhibits() { safe(() -> exhibits.setItems(FXCollections.observableArrayList(api.getExhibits()))); }
    private void refreshInspections() { safe(() -> inspections.setItems(FXCollections.observableArrayList(api.getInspections()))); }
    private void refreshRequests() { safe(() -> requests.setItems(FXCollections.observableArrayList(api.getRequests()))); }
    private void refreshSpecialists() { safe(() -> specialists.setItems(FXCollections.observableArrayList(api.getSpecialists()))); }

    private <T> T selected(TableView<T> table) {
        T v = table.getSelectionModel().getSelectedItem();
        if (v == null) throw new IllegalArgumentException("Виберіть запис");
        return v;
    }
    private void safe(Runnable action) {
        try { action.run(); } catch (Exception e) { LOG.warning(e.getMessage()); showError(e); }
    }
    private void showError(Throwable e) {
        Alert d = new Alert(Alert.AlertType.ERROR, e == null ? "Невідома помилка" : e.getMessage(), ButtonType.OK);
        d.setTitle("Помилка");
        d.setHeaderText("Операцію не виконано");
        d.showAndWait();
    }
    private Pane layout(TableView<?> table, Pane form) {
        // колонки розтягуються на всю ширину таблиці, щоб текст не обрізався
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        SplitPane p = new SplitPane(table, form);
        p.setDividerPositions(0.68);
        BorderPane root = new BorderPane(p);
        root.setPadding(new Insets(10));
        return root;
    }
    private <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(property));
        return c;
    }
    public static void main(String[] args) { launch(args); }
}
