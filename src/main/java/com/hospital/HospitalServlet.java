package com.hospital;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(urlPatterns = {"/"})
public class HospitalServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String page = value(req.getParameter("page"), "dashboard");
        String q = value(req.getParameter("q"), "").trim();
        try (Connection c = Database.getConnection()) {
            req.setAttribute("page", page);
            req.setAttribute("q", q);
            req.setAttribute("patients", rows(c, "SELECT * FROM patients WHERE LOWER(name) LIKE ? OR LOWER(COALESCE(phone,'')) LIKE ? ORDER BY id DESC", "%" + q.toLowerCase() + "%"));
            req.setAttribute("doctors", rows(c, "SELECT * FROM doctors WHERE LOWER(name) LIKE ? OR LOWER(COALESCE(specialization,'')) LIKE ? ORDER BY id DESC", "%" + q.toLowerCase() + "%"));
            req.setAttribute("appointments", rows(c, "SELECT a.*, p.name patient_name, d.name doctor_name FROM appointments a LEFT JOIN patients p ON p.id=a.patient_id LEFT JOIN doctors d ON d.id=a.doctor_id ORDER BY a.appointment_date DESC, a.id DESC"));
            req.setAttribute("prescriptions", rows(c, "SELECT pr.*, p.name patient_name, d.name doctor_name FROM prescriptions pr LEFT JOIN appointments a ON a.id=pr.appointment_id LEFT JOIN patients p ON p.id=a.patient_id LEFT JOIN doctors d ON d.id=a.doctor_id ORDER BY pr.id DESC"));
            req.setAttribute("bills", rows(c, "SELECT b.*, p.name patient_name FROM bills b LEFT JOIN patients p ON p.id=b.patient_id ORDER BY b.id DESC"));
            req.setAttribute("patientCount", scalar(c, "SELECT COUNT(*) FROM patients"));
            req.setAttribute("doctorCount", scalar(c, "SELECT COUNT(*) FROM doctors"));
            req.setAttribute("appointmentCount", scalar(c, "SELECT COUNT(*) FROM appointments"));
            req.setAttribute("unpaidCount", scalar(c, "SELECT COUNT(*) FROM bills WHERE paid=FALSE"));
            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
        } catch (SQLException e) { throw new ServletException("Database error", e); }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        try (Connection c = Database.getConnection()) {
            switch (value(action, "")) {
                case "addPatient" -> { try (PreparedStatement p=c.prepareStatement("INSERT INTO patients(name,age,gender,phone,email,medical_history) VALUES(?,?,?,?,?,?)")) {
                    p.setString(1, req.getParameter("name")); p.setInt(2, integer(req.getParameter("age"))); p.setString(3, req.getParameter("gender")); p.setString(4, req.getParameter("phone")); p.setString(5, req.getParameter("email")); p.setString(6, req.getParameter("medical_history")); p.executeUpdate(); } }
                case "addDoctor" -> { try (PreparedStatement p=c.prepareStatement("INSERT INTO doctors(name,specialization,phone,email) VALUES(?,?,?,?)")) {
                    p.setString(1, req.getParameter("name")); p.setString(2, req.getParameter("specialization")); p.setString(3, req.getParameter("phone")); p.setString(4, req.getParameter("email")); p.executeUpdate(); } }
                case "addAppointment" -> { try (PreparedStatement p=c.prepareStatement("INSERT INTO appointments(patient_id,doctor_id,appointment_date,appointment_time,reason,status) VALUES(?,?,?,?,?,'Scheduled')")) {
                    p.setLong(1, longValue(req.getParameter("patient_id"))); p.setLong(2, longValue(req.getParameter("doctor_id"))); p.setDate(3, java.sql.Date.valueOf(req.getParameter("appointment_date"))); p.setString(4, req.getParameter("appointment_time")); p.setString(5, req.getParameter("reason")); p.executeUpdate(); } }
                case "addPrescription" -> { try (PreparedStatement p=c.prepareStatement("INSERT INTO prescriptions(appointment_id,diagnosis,medicines,notes) VALUES(?,?,?,?)")) {
                    p.setLong(1, longValue(req.getParameter("appointment_id"))); p.setString(2, req.getParameter("diagnosis")); p.setString(3, req.getParameter("medicines")); p.setString(4, req.getParameter("notes")); p.executeUpdate(); } }
                case "addBill" -> { try (PreparedStatement p=c.prepareStatement("INSERT INTO bills(patient_id,appointment_id,amount,description,paid) VALUES(?,?,?,?,FALSE)")) {
                    p.setLong(1, longValue(req.getParameter("patient_id"))); p.setLong(2, longValue(req.getParameter("appointment_id"))); p.setBigDecimal(3, new java.math.BigDecimal(value(req.getParameter("amount"),"0"))); p.setString(4, req.getParameter("description")); p.executeUpdate(); } }
                case "payBill" -> { try (PreparedStatement p=c.prepareStatement("UPDATE bills SET paid=TRUE WHERE id=?")) { p.setLong(1, longValue(req.getParameter("id"))); p.executeUpdate(); } }
                case "deletePatient" -> { try (PreparedStatement p=c.prepareStatement("DELETE FROM patients WHERE id=?")) { p.setLong(1, longValue(req.getParameter("id"))); p.executeUpdate(); } }
                case "deleteDoctor" -> { try (PreparedStatement p=c.prepareStatement("DELETE FROM doctors WHERE id=?")) { p.setLong(1, longValue(req.getParameter("id"))); p.executeUpdate(); } }
            }
            resp.sendRedirect(req.getContextPath() + "/?page=" + url(value(req.getParameter("returnPage"), "dashboard")));
        } catch (Exception e) {
            throw new ServletException("Could not save record. Check that required fields are filled and the selected IDs exist.", e);
        }
    }
    private static List<Map<String,Object>> rows(Connection c, String sql) throws SQLException {
        return rows(c, sql, "");
    }
    private static List<Map<String,Object>> rows(Connection c, String sql, String q) throws SQLException {
        List<Map<String,Object>> list=new ArrayList<>();
        try (PreparedStatement p=c.prepareStatement(sql)) { if (sql.indexOf("?") >= 0) { p.setString(1,q); p.setString(2,q); } try(ResultSet r=p.executeQuery()) {
            ResultSetMetaData m=r.getMetaData(); while(r.next()){Map<String,Object> row=new LinkedHashMap<>(); for(int i=1;i<=m.getColumnCount();i++) row.put(m.getColumnLabel(i).toLowerCase(),r.getObject(i)); list.add(row);}
        }} return list;
    }
    private static long scalar(Connection c,String sql)throws SQLException{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getLong(1);}}
    private static String value(String s,String d){return s==null?d:s;}
    private static int integer(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}}
    private static long longValue(String s){return Long.parseLong(s);}
    private static String url(String s){return s.replaceAll("[^a-zA-Z0-9_-]","");}
}