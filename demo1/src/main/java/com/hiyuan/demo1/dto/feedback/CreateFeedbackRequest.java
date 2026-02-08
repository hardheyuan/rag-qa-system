package com.hiyuan.demo1.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateFeedbackRequest {

    @NotNull(message = "教师ID不能为空")
    private UUID teacherId;

    private UUID qaHistoryId;

    private String type;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String content;
}
