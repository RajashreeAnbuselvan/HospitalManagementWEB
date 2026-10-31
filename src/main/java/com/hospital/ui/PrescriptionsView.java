package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import com.hospital.model.Appointment;
import com.hospital.model.Prescription;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.SimpleDateFormat;
import java.util.List;

public class PrescriptionsView extends VBox {

    private final HospitalDao dao;
    private TableView<Prescription> table;
    private TextField searchField;
    private Label countLabel;

    public PrescriptionsView(HospitalDao dao) {
        this.dao = dao;
        setSpacing(16);
        getStyleClass().add("content-area");

        initUI();
        refresh();
    }

    private void initUI() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Prescriptions & Medical Diagnoses");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        countLabel = new Label("Loading...");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search patient, diagnosis, medicine...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData(newVal));

        Button btnNew = new Button("+ New Prescription");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setOnAction(e -> showPrescriptionDialog());

        topBar.getChildren().addAll(titleBox, searchField, btnNew);

        // Table Panel
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No prescription records found."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Prescription, Number> colId = new TableColumn<>("Rx #");
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()));
        colId.setMaxWidth(60);

        TableColumn<Prescription, String> colPatient = new TableColumn<>("Patient");
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));

        TableColumn<Prescription, String> colDoctor = new TableColumn<>("Prescribing Doctor");
        colDoctor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDoctorName()));

        TableColumn<Prescription, String> colDiagnosis = new TableColumn<>("Diagnosis");
        colDiagnosis.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiagnosis()));

        TableColumn<Prescription, String> colMeds = new TableColumn<>("Medicines / Dosage");
        colMeds.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMedicines()));

        TableColumn<Prescription, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? new SimpleDateFormat("dd MMM yyyy").format(d.getValue().getCreatedAt()) : ""
        ));
        colDate.setMaxWidth(110);

        TableColumn<Prescription, Void> colActions = new TableColumn<>("Actions");
        colActions.setMaxWidth(160);
        colActions.setCellFactory(col -> new TableCell<Prescription, Void>() {
            private final Button btnSlip = new Button("📄 Rx Slip");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(6, btnSlip, btnDelete);

            {
                btnSlip.getStyleClass().add("btn-secondary");
                btnDelete.getStyleClass().add("btn-danger");
                btnSlip.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                box.setAlignment(Pos.CENTER);

                btnSlip.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    showSlipDialog(p);
                });

                btnDelete.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    if (UIUtils.showConfirm("Delete Prescription", "Delete prescription record #" + p.getId() + "?")) {
                        dao.deletePrescription(p.getId());
                        refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(List.of(colId, colPatient, colDoctor, colDiagnosis, colMeds, colDate, colActions));
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().add(table);
        getChildren().addAll(topBar, panel);
    }

    private void loadData(String query) {
        List<Prescription> list = dao.getPrescriptions(query);
        table.getItems().setAll(list);
        countLabel.setText(list.size() + " prescriptions logged");
    }

    public void refresh() {
        loadData(searchField.getText());
    }

    private void showPrescriptionDialog() {
        List<Appointment> appointments = dao.getAppointments("", "All Statuses", null);
        if (appointments.isEmpty()) {
            UIUtils.showInfo("No Appointments", "Please schedule an appointment first before writing a prescription.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Prescription & Clinical Diagnosis");
        dialog.setHeaderText("Link prescription to an appointment and document medical advice.");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<Appointment> cbAppointment = new ComboBox<>();
        cbAppointment.getItems().addAll(appointments);
        cbAppointment.setValue(appointments.get(0));
        cbAppointment.setPrefWidth(350);

        TextArea taDiagnosis = new TextArea();
        taDiagnosis.setPromptText("Clinical diagnosis / assessment...");
        taDiagnosis.setPrefRowCount(2);
        taDiagnosis.setPrefWidth(350);

        TextArea taMedicines = new TextArea();
        taMedicines.setPromptText("Medicine name, dosage, frequency, duration (e.g., Tab. Paracetamol 650mg 1-0-1 for 3 days)...");
        taMedicines.setPrefRowCount(4);
        taMedicines.setPrefWidth(350);

        TextArea taNotes = new TextArea();
        taNotes.setPromptText("Dietary advice, precautions, follow-up recommendations...");
        taNotes.setPrefRowCount(2);
        taNotes.setPrefWidth(350);

        grid.add(new Label("Select Consultation *:"), 0, 0);
        grid.add(cbAppointment, 1, 0);

        grid.add(new Label("Diagnosis *:"), 0, 1);
        grid.add(taDiagnosis, 1, 1);

        grid.add(new Label("Medicines / Rx *:"), 0, 2);
        grid.add(taMedicines, 1, 2);

        grid.add(new Label("Notes / Advice:"), 0, 3);
        grid.add(taNotes, 1, 3);

        pane.setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Appointment appt = cbAppointment.getValue();
                String diagnosis = taDiagnosis.getText().trim();
                String medicines = taMedicines.getText().trim();

                if (appt == null || diagnosis.isEmpty() || medicines.isEmpty()) {
                    UIUtils.showError("Validation Error", "Please select an appointment and provide diagnosis and medicines.");
                    return;
                }

                Prescription pr = new Prescription();
                pr.setAppointmentId(appt.getId());
                pr.setDiagnosis(diagnosis);
                pr.setMedicines(medicines);
                pr.setNotes(taNotes.getText().trim());

                if (dao.savePrescription(pr)) {
                    refresh();
                } else {
                    UIUtils.showError("Error", "Could not save prescription.");
                }
            }
        });
    }

    private void showSlipDialog(Prescription p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Medical Prescription Slip #" + p.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox slip = new VBox(14);
        slip.setPadding(new Insets(24));
        slip.setStyle("-fx-background-color: #ffffff; -fx-border-color: #0d9488; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-pref-width: 480;");

        // Hospital Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("✚");
        icon.setStyle("-fx-font-size: 28px; -fx-text-fill: #0d9488; -fx-font-weight: bold;");

        VBox hText = new VBox(2);
        Label hTitle = new Label("CAREPOINT MULTISPECIALTY HOSPITAL");
        hTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        Label hSub = new Label("Outpatient Department · Electronic Health Record");
        hSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        hText.getChildren().addAll(hTitle, hSub);

        header.getChildren().addAll(icon, hText);

        Separator sep1 = new Separator();

        // Meta Info
        GridPane meta = new GridPane();
        meta.setHgap(16);
        meta.setVgap(6);
        meta.add(new Label("Rx Slip ID: #" + p.getId()), 0, 0);
        meta.add(new Label("Date: " + (p.getCreatedAt() != null ? new SimpleDateFormat("dd MMM yyyy, HH:mm").format(p.getCreatedAt()) : "N/A")), 1, 0);
        meta.add(new Label("Patient: " + p.getPatientName()), 0, 1);
        meta.add(new Label("Doctor: " + p.getDoctorName()), 1, 1);

        Separator sep2 = new Separator();

        // Diagnosis
        VBox diagBox = new VBox(4);
        Label diagTitle = new Label("Clinical Diagnosis:");
        diagTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label diagContent = new Label(p.getDiagnosis());
        diagContent.setWrapText(true);
        diagBox.getChildren().addAll(diagTitle, diagContent);

        // Rx Section
        VBox rxBox = new VBox(6);
        Label rxTitle = new Label("℞  Prescribed Medications:");
        rxTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0d9488;");
        Label rxMeds = new Label(p.getMedicines());
        rxMeds.setStyle("-fx-font-family: 'Consolas', monospace; -fx-background-color: #f8fafc; -fx-padding: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");
        rxMeds.setWrapText(true);
        rxBox.getChildren().addAll(rxTitle, rxMeds);

        // Notes
        VBox notesBox = new VBox(4);
        Label notesTitle = new Label("Advice & Precautions:");
        notesTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label notesContent = new Label(p.getNotes() != null && !p.getNotes().isBlank() ? p.getNotes() : "Follow general healthy lifestyle guidance.");
        notesContent.setWrapText(true);
        notesBox.getChildren().addAll(notesTitle, notesContent);

        // Signature line
        HBox footer = new HBox();
        footer.setAlignment(Pos.BOTTOM_RIGHT);
        footer.setPadding(new Insets(20, 0, 0, 0));
        VBox sig = new VBox(2);
        sig.setAlignment(Pos.CENTER);
        Label sigLine = new Label("____________________________");
        Label sigDoctor = new Label(p.getDoctorName() != null ? p.getDoctorName() : "Attending Physician");
        sigDoctor.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        sig.getChildren().addAll(sigLine, sigDoctor);
        footer.getChildren().add(sig);

        slip.getChildren().addAll(header, sep1, meta, sep2, diagBox, rxBox, notesBox, footer);
        pane.setContent(slip);

        dialog.showAndWait();
    }
}
