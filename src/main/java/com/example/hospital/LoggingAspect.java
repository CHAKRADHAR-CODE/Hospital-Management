package com.example.hospital;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting logging for all service and controller layers.
 * IMPORTANT: the SOAP endpoint (SoapReportService) is deliberately excluded
 * from every pointcut because proxying it would hide the @WebService
 * annotation from JAX-WS Endpoint.publish.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.hospital.*Service.*(..)) "
            + "&& !within(com.example.hospital.SoapReportService)")
    public void serviceLayer() {
    }

    @Pointcut("execution(* com.example.hospital.*Controller.*(..))")
    public void controllerLayer() {
    }

    @Pointcut("serviceLayer() || controllerLayer()")
    public void allLayers() {
    }

    @Around("allLayers()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toShortString();
        boolean isService = joinPoint.getSignature().getDeclaringTypeName().endsWith("Service");
        log.info("{} {}", isService ? "[SVC]" : "[CTL] Enter -> {} args={}",
                signature, Arrays.toString(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("{} Exit  <- {} took {} ms return={}",
                    isService ? "[SVC]" : "[CTL]", signature, elapsed,
                    safeSummary(result));
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("{} ERROR <- {} took {} ms error={}",
                    isService ? "[SVC]" : "[CTL]", signature, elapsed, t.getMessage());
            throw t;
        }
    }

    @AfterThrowing(pointcut = "allLayers()", throwing = "ex")
    public void logAfterThrowing(org.aspectj.lang.JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {}: {} (cause: {})",
                joinPoint.getSignature().toShortString(),
                ex.getMessage(),
                ex.getCause() != null ? ex.getCause().getMessage() : "none");
    }

    private String safeSummary(Object result) {
        if (result == null) return "null";
        if (result instanceof org.springframework.http.ResponseEntity<?> re) {
            return "ResponseEntity[" + (re.getStatusCode() != null ? re.getStatusCode().value() : "?") + "]";
        }
        String s = result.toString();
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
