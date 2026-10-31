package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentsView extends VBox {

    private final HospitalDao dao;
    private final MainLayout mainLayout;
    private TableView<Appointment> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private DatePicker datePicker;
    private Label countLabel;

    public AppointmentsView(HospitalDao dao, MainLayout mainLayout) {
        this.dao = dao;
        this.mainLayout = mainLayout;
        setSpacing(16);
        getStyleClass().add("content-area");

        initUI();
        refresh();
    }

    private void initUI() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Appointment Schedules");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        countLabel = new Label("Loading...");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search patient, doctor, reason...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "Scheduled", "Completed", "Cancelled");
        statusFilter.setValue("All Statuses");
        statusFilter.setOnAction(e -> loadData());

        datePicker = new DatePicker();
        datePicker.setPromptText("Filter date");
        datePicker.setPrefWidth(130);
        datePicker.setOnAction(e -> loadData());

        Button btnClearDate = new Button("✕");
        btnClearDate.setTooltip(new Tooltip("Clear date filter"));
        btnClearDate.getStyleClass().add("btn-secondary");
        btnClearDate.setOnAction(e -> {
            datePicker.setValue(null);
            loadData();
        });

        Button btnNew = new Button("+ Book Appointment");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setOnAction(e -> showBookDialog());

        topBar.getChildren().addAll(titleBox, searchField, statusFilter, datePicker, btnClearDate, btnNew);

        // Table Panel
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No appointments found matching criteria."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Appointment, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()));
        colId.setMaxWidth(60);

        TableColumn<Appointment, String> colPatient = new TableColumn<>("Patient");
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));

        TableColumn<Appointment, String> colDoctor = new TableColumn<>("Assigned Doctor");
        colDoctor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDoctorName()));

        TableColumn<Appointment, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAppointmentDate() != null ? d.getValue().getAppointmentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : ""));
        colDate.setMaxWidth(110);

        TableColumn<Appointment, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAppointmentTime()));
        colTime.setMaxWidth(80);

        TableColumn<Appointment, String> colReason = new TableColumn<>("Reason for Visit");
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));

        TableColumn<Appointment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setMaxWidth(110);
        colStatus.setCellFactory(col -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    String cls = "badge-scheduled";
                    if ("Completed".equalsIgnoreCase(status)) cls = "badge-completed";
                    else if ("Cancelled".equalsIgnoreCase(status)) cls = "badge-cancelled";
                    setGraphic(UIUtils.createBadge(status, cls));
                }
            }
        });

        TableColumn<Appointment, Void> colActions = new TableColumn<>("Actions");
        colActions.setMinWidth(210);
        colActions.setMaxWidth(230);
        colActions.setCellFactory(col -> new TableCell<Appointment, Void>() {
            private final Button btnDone = new Button("✓ Done");
            private final Button btnCancel = new Button("✕ Cancel");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(5, btnDone, btnCancel, btnDelete);

            {
                btnDone.getStyleClass().add("btn-success");
                btnCancel.getStyleClass().add("btn-secondary");
                btnDelete.getStyleClass().add("btn-danger");
                btnDone.setStyle("-fx-font-size: 11px; -fx-padding: 3 6;");
                btnCancel.setStyle("-fx-font-size: 11px; -fx-padding: 3 6;");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 3 6;");
                box.setAlignment(Pos.CENTER);

                btnDone.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    dao.updateAppointmentStatus(a.getId(), "Completed");
                    refresh();
                });

                btnCancel.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    dao.updateAppointmentStatus(a.getId(), "Cancelled");
                    refresh();
                });

                btnDelete.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    if (UIUtils.showConfirm("Delete Appointment", "Are you sure you want to delete appointment #" + a.getId() + "?")) {
                        dao.deleteAppointment(a.getId());
                        refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Appointment a = getTableRow().getItem();
                    btnDone.setDisable("Completed".equalsIgnoreCase(a.getStatus()));
                    btnCancel.setDisable("Cancelled".equalsIgnoreCase(a.getStatus()));
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(List.of(colId, colPatient, colDoctor, colDate, colTime, colReason, colStatus, colActions));
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().add(table);
        getChildren().addAll(topBar, panel);
    }

    private void loadData() {
        String query = searchField.getText();
        String status = statusFilter.getValue();
        LocalDate date = datePicker.getValue();
        List<Appointment> list = dao.getAppointments(query, status, date);
        table.getItems().setAll(list);
        countLabel.setText(list.size() + " appointments scheduled");
    }

    public void refresh() {
        loadData();
    }

    public void showBookDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book New Patient Consultation");
        dialog.setHeaderText("Select patient, doctor, date and preferred consultation time slot.");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<Patient> cbPatient = new ComboBox<>();
        cbPatient.getItems().addAll(dao.getPatients(""));
        if (!cbPatient.getItems().isEmpty()) cbPatient.setValue(cbPatient.getItems().get(0));
        cbPatient.setPrefWidth(300);

        ComboBox<Doctor> cbDoctor = new ComboBox<>();
        cbDoctor.getItems().addAll(dao.getDoctors("", ""));
        if (!cbDoctor.getItems().isEmpty()) cbDoctor.setValue(cbDoctor.getItems().get(0));
        cbDoctor.setPrefWidth(300);

        DatePicker dpDate = new DatePicker(LocalDate.now());
        dpDate.setPrefWidth(300);

        ComboBox<String> cbTime = new ComboBox<>();
        cbTime.getItems().addAll("09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00");
        cbTime.setValue("10:00");
        cbTime.setPrefWidth(300);

        TextArea taReason = new TextArea();
        taReason.setPromptText("Symptoms or consultation reason...");
        taReason.setPrefRowCount(3);
        taReason.setPrefWidth(300);

        grid.add(new Label("Select Patient *:"), 0, 0);
        grid.add(cbPatient, 1, 0);

        grid.add(new Label("Select Doctor *:"), 0, 1);
        grid.add(cbDoctor, 1, 1);

        grid.add(new Label("Appointment Date *:"), 0, 2);
        grid.add(dpDate, 1, 2);

        grid.add(new Label("Time Slot *:"), 0, 3);
        grid.add(cbTime, 1, 3);

        grid.add(new Label("Reason:"), 0, 4);
        grid.add(taReason, 1, 4);

        pane.setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Patient patient = cbPatient.getValue();
                Doctor doctor = cbDoctor.getValue();
                LocalDate date = dpDate.getValue();
                String time = cbTime.getValue();

                if (patient == null || doctor == null || date == null) {
                    UIUtils.showError("Validation Error", "Please select a patient, doctor, and consultation date.");
                    return;
                }

                Appointment a = new Appointment();
                a.setPatientId(patient.getId());
                a.setDoctorId(doctor.getId());
                a.setAppointmentDate(date);
                a.setAppointmentTime(time);
                a.setReason(taReason.getText().trim());
                a.setStatus("Scheduled");

                if (dao.saveAppointment(a)) {
                    refresh();
                } else {
                    UIUtils.showError("Booking Error", "Could not book appointment.");
                }
            }
        });
    }
}
