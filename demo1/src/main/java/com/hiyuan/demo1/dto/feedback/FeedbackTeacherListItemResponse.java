package com.hiyuan.demo1.dto.feedback;

import com.hiyuan.demo1.entity.FeedbackStatus;
import com.hiyuan.demo1.entity.FeedbackType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeedbackTeacherListItemResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private FeedbackType type;
    private String content;
    private FeedbackStatus status;
    private LocalDateTime createdAt;
}
