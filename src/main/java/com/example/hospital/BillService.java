package com.example.hospital;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;

    public BillService(BillRepository billRepository,
                       PatientRepository patientRepository,
                       CurrentUserService currentUserService) {
        this.billRepository = billRepository;
        this.patientRepository = patientRepository;
        this.currentUserService = currentUserService;
    }

    public Bill create(Long patientId, Long appointmentId, BigDecimal amount, String description) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (amount == null || amount.signum() < 0) {
            throw new InvalidOperationException("Invalid bill amount");
        }
        Bill bill = new Bill();
        bill.setPatientId(patientId);
        bill.setAppointmentId(appointmentId);
        bill.setAmount(amount);
        bill.setDescription(description);
        bill.setDate(LocalDate.now());
        bill.setPaymentStatus(Bill.PaymentStatus.PENDING);
        bill = billRepository.save(bill);
        bill.setPatient(patient);
        return bill;
    }

    public List<Bill> getByPatient(Long patientId) {
        currentUserService.requirePatientAccess(patientId);
        return billRepository.findByPatientIdOrderByDateDesc(patientId).stream()
                .map(this::enrich).toList();
    }

    public Bill updateStatus(Long id, String status) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        try {
            bill.setPaymentStatus(Bill.PaymentStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid payment status: " + status);
        }
        return billRepository.save(bill);
    }

    public int count() {
        return (int) billRepository.count();
    }

    private Bill enrich(Bill bill) {
        bill.setPatient(patientRepository.findById(bill.getPatientId()).orElse(null));
        return bill;
    }
}
