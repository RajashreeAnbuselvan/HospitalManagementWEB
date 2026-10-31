# CarePoint Hospital Management System — JavaFX Desktop & Web

A modern, high-performance Hospital Management desktop application built with **JavaFX 21**, **Java 21**, and an embedded **H2 Database**.

---

## 🚀 Features

- **Dashboard Intelligence**: Live counts of patients, registered medical specialists, consultations, pending bills, and real-time revenue summaries.
- **Patient Directory**: Register, view, search, edit, and manage patient medical records, contact numbers, and histories.
- **Medical Specialists**: Manage doctors by specialization (Cardiology, General Medicine, Pediatrics, Orthopedics, Neurology), track contact details.
- **Appointment Scheduling**: Interactive appointment booking with patient and doctor selection dropdowns, time slot pickers, and statuses (`Scheduled`, `Completed`, `Cancelled`).
- **Prescriptions & Diagnostics**: Create detailed clinical diagnoses and medication regimens; generate and view printable **Medical Prescription (Rx) Slips** with official hospital branding.
- **Billing & Finance**: Generate itemized patient bills, track paid vs pending invoices, instant **Mark Paid** action, and view/print **Official Tax Invoice Receipts**.
- **Embedded Database**: Local file-based H2 database saved under `~/.hospital-management/hospitaldb` (requires zero manual setup).
- **Modern Theme**: Sleek medical dark/light responsive interface with customized buttons, KPI cards, and status badge pills.

---

## 🛠️ How to Run the JavaFX Application

### Option 1: Double Click (Fastest)
Double-click `run.bat` in the root folder or in `HospitalManagementWeb/run.bat`.

### Option 2: Command Line (Maven Wrapper)
From the project folder:
```powershell
.\mvnw.cmd javafx:run
```

Or from the root directory:
```powershell
.\mvnw.cmd -f HospitalManagementWeb/pom.xml javafx:run
```

---

## 📦 Project Structure

```
HospitalManagementWeb/
├── mvnw.cmd                     # Maven wrapper script
├── run.bat                      # 1-click application runner
├── pom.xml                      # JavaFX 21 + H2 + Compiler config
├── src/main/java/com/hospital/
│   ├── MainApp.java             # JavaFX Application entrypoint
│   ├── MainLauncher.java        # Main launcher wrapper
│   ├── Database.java            # H2 Database connection & auto-migration
│   ├── dao/
│   │   └── HospitalDao.java     # JDBC operations for all entities
│   ├── model/
│   │   ├── Patient.java         # Patient model
│   │   ├── Doctor.java          # Doctor model
│   │   ├── Appointment.java     # Appointment model
│   │   ├── Prescription.java    # Prescription model
│   │   ├── Bill.java            # Billing invoice model
│   │   └── DashboardStats.java  # Dashboard KPI metrics model
│   └── ui/
│       ├── MainLayout.java      # Sidebar navigation & view switcher
│       ├── DashboardView.java   # KPI cards, quick actions & recent tables
│       ├── PatientsView.java    # Patient search, register & edit
│       ├── DoctorsView.java     # Doctor directory & specialization filters
│       ├── AppointmentsView.java# Appointment schedule & booking modal
│       ├── PrescriptionsView.java# Rx registry & printable prescription slip
│       ├── BillingView.java     # Invoice tracker & printable receipt
│       └── UIUtils.java         # Reusable UI cards, dialogs & badges
└── src/main/resources/styles/
    └── app.css                  # Custom CarePoint modern medical stylesheet
```
