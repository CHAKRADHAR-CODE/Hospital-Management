package com.example.hospital;

import jakarta.xml.ws.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Publishes the JAX-WS endpoint on a dedicated port (8181) so it does not
 * conflict with the embedded Tomcat web server running on 8080.
 */
@Configuration
public class SoapServerConfig {

    private static final Logger log = LoggerFactory.getLogger(SoapServerConfig.class);

    private final HospitalReportService reportService;

    public SoapServerConfig(HospitalReportService reportService) {
        this.reportService = reportService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publish() {
        try {
            String url = "http://localhost:8181/ws/soap/reportService";
            Endpoint.publish(url, reportService);
            log.info("SOAP report service published at {}?wsdl", url);
        } catch (Exception e) {
            log.error("Failed to publish SOAP service: {}", e.getMessage());
        }
    }
}
