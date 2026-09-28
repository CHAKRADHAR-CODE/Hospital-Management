package com.example.hospital;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReceptionistRepository receptionistRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final FeedbackRepository feedbackRepository;

    public DashboardService(UserRepository userRepository,
                            DepartmentRepository departmentRepository,
                            DoctorRepository doctorRepository,
                            PatientRepository patientRepository,
                            ReceptionistRepository receptionistRepository,
                            AppointmentRepository appointmentRepository,
                            BillRepository billRepository,
                            FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.receptionistRepository = receptionistRepository;
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalPatients", patientRepository.count());
        m.put("totalDoctors", doctorRepository.count());
        m.put("totalReceptionists", receptionistRepository.count());
        m.put("totalDepartments", departmentRepository.count());
        m.put("totalAppointments", appointmentRepository.count());
        m.put("completedAppointments", appointmentRepository.findByStatusOrderByDateDesc(Appointment.Status.COMPLETED).size());
        m.put("scheduledAppointments", appointmentRepository.findByStatusOrderByDateDesc(Appointment.Status.SCHEDULED).size());
        m.put("totalBills", billRepository.count());
        return m;
    }

    public Map<String, Object> reports() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("patients", patientRepository.count());
        m.put("doctors", doctorRepository.count());
        m.put("receptionists", receptionistRepository.count());
        m.put("departments", departmentRepository.count());
        m.put("appointments", appointmentRepository.count());
        m.put("bills", billRepository.count());
        m.put("feedback", feedbackRepository.count());
        m.put("users", userRepository.count());
        return m;
    }
}
