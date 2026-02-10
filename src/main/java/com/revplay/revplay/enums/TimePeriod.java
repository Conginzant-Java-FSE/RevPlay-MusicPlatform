package com.revplay.revplay.enums;

import java.time.LocalDateTime;

public enum TimePeriod {
    DAILY(1, "Last 24 hours"),
    WEEKLY(7, "Last 7 days"),
    MONTHLY(30, "Last 30 days");

    private final int days;
    private final String description;

    TimePeriod(int days, String description) {
        this.days = days;
        this.description = description;
    }

    public int getDays() {
        return days;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartDate() {
        return LocalDateTime.now().minusDays(days);
    }

    public LocalDateTime getStartDateFrom(LocalDateTime from) {
        return from.minusDays(days);
    }
}


