package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentFeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private StudentFeedbackController studentFeedbackController;

    @Test
    void deleteFeedbackShouldCallServiceAndReturnSuccess() {
        UUID studentId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(studentId, "student", "pwd", UserRole.STUDENT);

        ApiResponse<Void> response = studentFeedbackController.deleteMyFeedback(feedbackId, principal);

        verify(feedbackService).deleteStudentFeedback(studentId, feedbackId);
        assertEquals(200, response.getCode());
        assertEquals("反馈删除成功", response.getMessage());
    }
}
