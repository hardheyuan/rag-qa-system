package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.repository.ClassAssociationRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QaDocumentAccessScopeResolverTest {

    @Mock
    private ClassAssociationRepository classAssociationRepository;

    @InjectMocks
    private QaDocumentAccessScopeResolver resolver;

    @Test
    void teacherCanQueryAllDocuments() {
        UUID teacherId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(teacherId, "teacher", "pwd", UserRole.TEACHER);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        QaDocumentAccessScopeResolver.AccessScope scope = resolver.resolve(auth);

        assertTrue(scope.queryAllDocuments());
        assertEquals(teacherId, scope.currentUserId());
        assertTrue(scope.ownerIds().isEmpty());
        verifyNoInteractions(classAssociationRepository);
    }

    @Test
    void studentCanQueryOwnAndAssociatedTeacherDocuments() {
        UUID studentId = UUID.randomUUID();
        UUID teacherA = UUID.randomUUID();
        UUID teacherB = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(studentId, "student", "pwd", UserRole.STUDENT);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(classAssociationRepository.findTeacherIdsByStudentId(studentId))
                .thenReturn(List.of(teacherA, teacherB, teacherA, studentId));

        QaDocumentAccessScopeResolver.AccessScope scope = resolver.resolve(auth);

        assertFalse(scope.queryAllDocuments());
        assertEquals(studentId, scope.currentUserId());
        assertEquals(List.of(studentId, teacherA, teacherB), scope.ownerIds());
    }

    @Test
    void anonymousUserFallsBackToAllDocuments() {
        QaDocumentAccessScopeResolver.AccessScope scope = resolver.resolve(null);

        assertTrue(scope.queryAllDocuments());
        assertNull(scope.currentUserId());
        assertTrue(scope.ownerIds().isEmpty());
        verifyNoInteractions(classAssociationRepository);
    }
}
