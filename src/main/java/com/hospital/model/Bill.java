package com.hospital.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Bill {
    private long id;
    private long patientId;
    private String patientName;
    private Long appointmentId;
    private BigDecimal amount;
    private String description;
    private boolean paid;
    private Timestamp createdAt;

    public Bill() {}

    public Bill(long id, long patientId, String patientName, Long appointmentId,
                BigDecimal amount, String description, boolean paid, Timestamp createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.description = description;
        this.paid = paid;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
