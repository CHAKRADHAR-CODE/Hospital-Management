package com.example.hospital;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

/**
 * A doctor availability slot (embedded inside Doctor).
 */
@Embeddable
public class Availability {

    @NotBlank(message = "Day is required")
    @Column(nullable = false, length = 20)
    private String day;

    @NotBlank(message = "Start time is required")
    @Column(nullable = false, length = 20)
    private String startTime;

    @NotBlank(message = "End time is required")
    @Column(nullable = false, length = 20)
    private String endTime;

    public Availability() {
    }

    public Availability(String day, String startTime, String endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
