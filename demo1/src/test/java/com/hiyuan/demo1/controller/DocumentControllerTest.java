package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.DocumentRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @Test
    void uploadDocumentReturnsBusinessErrorMessageWhenDuplicateFilename() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "teacher", "pwd", UserRole.TEACHER);
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("04-Web后端基础(基础知识) (1).pdf");
        when(file.getSize()).thenReturn(1024L);
        when(documentService.uploadDocument(file, userId))
                .thenThrow(BusinessException.badRequest("filename", "已存在同名文件: 04-Web后端基础(基础知识) (1).pdf"));

        ApiResponse<Document> response = documentController.uploadDocument(file, principal);

        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("已存在同名文件"));
    }
}
