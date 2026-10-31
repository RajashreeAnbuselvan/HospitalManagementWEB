package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.DashboardStats;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DashboardView extends VBox {

    private final HospitalDao dao;
    private final MainLayout mainLayout;

    private HBox kpiBox;
    private TableView<Appointment> appointmentsTable;
    private TableView<Bill> billsTable;

    public DashboardView(HospitalDao dao, MainLayout mainLayout) {
        this.dao = dao;
        this.mainLayout = mainLayout;
        setSpacing(20);
        getStyleClass().add("content-area");

        initUI();
        refresh();
    }

    private void initUI() {
        // Welcome Banner
        HBox banner = new HBox(15);
        banner.getStyleClass().add("welcome-banner");
        banner.setAlignment(Pos.CENTER_LEFT);

        VBox bannerText = new VBox(4);
        HBox.setHgrow(bannerText, Priority.ALWAYS);

        Label eyebrow = new Label("CAREPOINT OPERATIONS CENTER");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2dd4bf; -fx-letter-spacing: 1px;");

        Label title = new Label("Hospital Intelligence & Quick Overview");
        title.getStyleClass().add("welcome-title");

        Label desc = new Label("Live overview of registered patients, medical specialists, consultations, and finance.");
        desc.getStyleClass().add("welcome-desc");

        bannerText.getChildren().addAll(eyebrow, title, desc);

        Label crossIcon = new Label("✚");
        crossIcon.setStyle("-fx-font-size: 44px; -fx-text-fill: rgba(45, 212, 191, 0.25); -fx-font-weight: bold;");

        banner.getChildren().addAll(bannerText, crossIcon);

        // KPI Box
        kpiBox = new HBox(16);

        // Quick Actions
        VBox quickSection = new VBox(10);
        Label quickTitle = new Label("Quick Operational Actions");
        quickTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        HBox quickGrid = new HBox(14);
        quickGrid.getChildren().addAll(
                UIUtils.createQuickCard("✚", "Register Patient", "Add a new patient record", () -> mainLayout.showView("patients")),
                UIUtils.createQuickCard("⚕", "Add Doctor", "Register medical staff", () -> mainLayout.showView("doctors")),
                UIUtils.createQuickCard("📅", "Book Appointment", "Schedule a consultation", () -> mainLayout.showView("appointments")),
                UIUtils.createQuickCard("₹", "Generate Bill", "Create patient billing invoice", () -> mainLayout.showView("billing"))
        );
        quickSection.getChildren().addAll(quickTitle, quickGrid);

        // Split Panels: Recent Appointments & Recent Bills
        HBox bottomSection = new HBox(16);
        VBox.setVgrow(bottomSection, Priority.ALWAYS);

        // Appointments Panel
        VBox apptPanel = new VBox(12);
        apptPanel.getStyleClass().add("panel");
        HBox.setHgrow(apptPanel, Priority.ALWAYS);

        HBox apptHead = new HBox(10);
        apptHead.setAlignment(Pos.CENTER_LEFT);
        Label apptTitle = new Label("Recent Consultations");
        apptTitle.getStyleClass().add("panel-title");
        HBox.setHgrow(apptTitle, Priority.ALWAYS);

        Hyperlink viewAllAppts = new Hyperlink("View All →");
        viewAllAppts.setStyle("-fx-text-fill: #0d9488; -fx-font-weight: bold;");
        viewAllAppts.setOnAction(e -> mainLayout.showView("appointments"));
        apptHead.getChildren().addAll(apptTitle, viewAllAppts);

        appointmentsTable = new TableView<>();
        appointmentsTable.setPlaceholder(new Label("No recent appointments scheduled."));
        appointmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Appointment, String> colPatient = new TableColumn<>("Patient");
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));

        TableColumn<Appointment, String> colDoctor = new TableColumn<>("Doctor");
        colDoctor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDoctorName()));

        TableColumn<Appointment, String> colDate = new TableColumn<>("Date / Time");
        colDate.setCellValueFactory(d -> new SimpleStringProperty((d.getValue().getAppointmentDate() != null ? d.getValue().getAppointmentDate().toString() : "") + " " + d.getValue().getAppointmentTime()));

        TableColumn<Appointment, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
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

        appointmentsTable.getColumns().addAll(List.of(colPatient, colDoctor, colDate, colStatus));
        VBox.setVgrow(appointmentsTable, Priority.ALWAYS);
        apptPanel.getChildren().addAll(apptHead, appointmentsTable);

        // Billing Panel
        VBox billPanel = new VBox(12);
        billPanel.getStyleClass().add("panel");
        HBox.setHgrow(billPanel, Priority.ALWAYS);

        HBox billHead = new HBox(10);
        billHead.setAlignment(Pos.CENTER_LEFT);
        Label billTitle = new Label("Recent Billing Records");
        billTitle.getStyleClass().add("panel-title");
        HBox.setHgrow(billTitle, Priority.ALWAYS);

        Hyperlink viewAllBills = new Hyperlink("View All →");
        viewAllBills.setStyle("-fx-text-fill: #0d9488; -fx-font-weight: bold;");
        viewAllBills.setOnAction(e -> mainLayout.showView("billing"));
        billHead.getChildren().addAll(billTitle, viewAllBills);

        billsTable = new TableView<>();
        billsTable.setPlaceholder(new Label("No billing records yet."));
        billsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Bill, String> colBillPatient = new TableColumn<>("Patient");
        colBillPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));

        TableColumn<Bill, String> colBillDesc = new TableColumn<>("Description");
        colBillDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        TableColumn<Bill, String> colBillAmt = new TableColumn<>("Amount");
        colBillAmt.setCellValueFactory(d -> new SimpleStringProperty("₹ " + d.getValue().getAmount()));

        TableColumn<Bill, Boolean> colBillPaid = new TableColumn<>("Status");
        colBillPaid.setCellFactory(col -> new TableCell<Bill, Boolean>() {
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

        billsTable.getColumns().addAll(List.of(colBillPatient, colBillDesc, colBillAmt, colBillPaid));
        VBox.setVgrow(billsTable, Priority.ALWAYS);
        billPanel.getChildren().addAll(billHead, billsTable);

        bottomSection.getChildren().addAll(apptPanel, billPanel);

        getChildren().addAll(banner, kpiBox, quickSection, bottomSection);
    }

    public void refresh() {
        DashboardStats stats = dao.getDashboardStats();
        kpiBox.getChildren().clear();
        kpiBox.getChildren().addAll(
                UIUtils.createKpiCard("Total Patients", String.valueOf(stats.getPatientCount()), "Registered Records", "#0ea5e9"),
                UIUtils.createKpiCard("Medical Specialists", String.valueOf(stats.getDoctorCount()), "Active Doctors", "#10b981"),
                UIUtils.createKpiCard("Appointments", String.valueOf(stats.getAppointmentCount()), "Total Scheduled", "#6366f1"),
                UIUtils.createKpiCard("Pending Invoices", String.valueOf(stats.getUnpaidBillsCount()), "₹ " + stats.getPendingRevenue() + " Uncollected", "#f59e0b")
        );

        appointmentsTable.getItems().setAll(dao.getRecentAppointments(6));
        billsTable.getItems().setAll(dao.getBills("", "All").stream().limit(6).toList());
    }
}
