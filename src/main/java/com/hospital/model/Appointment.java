package com.hospital.model;

import java.time.LocalDate;

public class Appointment {
    private long id;
    private long patientId;
    private String patientName;
    private long doctorId;
    private String doctorName;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String reason;
    private String status;

    public Appointment() {}

    public Appointment(long id, long patientId, String patientName, long doctorId, String doctorName,
                       LocalDate appointmentDate, String appointmentTime, String reason, String status) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public long getDoctorId() { return doctorId; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "#" + id + " - " + (patientName != null ? patientName : "Patient #" + patientId) +
               " with " + (doctorName != null ? doctorName : "Doctor #" + doctorId) +
               " (" + (appointmentDate != null ? appointmentDate.toString() : "") + " " + (appointmentTime != null ? appointmentTime : "") + ")";
    }
}
