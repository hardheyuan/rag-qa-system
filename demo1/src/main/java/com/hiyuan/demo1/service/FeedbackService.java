package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.feedback.CreateFeedbackRequest;
import com.hiyuan.demo1.dto.feedback.FeedbackStudentItemResponse;
import com.hiyuan.demo1.dto.feedback.FeedbackTeacherDetailResponse;
import com.hiyuan.demo1.dto.feedback.FeedbackTeacherListItemResponse;
import com.hiyuan.demo1.dto.feedback.FeedbackTeacherOptionResponse;
import com.hiyuan.demo1.entity.ClassAssociation;
import com.hiyuan.demo1.entity.Feedback;
import com.hiyuan.demo1.entity.FeedbackStatus;
import com.hiyuan.demo1.entity.FeedbackType;
import com.hiyuan.demo1.entity.QaHistory;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.exception.AuthorizationException;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.ClassAssociationRepository;
import com.hiyuan.demo1.repository.FeedbackRepository;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ClassAssociationRepository classAssociationRepository;
    private final QaHistoryRepository qaHistoryRepository;

    @Transactional(readOnly = true)
    public List<FeedbackTeacherOptionResponse> listStudentTeachers(UUID studentId) {
        List<ClassAssociation> associations = classAssociationRepository.findByStudentId(studentId);
        Map<UUID, FeedbackTeacherOptionResponse> uniqueTeachers = new LinkedHashMap<>();

        for (ClassAssociation association : associations) {
            User teacher = association.getTeacher();
            if (teacher == null || teacher.getId() == null || teacher.getRole() != UserRole.TEACHER) {
                continue;
            }
            uniqueTeachers.putIfAbsent(
                    teacher.getId(),
                    FeedbackTeacherOptionResponse.builder()
                            .id(teacher.getId())
                            .username(teacher.getUsername())
                            .build()
            );
        }

        return new ArrayList<>(uniqueTeachers.values());
    }

    @Transactional
    public FeedbackStudentItemResponse submitStudentFeedback(UUID studentId, CreateFeedbackRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .filter(user -> user.getRole() == UserRole.TEACHER)
                .orElseThrow(() -> BusinessException.notFound("教师", String.valueOf(request.getTeacherId())));

        if (!classAssociationRepository.existsByTeacherIdAndStudentId(teacher.getId(), studentId)) {
            throw new AuthorizationException("只能向已关联的教师提交反馈");
        }

        QaHistory qaHistory = null;
        if (request.getQaHistoryId() != null) {
            boolean ownsHistory = qaHistoryRepository.existsByIdAndUserId(request.getQaHistoryId(), studentId);
            if (!ownsHistory) {
                throw new AuthorizationException("只能关联自己的问答记录");
            }
            qaHistory = qaHistoryRepository.getReferenceById(request.getQaHistoryId());
        }

        Feedback feedback = Feedback.builder()
                .student(userRepository.getReferenceById(studentId))
                .teacher(teacher)
                .qaHistory(qaHistory)
                .feedbackType(FeedbackType.fromString(request.getType()))
                .content(request.getContent().trim())
                .status(FeedbackStatus.PENDING)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return toStudentItem(saved);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackStudentItemResponse> listStudentFeedback(UUID studentId, Pageable pageable) {
        return feedbackRepository.findByStudent_IdOrderByCreatedAtDesc(studentId, pageable)
                .map(this::toStudentItem);
    }

    @Transactional
    public void deleteStudentFeedback(UUID studentId, UUID feedbackId) {
        Feedback feedback = feedbackRepository.findByIdAndStudent_Id(feedbackId, studentId)
                .orElseThrow(() -> BusinessException.notFound("反馈", String.valueOf(feedbackId)));
        feedbackRepository.delete(feedback);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackTeacherListItemResponse> listTeacherPendingFeedback(UUID teacherId, Pageable pageable) {
        return feedbackRepository.findByTeacher_IdAndStatusOrderByCreatedAtDesc(teacherId, FeedbackStatus.PENDING, pageable)
                .map(this::toTeacherListItem);
    }

    @Transactional(readOnly = true)
    public long countTeacherPendingFeedback(UUID teacherId) {
        return feedbackRepository.countByTeacher_IdAndStatus(teacherId, FeedbackStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public FeedbackTeacherDetailResponse getTeacherFeedbackDetail(UUID teacherId, UUID feedbackId) {
        Feedback feedback = requireTeacherFeedback(teacherId, feedbackId);
        return toTeacherDetail(feedback);
    }

    @Transactional
    public FeedbackTeacherDetailResponse replyFeedback(UUID teacherId, UUID feedbackId, String replyContent) {
        Feedback feedback = requireTeacherFeedback(teacherId, feedbackId);

        feedback.setReplyContent(replyContent.trim());
        feedback.setRepliedAt(LocalDateTime.now());
        feedback.setStatus(FeedbackStatus.REPLIED);

        Feedback saved = feedbackRepository.save(feedback);
        return toTeacherDetail(saved);
    }

    private Feedback requireTeacherFeedback(UUID teacherId, UUID feedbackId) {
        return feedbackRepository.findByIdAndTeacher_Id(feedbackId, teacherId)
                .orElseThrow(() -> BusinessException.notFound("反馈", String.valueOf(feedbackId)));
    }

    private FeedbackStudentItemResponse toStudentItem(Feedback feedback) {
        User teacher = feedback.getTeacher();
        return FeedbackStudentItemResponse.builder()
                .id(feedback.getId())
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherName(teacher != null ? teacher.getUsername() : "-")
                .type(feedback.getFeedbackType())
                .content(feedback.getContent())
                .status(feedback.getStatus())
                .replyContent(feedback.getReplyContent())
                .repliedAt(feedback.getRepliedAt())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private FeedbackTeacherListItemResponse toTeacherListItem(Feedback feedback) {
        User student = feedback.getStudent();
        return FeedbackTeacherListItemResponse.builder()
                .id(feedback.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(student != null ? student.getUsername() : "未知学生")
                .studentEmail(student != null ? student.getEmail() : "")
                .type(feedback.getFeedbackType())
                .content(feedback.getContent())
                .status(feedback.getStatus())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private FeedbackTeacherDetailResponse toTeacherDetail(Feedback feedback) {
        User student = feedback.getStudent();
        QaHistory qaHistory = feedback.getQaHistory();
        return FeedbackTeacherDetailResponse.builder()
                .id(feedback.getId())
                .studentId(student != null ? student.getId() : null)
                .studentName(student != null ? student.getUsername() : "未知学生")
                .studentEmail(student != null ? student.getEmail() : "")
                .qaHistoryId(qaHistory != null ? qaHistory.getId() : null)
                .type(feedback.getFeedbackType())
                .content(feedback.getContent())
                .status(feedback.getStatus())
                .replyContent(feedback.getReplyContent())
                .repliedAt(feedback.getRepliedAt())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
