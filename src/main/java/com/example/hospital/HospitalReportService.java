package com.example.hospital;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

/**
 * SOAP (JAX-WS) service contract exposed for system reporting.
 */
@WebService(name = "ReportService", targetNamespace = "http://hospital.example.com/ws")
public interface HospitalReportService {

    @WebMethod
    String getSummaryReport();

    @WebMethod
    String getPatientCount();

    @WebMethod
    String getDoctorCount();

    @WebMethod
    String getAppointmentCount();

    @WebMethod
    String getCompletedAppointments();

    @WebMethod
    String getTotalBillAmount();
}
