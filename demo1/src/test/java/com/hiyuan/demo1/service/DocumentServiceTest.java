package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.DocumentRepository;
import com.hiyuan.demo1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentProcessorService processorService;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
    }

    @Test
    void uploadDocumentRejectsDuplicateFilenameIgnoringCaseAndSpaces() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().username("teacher").build();
        user.setId(userId);

        Document existing = Document.builder().filename("04-Web后端基础(基础知识) (1).pdf").build();
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("  04-Web后端基础(基础知识) (1).PDF  ");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(documentRepository.findByUserIdAndFilename(userId, "04-Web后端基础(基础知识) (1).PDF"))
                .thenReturn(Optional.empty());
        when(documentRepository.findByUserId(userId)).thenReturn(List.of(existing));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> documentService.uploadDocument(file, userId));

        assertTrue(ex.getMessage().contains("已存在同名文件"));
    }
}
