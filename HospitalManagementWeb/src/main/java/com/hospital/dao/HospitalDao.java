package com.hospital.dao;

import com.hospital.Database;
import com.hospital.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HospitalDao {

    // --- Dashboard ---
    public DashboardStats getDashboardStats() {
        long patients = 0;
        long doctors = 0;
        long appointments = 0;
        long unpaidBills = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal pendingRevenue = BigDecimal.ZERO;

        try (Connection c = Database.getConnection(); Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM patients")) {
                if (rs.next()) patients = rs.getLong(1);
            }
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM doctors")) {
                if (rs.next()) doctors = rs.getLong(1);
            }
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM appointments")) {
                if (rs.next()) appointments = rs.getLong(1);
            }
            try (ResultSet rs = s.executeQuery("SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM bills WHERE paid = FALSE")) {
                if (rs.next()) {
                    unpaidBills = rs.getLong(1);
                    pendingRevenue = rs.getBigDecimal(2);
                }
            }
            try (ResultSet rs = s.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM bills WHERE paid = TRUE")) {
                if (rs.next()) {
                    totalRevenue = rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DashboardStats(patients, doctors, appointments, unpaidBills, totalRevenue, pendingRevenue);
    }

    // --- Patients ---
    public List<Patient> getPatients(String query) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE LOWER(name) LIKE ? OR LOWER(COALESCE(phone,'')) LIKE ? OR LOWER(COALESCE(email,'')) LIKE ? OR LOWER(COALESCE(medical_history,'')) LIKE ? ORDER BY id DESC";
        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Patient(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("medical_history")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean savePatient(Patient p) {
        if (p.getId() <= 0) {
            String sql = "INSERT INTO patients(name, age, gender, phone, email, medical_history) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getName());
                ps.setInt(2, p.getAge());
                ps.setString(3, p.getGender());
                ps.setString(4, p.getPhone());
                ps.setString(5, p.getEmail());
                ps.setString(6, p.getMedicalHistory());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) p.setId(rs.getLong(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE patients SET name=?, age=?, gender=?, phone=?, email=?, medical_history=? WHERE id=?";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getName());
                ps.setInt(2, p.getAge());
                ps.setString(3, p.getGender());
                ps.setString(4, p.getPhone());
                ps.setString(5, p.getEmail());
                ps.setString(6, p.getMedicalHistory());
                ps.setLong(7, p.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean deletePatient(long id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Doctors ---
    public List<Doctor> getDoctors(String query, String specFilter) {
        List<Doctor> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM doctors WHERE (LOWER(name) LIKE ? OR LOWER(COALESCE(phone,'')) LIKE ? OR LOWER(COALESCE(email,'')) LIKE ?)");
        if (specFilter != null && !specFilter.equalsIgnoreCase("All Specializations") && !specFilter.isBlank()) {
            sql.append(" AND LOWER(COALESCE(specialization,'')) = ?");
        }
        sql.append(" ORDER BY id DESC");

        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            if (specFilter != null && !specFilter.equalsIgnoreCase("All Specializations") && !specFilter.isBlank()) {
                ps.setString(4, specFilter.trim().toLowerCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Doctor(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("specialization"),
                            rs.getString("phone"),
                            rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getDoctorSpecializations() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT specialization FROM doctors WHERE specialization IS NOT NULL AND TRIM(specialization) != '' ORDER BY specialization";
        try (Connection c = Database.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveDoctor(Doctor d) {
        if (d.getId() <= 0) {
            String sql = "INSERT INTO doctors(name, specialization, phone, email) VALUES (?, ?, ?, ?)";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, d.getName());
                ps.setString(2, d.getSpecialization());
                ps.setString(3, d.getPhone());
                ps.setString(4, d.getEmail());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) d.setId(rs.getLong(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE doctors SET name=?, specialization=?, phone=?, email=? WHERE id=?";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, d.getName());
                ps.setString(2, d.getSpecialization());
                ps.setString(3, d.getPhone());
                ps.setString(4, d.getEmail());
                ps.setLong(5, d.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean deleteDoctor(long id) {
        String sql = "DELETE FROM doctors WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Appointments ---
    public List<Appointment> getAppointments(String query, String statusFilter, LocalDate dateFilter) {
        List<Appointment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.*, p.name AS patient_name, d.name AS doctor_name " +
                "FROM appointments a " +
                "LEFT JOIN patients p ON p.id = a.patient_id " +
                "LEFT JOIN doctors d ON d.id = a.doctor_id " +
                "WHERE (LOWER(COALESCE(p.name,'')) LIKE ? OR LOWER(COALESCE(d.name,'')) LIKE ? OR LOWER(COALESCE(a.reason,'')) LIKE ?)");

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Statuses") && !statusFilter.isBlank()) {
            sql.append(" AND a.status = ?");
        }
        if (dateFilter != null) {
            sql.append(" AND a.appointment_date = ?");
        }
        sql.append(" ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC");

        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, pattern);
            ps.setString(idx++, pattern);
            ps.setString(idx++, pattern);
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Statuses") && !statusFilter.isBlank()) {
                ps.setString(idx++, statusFilter.trim());
            }
            if (dateFilter != null) {
                ps.setDate(idx++, Date.valueOf(dateFilter));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("appointment_date");
                    list.add(new Appointment(
                            rs.getLong("id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            d != null ? d.toLocalDate() : null,
                            rs.getString("appointment_time"),
                            rs.getString("reason"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> getRecentAppointments(int limit) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name " +
                "FROM appointments a " +
                "LEFT JOIN patients p ON p.id = a.patient_id " +
                "LEFT JOIN doctors d ON d.id = a.doctor_id " +
                "ORDER BY a.appointment_date DESC, a.id DESC LIMIT ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("appointment_date");
                    list.add(new Appointment(
                            rs.getLong("id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getLong("doctor_id"),
                            rs.getString("doctor_name"),
                            d != null ? d.toLocalDate() : null,
                            rs.getString("appointment_time"),
                            rs.getString("reason"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveAppointment(Appointment a) {
        if (a.getId() <= 0) {
            String sql = "INSERT INTO appointments(patient_id, doctor_id, appointment_date, appointment_time, reason, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, a.getPatientId());
                ps.setLong(2, a.getDoctorId());
                ps.setDate(3, a.getAppointmentDate() != null ? Date.valueOf(a.getAppointmentDate()) : Date.valueOf(LocalDate.now()));
                ps.setString(4, a.getAppointmentTime());
                ps.setString(5, a.getReason());
                ps.setString(6, a.getStatus() != null ? a.getStatus() : "Scheduled");
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) a.setId(rs.getLong(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE appointments SET patient_id=?, doctor_id=?, appointment_date=?, appointment_time=?, reason=?, status=? WHERE id=?";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, a.getPatientId());
                ps.setLong(2, a.getDoctorId());
                ps.setDate(3, a.getAppointmentDate() != null ? Date.valueOf(a.getAppointmentDate()) : Date.valueOf(LocalDate.now()));
                ps.setString(4, a.getAppointmentTime());
                ps.setString(5, a.getReason());
                ps.setString(6, a.getStatus());
                ps.setLong(7, a.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean updateAppointmentStatus(long id, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAppointment(long id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Prescriptions ---
    public List<Prescription> getPrescriptions(String query) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT pr.*, p.name AS patient_name, d.name AS doctor_name " +
                "FROM prescriptions pr " +
                "LEFT JOIN appointments a ON a.id = pr.appointment_id " +
                "LEFT JOIN patients p ON p.id = a.patient_id " +
                "LEFT JOIN doctors d ON d.id = a.doctor_id " +
                "WHERE (LOWER(COALESCE(p.name,'')) LIKE ? OR LOWER(COALESCE(d.name,'')) LIKE ? OR LOWER(COALESCE(pr.diagnosis,'')) LIKE ? OR LOWER(COALESCE(pr.medicines,'')) LIKE ?) " +
                "ORDER BY pr.id DESC";

        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Prescription(
                            rs.getLong("id"),
                            rs.getLong("appointment_id"),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("diagnosis"),
                            rs.getString("medicines"),
                            rs.getString("notes"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean savePrescription(Prescription pr) {
        String sql = "INSERT INTO prescriptions(appointment_id, diagnosis, medicines, notes) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pr.getAppointmentId());
            ps.setString(2, pr.getDiagnosis());
            ps.setString(3, pr.getMedicines());
            ps.setString(4, pr.getNotes());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) pr.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletePrescription(long id) {
        String sql = "DELETE FROM prescriptions WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Bills ---
    public List<Bill> getBills(String query, String statusFilter) {
        List<Bill> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT b.*, p.name AS patient_name " +
                "FROM bills b " +
                "LEFT JOIN patients p ON p.id = b.patient_id " +
                "WHERE (LOWER(COALESCE(p.name,'')) LIKE ? OR LOWER(COALESCE(b.description,'')) LIKE ?)");

        if ("Paid".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND b.paid = TRUE");
        } else if ("Unpaid".equalsIgnoreCase(statusFilter) || "Pending".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND b.paid = FALSE");
        }
        sql.append(" ORDER BY b.id DESC");

        String pattern = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";

        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Bill(
                            rs.getLong("id"),
                            rs.getLong("patient_id"),
                            rs.getString("patient_name"),
                            rs.getObject("appointment_id") != null ? rs.getLong("appointment_id") : null,
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getBoolean("paid"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveBill(Bill b) {
        String sql = "INSERT INTO bills(patient_id, appointment_id, amount, description, paid) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, b.getPatientId());
            if (b.getAppointmentId() != null) {
                ps.setLong(2, b.getAppointmentId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setBigDecimal(3, b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO);
            ps.setString(4, b.getDescription());
            ps.setBoolean(5, b.isPaid());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) b.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markBillPaid(long id) {
        String sql = "UPDATE bills SET paid = TRUE WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBill(long id) {
        String sql = "DELETE FROM bills WHERE id = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
