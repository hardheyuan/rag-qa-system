package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.feedback.FeedbackTeacherDetailResponse;
import com.hiyuan.demo1.dto.feedback.FeedbackTeacherListItemResponse;
import com.hiyuan.demo1.dto.feedback.ReplyFeedbackRequest;
import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/teacher/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/pending")
    public ApiResponse<Page<FeedbackTeacherListItemResponse>> listPendingFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ApiResponse.success(feedbackService.listTeacherPendingFeedback(principal.getId(), pageRequest));
    }

    @GetMapping("/pending/count")
    public ApiResponse<Long> countPendingFeedback(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(feedbackService.countTeacherPendingFeedback(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FeedbackTeacherDetailResponse> getFeedbackDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(feedbackService.getTeacherFeedbackDetail(principal.getId(), id));
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<FeedbackTeacherDetailResponse> replyFeedback(
            @PathVariable UUID id,
            @Valid @RequestBody ReplyFeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        FeedbackTeacherDetailResponse response = feedbackService.replyFeedback(
                principal.getId(),
                id,
                request.getReplyContent()
        );
        return ApiResponse.success("回复已发送", response);
    }
}
