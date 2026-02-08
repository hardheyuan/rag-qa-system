package com.hiyuan.demo1.entity;

public enum FeedbackType {
    SUGGESTION,
    ISSUE,
    OTHER;

    public static FeedbackType fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        for (FeedbackType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return OTHER;
    }
}
