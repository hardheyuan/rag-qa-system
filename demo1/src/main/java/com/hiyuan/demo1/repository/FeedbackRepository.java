package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.Feedback;
import com.hiyuan.demo1.entity.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Page<Feedback> findByStudent_IdOrderByCreatedAtDesc(UUID studentId, Pageable pageable);

    Page<Feedback> findByTeacher_IdOrderByCreatedAtDesc(UUID teacherId, Pageable pageable);

    Page<Feedback> findByTeacher_IdAndStatusOrderByCreatedAtDesc(UUID teacherId, FeedbackStatus status, Pageable pageable);

    Optional<Feedback> findByIdAndTeacher_Id(UUID id, UUID teacherId);

    Optional<Feedback> findByIdAndStudent_Id(UUID id, UUID studentId);

    long countByTeacher_IdAndStatus(UUID teacherId, FeedbackStatus status);
}
