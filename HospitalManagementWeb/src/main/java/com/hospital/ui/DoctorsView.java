package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import com.hospital.model.Doctor;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DoctorsView extends VBox {

    private final HospitalDao dao;
    private TableView<Doctor> table;
    private TextField searchField;
    private ComboBox<String> specFilter;
    private Label countLabel;

    public DoctorsView(HospitalDao dao) {
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
        Label title = new Label("Medical Specialists Directory");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        countLabel = new Label("Loading...");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search doctors by name or contact...");
        searchField.setPrefWidth(240);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData());

        specFilter = new ComboBox<>();
        specFilter.getItems().add("All Specializations");
        specFilter.setValue("All Specializations");
        specFilter.setOnAction(e -> loadData());

        Button btnNew = new Button("+ Add Doctor");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setOnAction(e -> showDoctorDialog(null));

        topBar.getChildren().addAll(titleBox, searchField, specFilter, btnNew);

        // Table Panel
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No doctors registered."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Doctor, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()));
        colId.setMaxWidth(60);

        TableColumn<Doctor, String> colName = new TableColumn<>("Doctor Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        TableColumn<Doctor, String> colSpec = new TableColumn<>("Specialization");
        colSpec.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSpecialization()));
        colSpec.setCellFactory(col -> new TableCell<Doctor, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(UIUtils.createBadge(item, "badge-teal"));
                }
            }
        });

        TableColumn<Doctor, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));

        TableColumn<Doctor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<Doctor, Void> colActions = new TableColumn<>("Actions");
        colActions.setMaxWidth(160);
        colActions.setCellFactory(col -> new TableCell<Doctor, Void>() {
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
                    Doctor d = getTableView().getItems().get(getIndex());
                    showDoctorDialog(d);
                });

                btnDelete.setOnAction(e -> {
                    Doctor d = getTableView().getItems().get(getIndex());
                    if (UIUtils.showConfirm("Delete Doctor", "Remove " + d.getName() + " from hospital staff directory?")) {
                        dao.deleteDoctor(d.getId());
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

        table.getColumns().addAll(List.of(colId, colName, colSpec, colPhone, colEmail, colActions));
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().add(table);
        getChildren().addAll(topBar, panel);
    }

    private void loadData() {
        String query = searchField.getText();
        String spec = specFilter.getValue();
        List<Doctor> list = dao.getDoctors(query, spec);
        table.getItems().setAll(list);
        countLabel.setText(list.size() + " doctors active");
    }

    public void refresh() {
        String currentSpec = specFilter.getValue();
        specFilter.getItems().clear();
        specFilter.getItems().add("All Specializations");
        specFilter.getItems().addAll(dao.getDoctorSpecializations());
        specFilter.setValue(currentSpec != null && specFilter.getItems().contains(currentSpec) ? currentSpec : "All Specializations");

        loadData();
    }

    private void showDoctorDialog(Doctor existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Register Medical Specialist" : "Edit Doctor Profile");
        dialog.setHeaderText(existing == null ? "Add a new physician or specialist." : "Update profile for " + existing.getName());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField tfName = new TextField(existing != null ? existing.getName() : "");
        TextField tfSpec = new TextField(existing != null ? existing.getSpecialization() : "");
        TextField tfPhone = new TextField(existing != null ? existing.getPhone() : "");
        TextField tfEmail = new TextField(existing != null ? existing.getEmail() : "");

        grid.add(new Label("Doctor Full Name *:"), 0, 0);
        grid.add(tfName, 1, 0);

        grid.add(new Label("Specialization *:"), 0, 1);
        grid.add(tfSpec, 1, 1);

        grid.add(new Label("Phone:"), 0, 2);
        grid.add(tfPhone, 1, 2);

        grid.add(new Label("Email:"), 0, 3);
        grid.add(tfEmail, 1, 3);

        pane.setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String name = tfName.getText().trim();
                String spec = tfSpec.getText().trim();
                if (name.isEmpty() || spec.isEmpty()) {
                    UIUtils.showError("Validation Error", "Doctor name and specialization are required.");
                    return;
                }
                Doctor d = existing != null ? existing : new Doctor();
                d.setName(name);
                d.setSpecialization(spec);
                d.setPhone(tfPhone.getText().trim());
                d.setEmail(tfEmail.getText().trim());

                if (dao.saveDoctor(d)) {
                    refresh();
                } else {
                    UIUtils.showError("Save Error", "Could not save doctor profile.");
                }
            }
        });
    }
}
