package com.hospital;

import com.hospital.ui.MainLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainLayout mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout, 1240, 800);

        // Load stylesheet
        try {
            String css = getClass().getResource("/styles/app.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Could not load app.css: " + e.getMessage());
        }

        primaryStage.setTitle("CarePoint — Hospital Management System");
        primaryStage.setMinWidth(1020);
        primaryStage.setMinHeight(680);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
