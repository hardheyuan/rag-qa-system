package com.hiyuan.demo1.dto.feedback;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FeedbackTeacherOptionResponse {
    private UUID id;
    private String username;
}
