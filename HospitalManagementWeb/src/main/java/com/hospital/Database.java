package com.hospital;

import java.nio.file.*;
import java.sql.*;

public final class Database {
    private static final String URL;
    static {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".hospital-management");
            Files.createDirectories(dir);
            URL = "jdbc:h2:file:" + dir.resolve("hospitaldb").toAbsolutePath() + ";AUTO_SERVER=TRUE";
            Class.forName("org.h2.Driver");
            initialize();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    private static void initialize() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS patients(" +
                    "id IDENTITY PRIMARY KEY, " +
                    "name VARCHAR(120) NOT NULL, " +
                    "age INT, " +
                    "gender VARCHAR(20), " +
                    "phone VARCHAR(30), " +
                    "email VARCHAR(120), " +
                    "medical_history VARCHAR(1000))");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS doctors(" +
                    "id IDENTITY PRIMARY KEY, " +
                    "name VARCHAR(120) NOT NULL, " +
                    "specialization VARCHAR(100), " +
                    "phone VARCHAR(30), " +
                    "email VARCHAR(120))");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS appointments(" +
                    "id IDENTITY PRIMARY KEY, " +
                    "patient_id BIGINT, " +
                    "doctor_id BIGINT, " +
                    "appointment_date DATE, " +
                    "appointment_time VARCHAR(10), " +
                    "reason VARCHAR(500), " +
                    "status VARCHAR(30) DEFAULT 'Scheduled')");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS prescriptions(" +
                    "id IDENTITY PRIMARY KEY, " +
                    "appointment_id BIGINT, " +
                    "diagnosis VARCHAR(1000), " +
                    "medicines VARCHAR(1000), " +
                    "notes VARCHAR(1000), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS bills(" +
                    "id IDENTITY PRIMARY KEY, " +
                    "patient_id BIGINT, " +
                    "appointment_id BIGINT, " +
                    "amount DECIMAL(10,2), " +
                    "description VARCHAR(500), " +
                    "paid BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Seed doctors
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM doctors")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    s.executeUpdate("INSERT INTO doctors(name,specialization,phone,email) VALUES " +
                            "('Dr. Meera Kumar', 'Cardiology', '+91 98765 43210', 'meera.kumar@carepoint.org')," +
                            "('Dr. Arjun Raj', 'General Medicine', '+91 98765 43211', 'arjun.raj@carepoint.org')," +
                            "('Dr. Sneha Nair', 'Pediatrics', '+91 98765 43212', 'sneha.nair@carepoint.org')," +
                            "('Dr. Rajesh Gupta', 'Orthopedics', '+91 98765 43213', 'rajesh.gupta@carepoint.org')," +
                            "('Dr. Ananya Roy', 'Neurology', '+91 98765 43214', 'ananya.roy@carepoint.org')");
                }
            }

            // Seed patients
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM patients")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    s.executeUpdate("INSERT INTO patients(name,age,gender,phone,email,medical_history) VALUES " +
                            "('Rohan Sharma', 34, 'Male', '+91 98123 45678', 'rohan.sharma@gmail.test', 'Hypertension Stage 1, Penicillin allergy')," +
                            "('Priya Patel', 29, 'Female', '+91 98234 56789', 'priya.patel@outlook.test', 'Mild seasonal asthma, no regular meds')," +
                            "('Amit Verma', 52, 'Male', '+91 98345 67890', 'amit.verma@yahoo.test', 'Type-2 Diabetes on Metformin, regular follow-up')," +
                            "('Sunita Sen', 45, 'Female', '+91 98456 78901', 'sunita.sen@mail.test', 'Chronic migraine aura, migraine prophylactic')," +
                            "('Vikram Malhotra', 61, 'Male', '+91 98567 89012', 'vikram.m@domain.test', 'Post-CABG cardiac rehab, aspirin daily')");
                }
            }

            // Seed appointments
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM appointments")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    s.executeUpdate("INSERT INTO appointments(patient_id,doctor_id,appointment_date,appointment_time,reason,status) VALUES " +
                            "(1, 1, CURRENT_DATE, '09:30', 'Routine Cardiac Checkup & BP Review', 'Scheduled')," +
                            "(2, 2, CURRENT_DATE, '11:00', 'Persistent dry cough and seasonal allergies', 'Scheduled')," +
                            "(3, 2, CURRENT_DATE - 1, '14:30', 'HbA1c quarterly review & diet consultation', 'Completed')," +
                            "(4, 5, CURRENT_DATE + 1, '10:15', 'Frequent migraine episodes and visual aura', 'Scheduled')," +
                            "(5, 4, CURRENT_DATE - 3, '16:00', 'Right knee arthroscopy follow-up examination', 'Completed')");
                }
            }

            // Seed prescriptions
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM prescriptions")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    s.executeUpdate("INSERT INTO prescriptions(appointment_id,diagnosis,medicines,notes) VALUES " +
                            "(3, 'Type-2 Diabetes Mellitus with satisfactory control', '1. Tab. Metformin 500mg (1-0-1 after food)\n2. Tab. Glimepiride 1mg (1-0-0 morning)\n3. Vitamin B12 supplements', 'Advised 30 mins brisk walk daily and low glycemic index diet. Repeat HbA1c in 90 days.')," +
                            "(5, 'Post-operative right knee recovery, mild joint stiffness', '1. Tab. Aceclofenac 100mg + Paracetamol 325mg (SOS)\n2. Glucosamine sulfate 500mg daily', 'Physiotherapy knee exercises 3x daily. Avoid heavy weight lifting.')");
                }
            }

            // Seed bills
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM bills")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    s.executeUpdate("INSERT INTO bills(patient_id,appointment_id,amount,description,paid) VALUES " +
                            "(1, 1, 1500.00, 'Cardiology Consultation & ECG Report', FALSE)," +
                            "(2, 2, 800.00, 'General Medicine Consultation & Allergy Test', FALSE)," +
                            "(3, 3, 2450.00, 'Comprehensive Diabetic Profile & Consultation', TRUE)," +
                            "(4, 4, 1800.00, 'Neurology Consultation & EEG Screening', FALSE)," +
                            "(5, 5, 3500.00, 'Orthopedic Followup & Knee X-Ray Digital', TRUE)");
                }
            }
        }
    }
}