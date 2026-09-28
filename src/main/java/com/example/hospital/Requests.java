package com.example.hospital;

import java.math.BigDecimal;

/**
 * Request DTOs matching the frontend payload shapes.
 * IDs arrive from the browser as strings; they are parsed to Long IDs
 * (primary keys) by the controllers/service layer.
 */
public final class Requests {

    private Requests() {
    }

    public static class IdRef {
        public String id;
    }

    public static class AppointmentCreate {
        public IdRef patient;
        public IdRef doctor;
        public String date;
        public String time;
        public String reason;
    }

    public static class BillCreate {
        public IdRef patient;
        public IdRef appointment;
        public BigDecimal amount;
        public String description;
    }

    public static class FeedbackCreate {
        public IdRef doctor;
        public Integer rating;
        public String comment;
    }

    public static class DoctorCreate {
        public String username;
        public String email;
        public String departmentId;
        public Doctor doctor;
    }

    public static class ReceptionistCreate {
        public String username;
        public String email;
        public Receptionist receptionist;
    }

    public static class PatientCreate {
        public String username;
        public String email;
        public Patient patient;
    }
}
