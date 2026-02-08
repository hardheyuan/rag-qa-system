package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.feedback.CreateFeedbackRequest;
import com.hiyuan.demo1.dto.feedback.FeedbackStudentItemResponse;
import com.hiyuan.demo1.dto.feedback.FeedbackTeacherOptionResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/student/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/teachers")
    public ApiResponse<List<FeedbackTeacherOptionResponse>> listTeachers(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(feedbackService.listStudentTeachers(principal.getId()));
    }

    @PostMapping
    public ApiResponse<FeedbackStudentItemResponse> submitFeedback(
            @Valid @RequestBody CreateFeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        FeedbackStudentItemResponse response = feedbackService.submitStudentFeedback(principal.getId(), request);
        return ApiResponse.success("反馈提交成功", response);
    }

    @GetMapping("/mine")
    public ApiResponse<Page<FeedbackStudentItemResponse>> listMyFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ApiResponse.success(feedbackService.listStudentFeedback(principal.getId(), pageRequest));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMyFeedback(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        feedbackService.deleteStudentFeedback(principal.getId(), id);
        return ApiResponse.success("反馈删除成功");
    }
}
