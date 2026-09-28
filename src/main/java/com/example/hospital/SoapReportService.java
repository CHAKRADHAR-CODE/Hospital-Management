package com.example.hospital;

import jakarta.jws.WebService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * JAX-WS implementation exposing hospital statistics.
 * NOTE: deliberately excluded from the logging aspect so the bean is NOT
 * proxied by Spring AOP - proxying would hide the @WebService annotation
 * from JAX-WS Endpoint.publish.
 */
@WebService(
        endpointInterface = "com.example.hospital.HospitalReportService",
        targetNamespace = "http://hospital.example.com/ws",
        serviceName = "ReportService")
@Service
public class SoapReportService implements HospitalReportService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ReceptionistRepository receptionistRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final FeedbackRepository feedbackRepository;

    public SoapReportService(PatientRepository patientRepository,
                             DoctorRepository doctorRepository,
                             ReceptionistRepository receptionistRepository,
                             DepartmentRepository departmentRepository,
                             AppointmentRepository appointmentRepository,
                             BillRepository billRepository,
                             FeedbackRepository feedbackRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.receptionistRepository = receptionistRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public String getSummaryReport() {
        return "Hospital Management System Report\n"
                + "================================\n"
                + "Total Patients      : " + patientRepository.count() + "\n"
                + "Total Doctors       : " + doctorRepository.count() + "\n"
                + "Total Receptionists : " + receptionistRepository.count() + "\n"
                + "Total Departments   : " + departmentRepository.count() + "\n"
                + "Total Appointments  : " + appointmentRepository.count() + "\n"
                + "Total Bills         : " + billRepository.count() + "\n"
                + "Total Feedback      : " + feedbackRepository.count();
    }

    @Override
    public String getPatientCount() {
        return String.valueOf(patientRepository.count());
    }

    @Override
    public String getDoctorCount() {
        return String.valueOf(doctorRepository.count());
    }

    @Override
    public String getAppointmentCount() {
        return String.valueOf(appointmentRepository.count());
    }

    @Override
    public String getCompletedAppointments() {
        return String.valueOf(appointmentRepository
                .findByStatusOrderByDateDesc(Appointment.Status.COMPLETED).size());
    }

    @Override
    public String getTotalBillAmount() {
        List<Bill> bills = billRepository.findAll();
        BigDecimal total = bills.stream()
                .map(Bill::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.toPlainString();
    }
}
