package com.hiyuan.demo1.entity;

public enum FeedbackStatus {
    PENDING,
    REPLIED;

    public static FeedbackStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (FeedbackStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return PENDING;
    }
}
