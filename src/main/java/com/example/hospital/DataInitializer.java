package com.example.hospital;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Seeds the database with starter data when it is empty.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ReceptionistRepository receptionistRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationReportRepository consultationRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           ReceptionistRepository receptionistRepository,
                           DepartmentRepository departmentRepository,
                           AppointmentRepository appointmentRepository,
                           BillRepository billRepository,
                           PrescriptionRepository prescriptionRepository,
                           ConsultationReportRepository consultationRepository,
                           FeedbackRepository feedbackRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.receptionistRepository = receptionistRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.consultationRepository = consultationRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        seed();
    }

    private void seed() {
        Department cardiology = departmentRepository.save(new Department("Cardiology", "Heart and cardiovascular care"));
        Department neurology = departmentRepository.save(new Department("Neurology", "Brain and nervous system care"));

        User admin = newUser("admin", "admin@cityhospital.com", "Admin@123", Roles.ADMIN);

        User doc1User = newUser("doctor1", "doctor1@cityhospital.com", "Doctor@123", Roles.DOCTOR);
        Doctor doc1 = new Doctor();
        doc1.setUserId(doc1User.getId());
        doc1.setName("Dr. Rajesh Kumar");
        doc1.setSpecialization("Cardiologist");
        doc1.setPhone("9876500001");
        doc1.setQualification("MD, DM (Cardiology)");
        doc1.setYearsOfExperience(12);
        doc1.setBio("Expert in interventional cardiology.");
        doc1.setDepartmentId(cardiology.getId());
        doc1.setAvailability(List.of(
                new Availability("Monday", "9:00 AM", "1:00 PM"),
                new Availability("Wednesday", "10:00 AM", "2:00 PM"),
                new Availability("Friday", "9:00 AM", "4:00 PM")));
        doc1 = doctorRepository.save(doc1);

        User doc2User = newUser("doctor2", "doctor2@cityhospital.com", "Doctor@123", Roles.DOCTOR);
        Doctor doc2 = new Doctor();
        doc2.setUserId(doc2User.getId());
        doc2.setName("Dr. Sarah Lee");
        doc2.setSpecialization("Neurologist");
        doc2.setPhone("9876500002");
        doc2.setQualification("MD, DM (Neurology)");
        doc2.setYearsOfExperience(8);
        doc2.setBio("Specialist in epilepsy and stroke care.");
        doc2.setDepartmentId(neurology.getId());
        doc2.setAvailability(List.of(
                new Availability("Tuesday", "9:00 AM", "1:00 PM"),
                new Availability("Thursday", "9:00 AM", "3:00 PM")));
        doc2 = doctorRepository.save(doc2);

        User rec1User = newUser("receptionist1", "receptionist1@cityhospital.com", "Reception@123", Roles.RECEPTIONIST);
        Receptionist rec1 = new Receptionist();
        rec1.setUserId(rec1User.getId());
        rec1.setName("Emma Watson");
        rec1.setPhone("9876500003");
        receptionistRepository.save(rec1);

        User rec2User = newUser("receptionist2", "receptionist2@cityhospital.com", "Reception@123", Roles.RECEPTIONIST);
        Receptionist rec2 = new Receptionist();
        rec2.setUserId(rec2User.getId());
        rec2.setName("John Smith");
        rec2.setPhone("9876500004");
        receptionistRepository.save(rec2);

        Patient p1 = createPatient("patient1", "patient1@cityhospital.com", "David Miller",
                34, "Male", "O+", "9876500005", "12 Park Avenue");
        Patient p2 = createPatient("patient2", "patient2@cityhospital.com", "Priya Sharma",
                28, "Female", "B+", "9876500006", "45 Lake View Road");
        Patient p3 = createPatient("patient3", "patient3@cityhospital.com", "Michael Brown",
                52, "Male", "A-", "9876500007", "89 Bridge Street");

        Appointment a1 = newAppointment(p1.getId(), doc1.getId(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Chest pain consultation", Appointment.Status.SCHEDULED);
        Appointment a2 = newAppointment(p2.getId(), doc2.getId(), LocalDate.now().minusDays(2), LocalTime.of(11, 30), "Migraine follow-up", Appointment.Status.COMPLETED);
        Appointment a3 = newAppointment(p3.getId(), doc1.getId(), LocalDate.now(), LocalTime.of(9, 30), "Annual heart check-up", Appointment.Status.SCHEDULED);

        Bill bill = new Bill();
        bill.setPatientId(p2.getId());
        bill.setAppointmentId(a2.getId());
        bill.setAmount(new BigDecimal("1500.00"));
        bill.setDescription("Neurology consultation");
        bill.setDate(LocalDate.now().minusDays(2));
        bill.setPaymentStatus(Bill.PaymentStatus.PENDING);
        billRepository.save(bill);

        Prescription rx = new Prescription();
        rx.setPatientId(p2.getId());
        rx.setDoctorId(doc2.getId());
        rx.setDate(LocalDate.now().minusDays(2));
        rx.setMedicine("Sumatriptan 50mg");
        rx.setDosage("1 tablet");
        rx.setFrequency("Twice daily");
        rx.setDuration("5 days");
        rx.setInstructions("Take after food");
        prescriptionRepository.save(rx);

        ConsultationReport con = new ConsultationReport();
        con.setPatientId(p2.getId());
        con.setDoctorId(doc2.getId());
        con.setDate(LocalDate.now().minusDays(2));
        con.setDiagnosis("Chronic migraine");
        con.setConsultationDetails("Reported frequent severe headaches over 3 months.");
        con.setTreatment("Prescribed Sumatriptan and lifestyle changes.");
        con.setRecommendations("Maintain hydration and sleep schedule.");
        consultationRepository.save(con);

        Feedback fb = new Feedback();
        fb.setPatientId(p2.getId());
        fb.setDoctorId(doc2.getId());
        fb.setAppointmentId(a2.getId());
        fb.setRating(5);
        fb.setComment("Very caring and professional doctor.");
        fb.setDate(LocalDate.now());
        feedbackRepository.save(fb);
    }

    private User newUser(String username, String email, String rawPassword, String role) {
        return userRepository.save(new User(username, email, passwordEncoder.encode(rawPassword), role));
    }

    private Patient createPatient(String username, String email, String name, int age,
                                  String gender, String bloodGroup, String phone, String address) {
        User user = newUser(username, email, "Patient@123", Roles.PATIENT);
        Patient patient = new Patient();
        patient.setUserId(user.getId());
        patient.setName(name);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setBloodGroup(bloodGroup);
        patient.setPhone(phone);
        patient.setAddress(address);
        patient.setRegistrationDate(LocalDate.now());
        return patientRepository.save(patient);
    }

    private Appointment newAppointment(Long patientId, Long doctorId, LocalDate date,
                                       LocalTime time, String reason, Appointment.Status status) {
        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setDoctorId(doctorId);
        a.setDate(date);
        a.setTime(time);
        a.setReason(reason);
        a.setStatus(status);
        return appointmentRepository.save(a);
    }
}
