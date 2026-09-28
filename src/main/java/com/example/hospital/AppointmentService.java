package com.example.hospital;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserService currentUserService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              CurrentUserService currentUserService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentUserService = currentUserService;
    }

    public List<Map<String, Object>> getAll() {
        return appointmentRepository.findAllByOrderByDateDesc().stream().map(this::toView).toList();
    }

    public List<Map<String, Object>> getByPatient(Long patientId) {
        currentUserService.requirePatientAccess(patientId);
        return appointmentRepository.findByPatientIdOrderByDateDesc(patientId).stream()
                .map(this::toView).toList();
    }

    public List<Map<String, Object>> getByDoctor(Long doctorId) {
        Doctor current = currentUserService.currentDoctor();
        if (current != null && !current.getId().equals(doctorId)) {
            throw new AccessDeniedException("You can only view your own appointments");
        }
        return appointmentRepository.findByDoctorIdOrderByDateDesc(doctorId).stream()
                .map(this::toView).toList();
    }

    @Transactional
    public Map<String, Object> create(Long patientId, Long doctorId, String dateStr,
                                      String timeStr, String reason) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateStr);
            time = LocalTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            throw new InvalidOperationException("Invalid date or time format");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Appointment date cannot be in the past.");
        }

        checkDoctorAvailability(doctor, date, time);

        if (appointmentRepository.existsByDoctorIdAndDateAndTime(doctorId, date, time)) {
            throw new DuplicateResourceException("Doctor already has an appointment at this time");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason(reason);
        appointment.setStatus(Appointment.Status.SCHEDULED);
        return toView(appointmentRepository.save(appointment));
    }

    private void checkDoctorAvailability(Doctor doctor, LocalDate date, LocalTime time) {
        List<Availability> availability = doctor.getAvailability();
        if (availability == null || availability.isEmpty()) {
            return;
        }
        String day = TimeUtil.dayName(date);
        int apptMin = TimeUtil.toMinutes(time);
        boolean found = availability.stream().anyMatch(slot -> {
            if (slot == null) return false;
            String slotDay = TimeUtil.normalizeDay(slot.getDay());
            if (!slotDay.equals(day)) return false;
            int start = TimeUtil.toMinutes(slot.getStartTime());
            int end = TimeUtil.toMinutes(slot.getEndTime());
            if (start < 0 || end < 0) return false;
            return apptMin >= start && apptMin < end;
        });
        if (!found) {
            throw new InvalidOperationException(
                    "Doctor is unavailable on " + date.getDayOfWeek().name().toLowerCase()
                            + " at " + time + ". Please check their availability.");
        }
    }

    public Map<String, Object> complete(Long id) {
        Appointment appointment = getAppointment(id);
        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new InvalidOperationException("Appointment is already completed");
        }
        appointment.setStatus(Appointment.Status.COMPLETED);
        return toView(appointmentRepository.save(appointment));
    }

    public Map<String, Object> cancel(Long id) {
        Appointment appointment = getAppointment(id);
        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed appointment");
        }
        appointment.setStatus(Appointment.Status.CANCELLED);
        return toView(appointmentRepository.save(appointment));
    }

    public List<Appointment> appointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    public int count() {
        return (int) appointmentRepository.count();
    }

    public int countByStatus(Appointment.Status status) {
        return appointmentRepository.findByStatusOrderByDateDesc(status).size();
    }

    public Map<String, Object> toView(Appointment appointment) {
        Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
        return appointment.toView(patient, doctor);
    }
}
