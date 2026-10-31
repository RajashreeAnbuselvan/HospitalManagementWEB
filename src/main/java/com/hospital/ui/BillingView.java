package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.DashboardStats;
import com.hospital.model.Patient;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class BillingView extends VBox {

    private final HospitalDao dao;
    private TableView<Bill> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Label countLabel;
    private HBox summaryStrip;

    public BillingView(HospitalDao dao) {
        this.dao = dao;
        setSpacing(16);
        getStyleClass().add("content-area");

        initUI();
        refresh();
    }

    private void initUI() {
        // Summary Strip
        summaryStrip = new HBox(16);

        // Header and Controls
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Billing & Revenue Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        countLabel = new Label("Loading...");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, countLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search patient, invoice description...");
        searchField.setPrefWidth(240);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Bills", "Pending", "Paid");
        statusFilter.setValue("All Bills");
        statusFilter.setOnAction(e -> loadData());

        Button btnNew = new Button("+ Generate Bill");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setOnAction(e -> showGenerateBillDialog());

        topBar.getChildren().addAll(titleBox, searchField, statusFilter, btnNew);

        // Table Panel
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        table = new TableView<>();
        table.setPlaceholder(new Label("No billing records found."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Bill, Number> colId = new TableColumn<>("Invoice #");
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()));
        colId.setMaxWidth(75);

        TableColumn<Bill, String> colPatient = new TableColumn<>("Patient Name");
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));

        TableColumn<Bill, String> colDesc = new TableColumn<>("Billing Items / Description");
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Bill, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().getAmount()));
        colAmount.setMaxWidth(110);

        TableColumn<Bill, Boolean> colStatus = new TableColumn<>("Payment Status");
        colStatus.setMaxWidth(110);
        colStatus.setCellFactory(col -> new TableCell<Bill, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Bill b = getTableRow().getItem();
                    setGraphic(UIUtils.createBadge(b.isPaid() ? "Paid" : "Pending", b.isPaid() ? "badge-paid" : "badge-pending"));
                }
            }
        });

        TableColumn<Bill, String> colDate = new TableColumn<>("Invoice Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? new SimpleDateFormat("dd MMM yyyy").format(d.getValue().getCreatedAt()) : ""
        ));
        colDate.setMaxWidth(110);

        TableColumn<Bill, Void> colActions = new TableColumn<>("Actions");
        colActions.setMinWidth(210);
        colActions.setMaxWidth(230);
        colActions.setCellFactory(col -> new TableCell<Bill, Void>() {
            private final Button btnPay = new Button("Mark Paid");
            private final Button btnInvoice = new Button("Receipt");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(5, btnPay, btnInvoice, btnDelete);

            {
                btnPay.getStyleClass().add("btn-success");
                btnInvoice.getStyleClass().add("btn-secondary");
                btnDelete.getStyleClass().add("btn-danger");
                btnPay.setStyle("-fx-font-size: 11px; -fx-padding: 3 6;");
                btnInvoice.setStyle("-fx-font-size: 11px; -fx-padding: 3 6;");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 3 6;");
                box.setAlignment(Pos.CENTER);

                btnPay.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    dao.markBillPaid(b.getId());
                    refresh();
                });

                btnInvoice.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    showReceiptDialog(b);
                });

                btnDelete.setOnAction(e -> {
                    Bill b = getTableView().getItems().get(getIndex());
                    if (UIUtils.showConfirm("Delete Bill", "Delete invoice record #" + b.getId() + "?")) {
                        dao.deleteBill(b.getId());
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
                    Bill b = getTableRow().getItem();
                    btnPay.setVisible(!b.isPaid());
                    btnPay.setManaged(!b.isPaid());
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(List.of(colId, colPatient, colDesc, colAmount, colStatus, colDate, colActions));
        VBox.setVgrow(table, Priority.ALWAYS);

        panel.getChildren().add(table);
        getChildren().addAll(summaryStrip, topBar, panel);
    }

    private void loadData() {
        String query = searchField.getText();
        String status = statusFilter.getValue();
        List<Bill> list = dao.getBills(query, status);
        table.getItems().setAll(list);
        countLabel.setText(list.size() + " billing entries");
    }

    public void refresh() {
        DashboardStats stats = dao.getDashboardStats();
        summaryStrip.getChildren().clear();
        summaryStrip.getChildren().addAll(
                UIUtils.createKpiCard("Collected Revenue", "₹ " + stats.getTotalRevenue(), "Fully Settled", "#10b981"),
                UIUtils.createKpiCard("Pending Receivables", "₹ " + stats.getPendingRevenue(), stats.getUnpaidBillsCount() + " Invoices Pending", "#f59e0b"),
                UIUtils.createKpiCard("Total Billed Volume", "₹ " + stats.getTotalRevenue().add(stats.getPendingRevenue()), "Cumulative Billing", "#6366f1")
        );

        loadData();
    }

    private void showGenerateBillDialog() {
        List<Patient> patients = dao.getPatients("");
        if (patients.isEmpty()) {
            UIUtils.showInfo("No Patients", "Please register a patient first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generate Patient Billing Invoice");
        dialog.setHeaderText("Create an itemized hospital bill for consultation, tests, or admission.");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<Patient> cbPatient = new ComboBox<>();
        cbPatient.getItems().addAll(patients);
        cbPatient.setValue(patients.get(0));
        cbPatient.setPrefWidth(300);

        ComboBox<Appointment> cbAppointment = new ComboBox<>();
        cbAppointment.getItems().addAll(dao.getAppointments("", "All Statuses", null));
        cbAppointment.setPromptText("Optional consultation reference");
        cbAppointment.setPrefWidth(300);

        TextField tfAmount = new TextField("500.00");
        tfAmount.setPrefWidth(300);

        TextArea taDesc = new TextArea("Consultation & Routine Examination");
        taDesc.setPrefRowCount(3);
        taDesc.setPrefWidth(300);

        CheckBox cbPaidNow = new CheckBox("Mark invoice as Paid immediately");

        grid.add(new Label("Select Patient *:"), 0, 0);
        grid.add(cbPatient, 1, 0);

        grid.add(new Label("Linked Appointment:"), 0, 1);
        grid.add(cbAppointment, 1, 1);

        grid.add(new Label("Bill Amount (₹) *:"), 0, 2);
        grid.add(tfAmount, 1, 2);

        grid.add(new Label("Description *:"), 0, 3);
        grid.add(taDesc, 1, 3);

        grid.add(new Label("Payment Status:"), 0, 4);
        grid.add(cbPaidNow, 1, 4);

        pane.setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Patient p = cbPatient.getValue();
                String desc = taDesc.getText().trim();
                BigDecimal amount;
                try {
                    amount = new BigDecimal(tfAmount.getText().trim());
                    if (amount.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                } catch (Exception ex) {
                    UIUtils.showError("Validation Error", "Please enter a valid positive billing amount.");
                    return;
                }

                if (p == null || desc.isEmpty()) {
                    UIUtils.showError("Validation Error", "Patient and description are required.");
                    return;
                }

                Bill b = new Bill();
                b.setPatientId(p.getId());
                Appointment appt = cbAppointment.getValue();
                if (appt != null) b.setAppointmentId(appt.getId());
                b.setAmount(amount);
                b.setDescription(desc);
                b.setPaid(cbPaidNow.isSelected());

                if (dao.saveBill(b)) {
                    refresh();
                } else {
                    UIUtils.showError("Error", "Could not generate bill.");
                }
            }
        });
    }

    private void showReceiptDialog(Bill b) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Hospital Billing Receipt #" + b.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox receipt = new VBox(14);
        receipt.setPadding(new Insets(24));
        receipt.setStyle("-fx-background-color: #ffffff; -fx-border-color: #0d9488; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-pref-width: 440;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("₹");
        icon.setStyle("-fx-font-size: 28px; -fx-text-fill: #0d9488; -fx-font-weight: bold;");

        VBox hText = new VBox(2);
        Label hTitle = new Label("CAREPOINT MULTISPECIALTY HOSPITAL");
        hTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        Label hSub = new Label("Official Patient Payment Receipt & Tax Invoice");
        hSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        hText.getChildren().addAll(hTitle, hSub);

        header.getChildren().addAll(icon, hText);

        Separator sep1 = new Separator();

        GridPane meta = new GridPane();
        meta.setHgap(16);
        meta.setVgap(6);
        meta.add(new Label("Invoice No: #" + b.getId()), 0, 0);
        meta.add(new Label("Date: " + (b.getCreatedAt() != null ? new SimpleDateFormat("dd MMM yyyy, HH:mm").format(b.getCreatedAt()) : "N/A")), 1, 0);
        meta.add(new Label("Patient: " + b.getPatientName()), 0, 1);
        meta.add(new Label("Status: " + (b.isPaid() ? "PAID" : "PENDING")), 1, 1);

        Separator sep2 = new Separator();

        VBox itemsBox = new VBox(8);
        HBox itemHeader = new HBox();
        Label lblDescH = new Label("Item Description");
        lblDescH.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(lblDescH, Priority.ALWAYS);
        Label lblAmtH = new Label("Amount");
        lblAmtH.setStyle("-fx-font-weight: bold;");
        itemHeader.getChildren().addAll(lblDescH, lblAmtH);

        HBox itemRow = new HBox();
        Label lblDesc = new Label(b.getDescription());
        lblDesc.setWrapText(true);
        HBox.setHgrow(lblDesc, Priority.ALWAYS);
        Label lblAmt = new Label("₹ " + b.getAmount());
        itemRow.getChildren().addAll(lblDesc, lblAmt);

        itemsBox.getChildren().addAll(itemHeader, new Separator(), itemRow);

        Separator sep3 = new Separator();

        HBox totalBox = new HBox();
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        Label totalLbl = new Label("Total Payable: ₹ " + b.getAmount());
        totalLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0d9488;");
        totalBox.getChildren().add(totalLbl);

        receipt.getChildren().addAll(header, sep1, meta, sep2, itemsBox, sep3, totalBox);
        pane.setContent(receipt);

        dialog.showAndWait();
    }
}
