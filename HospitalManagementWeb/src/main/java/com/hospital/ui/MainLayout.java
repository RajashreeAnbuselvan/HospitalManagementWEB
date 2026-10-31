package com.hospital.ui;

import com.hospital.dao.HospitalDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainLayout extends BorderPane {

    private final HospitalDao dao;
    private final Map<String, Button> navButtons = new HashMap<>();
    private final Map<String, Node> views = new HashMap<>();
    private final StackPane contentStack = new StackPane();
    private Label topTitle;
    private Label topEyebrow;

    public MainLayout() {
        this.dao = new HospitalDao();
        initUI();
    }

    private void initUI() {
        // --- Sidebar ---
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");

        // Brand
        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("brand-box");

        Label brandIcon = new Label("✚");
        brandIcon.getStyleClass().add("brand-icon");

        VBox brandText = new VBox(2);
        Label brandTitle = new Label("CarePoint");
        brandTitle.getStyleClass().add("brand-title");
        Label brandSub = new Label("HOSPITAL MANAGEMENT");
        brandSub.getStyleClass().add("brand-sub");
        brandText.getChildren().addAll(brandTitle, brandSub);

        brand.getChildren().addAll(brandIcon, brandText);

        // Nav List
        VBox navList = new VBox(6);
        navList.setPadding(new Insets(16, 0, 16, 0));
        VBox.setVgrow(navList, Priority.ALWAYS);

        addNavButton(navList, "dashboard", "📊  Dashboard Overview");
        addNavButton(navList, "patients", "👥  Patient Directory");
        addNavButton(navList, "doctors", "🩺  Medical Specialists");
        addNavButton(navList, "appointments", "📅  Appointments");
        addNavButton(navList, "prescriptions", "💊  Prescriptions & Rx");
        addNavButton(navList, "billing", "💳  Billing & Finance");

        // Sidebar Footer Status
        VBox statusBox = new VBox(4);
        statusBox.getStyleClass().add("sidebar-status");

        HBox statusLine = new HBox(6);
        statusLine.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;");
        Label statusTxt = new Label("System Online");
        statusTxt.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");
        statusLine.getChildren().addAll(dot, statusTxt);

        Label dbTxt = new Label("JavaFX 21 · H2 Embedded");
        dbTxt.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");

        statusBox.getChildren().addAll(statusLine, dbTxt);

        sidebar.getChildren().addAll(brand, navList, statusBox);
        setLeft(sidebar);

        // --- Topbar ---
        HBox topbar = new HBox(16);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.getStyleClass().add("topbar");

        VBox titleCol = new VBox(2);
        topEyebrow = new Label("CLINICAL OPERATIONS");
        topEyebrow.getStyleClass().add("topbar-eyebrow");
        topTitle = new Label("Dashboard Overview");
        topTitle.getStyleClass().add("topbar-title");
        titleCol.getChildren().addAll(topEyebrow, topTitle);
        HBox.setHgrow(titleCol, Priority.ALWAYS);

        // Date Display
        Label dateLbl = new Label("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        dateLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-weight: 500;");

        // User Avatar Chip
        HBox userChip = new HBox(8);
        userChip.setAlignment(Pos.CENTER_LEFT);
        userChip.getStyleClass().add("user-badge");

        Label avatar = new Label("CP");
        avatar.getStyleClass().add("avatar-circle");

        VBox userInfo = new VBox(1);
        Label userName = new Label("Administrator");
        userName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label userRole = new Label("Hospital Staff");
        userRole.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        userInfo.getChildren().addAll(userName, userRole);

        userChip.getChildren().addAll(avatar, userInfo);
        topbar.getChildren().addAll(titleCol, dateLbl, userChip);
        setTop(topbar);

        // --- Content Area ---
        setCenter(contentStack);

        // Lazy initialize views
        showView("dashboard");
    }

    private void addNavButton(VBox container, String viewKey, String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> showView(viewKey));
        navButtons.put(viewKey, btn);
        container.getChildren().add(btn);
    }

    public void showView(String viewKey) {
        // Highlight active nav
        navButtons.forEach((k, b) -> {
            b.getStyleClass().remove("nav-button-active");
            if (k.equals(viewKey)) b.getStyleClass().add("nav-button-active");
        });

        Node view = views.computeIfAbsent(viewKey, key -> {
            return switch (key) {
                case "patients" -> new PatientsView(dao);
                case "doctors" -> new DoctorsView(dao);
                case "appointments" -> new AppointmentsView(dao, this);
                case "prescriptions" -> new PrescriptionsView(dao);
                case "billing" -> new BillingView(dao);
                default -> new DashboardView(dao, this);
            };
        });

        // Update titles
        switch (viewKey) {
            case "patients" -> {
                topEyebrow.setText("PATIENT MANAGEMENT");
                topTitle.setText("Patient Registry & Medical Profiles");
                ((PatientsView) view).refresh();
            }
            case "doctors" -> {
                topEyebrow.setText("MEDICAL STAFF");
                topTitle.setText("Specialist Physicians Directory");
                ((DoctorsView) view).refresh();
            }
            case "appointments" -> {
                topEyebrow.setText("CLINICAL SCHEDULING");
                topTitle.setText("Consultation Appointments");
                ((AppointmentsView) view).refresh();
            }
            case "prescriptions" -> {
                topEyebrow.setText("DIAGNOSTICS & PHARMACY");
                topTitle.setText("Prescriptions & Treatment Records");
                ((PrescriptionsView) view).refresh();
            }
            case "billing" -> {
                topEyebrow.setText("FINANCIAL OPERATIONS");
                topTitle.setText("Invoices & Payment Tracking");
                ((BillingView) view).refresh();
            }
            default -> {
                topEyebrow.setText("CLINICAL OPERATIONS");
                topTitle.setText("CarePoint Intelligence Dashboard");
                ((DashboardView) view).refresh();
            }
        }

        contentStack.getChildren().setAll(view);
    }
}
