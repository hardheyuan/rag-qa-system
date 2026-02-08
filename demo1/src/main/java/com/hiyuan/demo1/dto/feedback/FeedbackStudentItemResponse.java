package com.hiyuan.demo1.dto.feedback;

import com.hiyuan.demo1.entity.FeedbackStatus;
import com.hiyuan.demo1.entity.FeedbackType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeedbackStudentItemResponse {
    private UUID id;
    private UUID teacherId;
    private String teacherName;
    private FeedbackType type;
    private String content;
    private FeedbackStatus status;
    private String replyContent;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
