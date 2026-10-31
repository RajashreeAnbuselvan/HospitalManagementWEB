package com.hospital.model;

import java.math.BigDecimal;

public class DashboardStats {
    private long patientCount;
    private long doctorCount;
    private long appointmentCount;
    private long unpaidBillsCount;
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;

    public DashboardStats(long patientCount, long doctorCount, long appointmentCount,
                          long unpaidBillsCount, BigDecimal totalRevenue, BigDecimal pendingRevenue) {
        this.patientCount = patientCount;
        this.doctorCount = doctorCount;
        this.appointmentCount = appointmentCount;
        this.unpaidBillsCount = unpaidBillsCount;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.pendingRevenue = pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO;
    }

    public long getPatientCount() { return patientCount; }
    public long getDoctorCount() { return doctorCount; }
    public long getAppointmentCount() { return appointmentCount; }
    public long getUnpaidBillsCount() { return unpaidBillsCount; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getPendingRevenue() { return pendingRevenue; }
}
