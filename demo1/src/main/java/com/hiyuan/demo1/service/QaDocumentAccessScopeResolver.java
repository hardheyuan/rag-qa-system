package com.hiyuan.demo1.service;

import com.hiyuan.demo1.repository.ClassAssociationRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QaDocumentAccessScopeResolver {

    private final ClassAssociationRepository classAssociationRepository;

    public AccessScope resolve(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            return new AccessScope(null, true, List.of(), "未找到认证用户，将查询所有文档");
        }

        UUID currentUserId = userPrincipal.getId();
        boolean canQueryAllDocs = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_TEACHER"));

        if (canQueryAllDocs) {
            return new AccessScope(currentUserId, true, List.of(), "管理员/教师权限：可查询全部文档");
        }

        Set<UUID> ownerIds = new LinkedHashSet<>();
        ownerIds.add(currentUserId);
        ownerIds.addAll(classAssociationRepository.findTeacherIdsByStudentId(currentUserId));

        List<UUID> normalizedOwnerIds = new ArrayList<>();
        for (UUID ownerId : ownerIds) {
            if (ownerId != null) {
                normalizedOwnerIds.add(ownerId);
            }
        }

        return new AccessScope(currentUserId, false, normalizedOwnerIds,
                "学生权限：可查询本人和已关联教师文档");
    }

    public record AccessScope(UUID currentUserId,
                              boolean queryAllDocuments,
                              List<UUID> ownerIds,
                              String description) {
    }
}
