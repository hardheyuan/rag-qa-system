package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.Feedback;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.ClassAssociationRepository;
import com.hiyuan.demo1.repository.FeedbackRepository;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassAssociationRepository classAssociationRepository;

    @Mock
    private QaHistoryRepository qaHistoryRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void deleteStudentFeedbackShouldDeleteOwnedFeedback() {
        UUID studentId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();

        User student = User.builder().username("student").build();
        student.setId(studentId);

        Feedback feedback = Feedback.builder().student(student).build();
        feedback.setId(feedbackId);

        when(feedbackRepository.findByIdAndStudent_Id(feedbackId, studentId)).thenReturn(Optional.of(feedback));

        feedbackService.deleteStudentFeedback(studentId, feedbackId);

        verify(feedbackRepository).delete(feedback);
    }

    @Test
    void deleteStudentFeedbackShouldThrowWhenNotOwned() {
        UUID studentId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();

        when(feedbackRepository.findByIdAndStudent_Id(feedbackId, studentId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> feedbackService.deleteStudentFeedback(studentId, feedbackId));
    }
}
