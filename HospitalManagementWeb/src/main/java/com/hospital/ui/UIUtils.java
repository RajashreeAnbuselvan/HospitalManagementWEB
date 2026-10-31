package com.hospital.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UIUtils {

    public static Label createBadge(String text, String typeClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("badge", typeClass);
        return label;
    }

    public static VBox createKpiCard(String title, String value, String subtitle, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("kpi-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("kpi-value");

        Label lblSub = new Label(subtitle);
        lblSub.getStyleClass().add("kpi-sub");

        Region bar = new Region();
        bar.setPrefHeight(4);
        bar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2px;");

        card.getChildren().addAll(lblTitle, lblValue, lblSub, bar);
        return card;
    }

    public static VBox createQuickCard(String icon, String title, String subtitle, Runnable action) {
        VBox card = new VBox(4);
        card.getStyleClass().add("quick-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: #0d9488; -fx-font-weight: bold;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        header.getChildren().addAll(iconLbl, titleLbl);

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(header, subLbl);
        card.setOnMouseClicked(e -> {
            if (action != null) action.run();
        });
        return card;
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void styleDialog(Dialog<?> dialog) {
        try {
            DialogPane pane = dialog.getDialogPane();
            pane.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #ffffff;");
        } catch (Exception ignored) {}
    }
}
