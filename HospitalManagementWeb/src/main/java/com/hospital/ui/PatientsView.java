package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import com.hospital.model.Patient;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class PatientsView extends VBox {

    private final HospitalDao dao;
    private TableView<Patient> table;
    private TextField searchField;
    private Label countLabel;

    public PatientsView(HospitalDao dao) {
        this.dao = dao;
        setSpacing(16);
        getStyleClass().add("content-area");

        initUI();
        refresh();
    }

    private void initUI() {
        // Header & Actions
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Patient Directory");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        countLabel = new Label("Loading...");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search name, phone, medical history...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData(newVal));

        Button btnNew = new Button("+ Register Patient");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setOnAction(e -> showPatientDialog(null));

        topBar.getChildren().addAll(titleBox, searchField, btnNew);

        // Table Panel
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No patients found."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Patient, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()));
        colId.setMaxWidth(60);
        colId.setMinWidth(50);

        TableColumn<Patient, String> colName = new TableColumn<>("Patient Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Patient, Number> colAge = new TableColumn<>("Age");
        colAge.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getAge()));
        colAge.setMaxWidth(70);

        TableColumn<Patient, String> colGender = new TableColumn<>("Gender");
        colGender.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGender()));
        colGender.setMaxWidth(90);

        TableColumn<Patient, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));

        TableColumn<Patient, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<Patient, String> colHistory = new TableColumn<>("Medical Notes / History");
        colHistory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMedicalHistory()));

        TableColumn<Patient, Void> colActions = new TableColumn<>("Actions");
        colActions.setMinWidth(140);
        colActions.setMaxWidth(160);
        colActions.setCellFactory(col -> new TableCell<Patient, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnDelete.getStyleClass().add("btn-danger");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    showPatientDialog(p);
                });

                btnDelete.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    if (UIUtils.showConfirm("Delete Patient", "Are you sure you want to delete patient #" + p.getId() + " (" + p.getName() + ")?")) {
                        dao.deletePatient(p.getId());
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

        table.getColumns().addAll(List.of(colId, colName, colAge, colGender, colPhone, colEmail, colHistory, colActions));
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().add(table);
        getChildren().addAll(topBar, panel);
    }

    private void loadData(String query) {
        List<Patient> list = dao.getPatients(query);
        table.getItems().setAll(list);
        countLabel.setText(list.size() + " patients found");
    }

    public void refresh() {
        loadData(searchField.getText());
    }

    private void showPatientDialog(Patient existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Register New Patient" : "Edit Patient Record");
        dialog.setHeaderText(existing == null ? "Enter patient demographic and medical details." : "Update details for patient #" + existing.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField tfName = new TextField(existing != null ? existing.getName() : "");
        TextField tfAge = new TextField(existing != null && existing.getAge() > 0 ? String.valueOf(existing.getAge()) : "");
        ComboBox<String> cbGender = new ComboBox<>();
        cbGender.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        cbGender.setValue(existing != null && existing.getGender() != null ? existing.getGender() : "Male");

        TextField tfPhone = new TextField(existing != null ? existing.getPhone() : "");
        TextField tfEmail = new TextField(existing != null ? existing.getEmail() : "");
        TextArea taHistory = new TextArea(existing != null ? existing.getMedicalHistory() : "");
        taHistory.setPrefRowCount(3);

        grid.add(new Label("Full Name *:"), 0, 0);
        grid.add(tfName, 1, 0);

        grid.add(new Label("Age:"), 0, 1);
        grid.add(tfAge, 1, 1);

        grid.add(new Label("Gender:"), 0, 2);
        grid.add(cbGender, 1, 2);

        grid.add(new Label("Phone:"), 0, 3);
        grid.add(tfPhone, 1, 3);

        grid.add(new Label("Email:"), 0, 4);
        grid.add(tfEmail, 1, 4);

        grid.add(new Label("Medical History:"), 0, 5);
        grid.add(taHistory, 1, 5);

        pane.setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String name = tfName.getText().trim();
                if (name.isEmpty()) {
                    UIUtils.showError("Validation Error", "Patient full name is required.");
                    return;
                }
                int age = 0;
                try {
                    if (!tfAge.getText().isBlank()) age = Integer.parseInt(tfAge.getText().trim());
                } catch (NumberFormatException ignored) {}

                Patient p = existing != null ? existing : new Patient();
                p.setName(name);
                p.setAge(age);
                p.setGender(cbGender.getValue());
                p.setPhone(tfPhone.getText().trim());
                p.setEmail(tfEmail.getText().trim());
                p.setMedicalHistory(taHistory.getText().trim());

                if (dao.savePatient(p)) {
                    refresh();
                } else {
                    UIUtils.showError("Save Error", "Could not save patient details to database.");
                }
            }
        });
    }
}
