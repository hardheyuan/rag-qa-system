package com.hiyuan.demo1.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReplyFeedbackRequest {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容不能超过1000字")
    private String replyContent;
}
