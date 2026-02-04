package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.DocumentChunk;
import com.hiyuan.demo1.enums.DocumentStatus;
import com.hiyuan.demo1.enums.FileType;
import com.hiyuan.demo1.exception.DocumentNotFoundException;
import com.hiyuan.demo1.exception.DocumentProcessingException;
import com.hiyuan.demo1.exception.VectorOperationException;
import com.hiyuan.demo1.repository.DocumentChunkRepository;
import com.hiyuan.demo1.repository.DocumentRepository;
import com.hiyuan.demo1.util.VectorUtils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文档异步处理服务 - 独立服务确保 @Async 生效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessorService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final VectorStorageService vectorStorageService;
    private final EmbeddingModel embeddingModel;
    private final MrlService mrlService;
    private final AliyunOcrService aliyunOcrService;

    @Value("${document.chunk-size:1000}")
    private int chunkSize;

    @Value("${document.chunk-overlap:100}")
    private int chunkOverlap;

    /**
     * 异步处理文档（解析、分块、向量化）
     */
    @Async("taskExecutor")
    public void processDocumentAsync(UUID documentId) {
        log.info("开始异步处理文档: id={}", documentId);

        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> DocumentNotFoundException.forId(documentId));

        try {
            // 更新状态为处理中
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            // 1. 解析文档内容
            String content = parseDocument(document);
            if (content == null || content.trim().isEmpty()) {
                throw DocumentProcessingException.emptyContent(documentId);
            }
            log.info("文档解析完成，内容长度: {} 字符", content.length());

            // 2. 文本分块
            List<String> chunks = splitIntoChunks(content);
            log.info("文本分块完成，共 {} 个分块", chunks.size());

            // 3. 保存分块并向量化
            saveChunksAndVectors(document, chunks);

            // 4. 更新文档状态
            document.setStatus(DocumentStatus.SUCCESS);
            document.setChunkCount(chunks.size());
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);

            log.info("文档处理成功: id={}, chunks={}", documentId, chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: id={}, error={}", documentId, e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);
        }
    }

    /**
     * 解析文档内容
     */
    private String parseDocument(Document document) throws IOException {
        Path filePath = Paths.get(document.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在: " + filePath);
        }

        FileType fileType = document.getFileType();
        if (fileType == null) {
            throw new IOException("未知的文件类型");
        }

        return switch (fileType) {
            case PDF -> parsePdfWithOcr(filePath);  // PDF 特殊处理，支持 OCR
            case DOCX -> parseDocx(Files.newInputStream(filePath));
            case PPTX -> parsePptx(Files.newInputStream(filePath));
        };
    }

    /**
     * 解析 PDF，支持 OCR 识别扫描版
     */
    private String parsePdfWithOcr(Path filePath) throws IOException {
        log.error("[PDF-OCR-开始] 开始解析 PDF: {}", filePath.getFileName());
        
        // 1. 先用 PDFBox 提取文字
        String text;
        try (InputStream is = Files.newInputStream(filePath)) {
            text = parsePdf(is);
        }
        
        log.error("[PDF-OCR-PDFBox] PDFBox 提取结果: 长度={} 字符, 内容预览={}", 
                text != null ? text.length() : 0,
                text != null && text.length() > 50 ? text.substring(0, 50).replaceAll("\\s+", " ") + "..." : text);
        
        // 2. 检查是否需要 OCR（文字太少，可能是扫描版）
        boolean needsOcr = aliyunOcrService.needsOcr(text);
        log.error("[PDF-OCR-判断] 是否需要 OCR: {}", needsOcr);
        
        if (needsOcr) {
            log.error("[PDF-OCR-调用] 文字太少({}字符)，调用阿里云 OCR...", text != null ? text.length() : 0);
            try {
                String ocrText = aliyunOcrService.extractTextFromPdf(filePath);
                log.error("[PDF-OCR-结果] OCR 返回: 长度={} 字符", ocrText != null ? ocrText.length() : 0);
                
                if (ocrText != null && !ocrText.trim().isEmpty()) {
                    log.error("[PDF-OCR-成功] 阿里云 OCR 识别成功，识别出 {} 字符", ocrText.length());
                    return ocrText;
                } else {
                    log.error("[PDF-OCR-失败] 阿里云 OCR 返回空结果，将使用原始提取结果({}字符)", 
                            text != null ? text.length() : 0);
                }
            } catch (Exception e) {
                log.error("[PDF-OCR-异常] 阿里云 OCR 识别异常: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
                // OCR 失败时，返回原始提取的文字（即使很少）
            }
        } else {
            log.error("[PDF-OCR-跳过] 文字充足({}字符)，跳过 OCR", text != null ? text.length() : 0);
        }
        
        log.error("[PDF-OCR-结束] 返回结果: {} 字符", text != null ? text.length() : 0);
        return text;
    }

    private String parsePdf(InputStream is) throws IOException {
        try (PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }

    private String parseDocx(InputStream is) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : doc.getParagraphs()) {
                sb.append(para.getText()).append("\n");
            }
            String textContent = sb.toString();

            if (aliyunOcrService.needsOcr(textContent)) {
                log.error("[DOCX-OCR] 文本过少，尝试识别图片中的文字...");
                List<XWPFPictureData> pictures = doc.getAllPictures();
                if (pictures != null && !pictures.isEmpty()) {
                    StringBuilder imageText = new StringBuilder();
                    int successCount = 0;
                    int failCount = 0;
                    for (int i = 0; i < pictures.size(); i++) {
                        try {
                            byte[] data = pictures.get(i).getData();
                            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
                            if (image == null) {
                                log.error("[DOCX-OCR] 第 {} 张图片无法读取", i + 1);
                                failCount++;
                                continue;
                            }
                            String ocrText = aliyunOcrService.extractTextFromImage(image);
                            if (ocrText != null && !ocrText.trim().isEmpty()) {
                                imageText.append("\n=== 图片 ").append(i + 1).append(" ===\n");
                                imageText.append(ocrText).append("\n");
                                successCount++;
                            } else {
                                failCount++;
                                log.error("[DOCX-OCR] 第 {} 张图片 OCR 结果为空", i + 1);
                            }
                        } catch (Exception e) {
                            failCount++;
                            log.error("[DOCX-OCR] 第 {} 张图片 OCR 失败: {} - {}", i + 1,
                                    e.getClass().getSimpleName(), e.getMessage());
                        }
                    }

                    log.error("[DOCX-OCR] 图片 OCR 完成: 成功={}, 失败={}", successCount, failCount);
                    if (imageText.length() > 0) {
                        return textContent + "\n" + imageText;
                    }
                } else {
                    log.error("[DOCX-OCR] 文档中未发现图片");
                }
            }

            return textContent;
        }
    }

    private String parsePptx(InputStream is) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
            StringBuilder sb = new StringBuilder();
            int slideNum = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                sb.append("--- 幻灯片 ").append(slideNum++).append(" ---\n");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        sb.append(textShape.getText()).append("\n");
                    }
                }
                sb.append("\n");
            }
            String textContent = sb.toString();

            if (aliyunOcrService.needsOcr(textContent)) {
                log.error("[PPTX-OCR] 文本过少，尝试识别幻灯片中的图片...");
                StringBuilder imageText = new StringBuilder();
                int successCount = 0;
                int failCount = 0;

                int slideIndex = 0;
                for (XSLFSlide slide : ppt.getSlides()) {
                    slideIndex++;
                    for (XSLFShape shape : slide.getShapes()) {
                        try {
                            if (shape instanceof org.apache.poi.xslf.usermodel.XSLFPictureShape picShape) {
                                byte[] data = picShape.getPictureData().getData();
                                BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
                                if (image == null) {
                                    log.error("[PPTX-OCR] 幻灯片 {} 图片无法读取", slideIndex);
                                    failCount++;
                                    continue;
                                }
                                String ocrText = aliyunOcrService.extractTextFromImage(image);
                                if (ocrText != null && !ocrText.trim().isEmpty()) {
                                    imageText.append("\n=== 幻灯片 ").append(slideIndex).append(" 图片 ===\n");
                                    imageText.append(ocrText).append("\n");
                                    successCount++;
                                } else {
                                    failCount++;
                                    log.error("[PPTX-OCR] 幻灯片 {} 图片 OCR 结果为空", slideIndex);
                                }
                            }
                        } catch (Exception e) {
                            failCount++;
                            log.error("[PPTX-OCR] 幻灯片 {} 图片 OCR 失败: {} - {}", slideIndex,
                                    e.getClass().getSimpleName(), e.getMessage());
                        }
                    }
                }

                log.error("[PPTX-OCR] 图片 OCR 完成: 成功={}, 失败={}", successCount, failCount);
                if (imageText.length() > 0) {
                    return textContent + "\n" + imageText;
                }
            }

            return textContent;
        }
    }

    /**
     * 文本分块 - 优化内存使用
     */
    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        // 清理文本 - 使用 StringBuilder 减少内存分配
        StringBuilder cleaned = new StringBuilder(content.length());
        boolean lastWasSpace = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!lastWasSpace) {
                    cleaned.append(' ');
                    lastWasSpace = true;
                }
            } else {
                cleaned.append(c);
                lastWasSpace = false;
            }
        }
        
        String cleanedContent = cleaned.toString().trim();
        cleaned = null; // 释放 StringBuilder
        
        int contentLength = cleanedContent.length();
        int start = 0;
        
        while (start < contentLength) {
            int end = Math.min(start + chunkSize, contentLength);
            
            // 尝试在句子边界处分割
            if (end < contentLength) {
                int searchStart = Math.max(start + chunkSize / 2, start);
                
                // 直接在原字符串上查找，避免创建子字符串
                int breakPoint = -1;
                for (int i = end - 1; i >= searchStart; i--) {
                    char c = cleanedContent.charAt(i);
                    if (c == '。' || c == '？' || c == '！' || 
                        (c == '.' && i + 1 < contentLength && cleanedContent.charAt(i + 1) == ' ')) {
                        breakPoint = i + 1;
                        break;
                    }
                }
                
                if (breakPoint > searchStart) {
                    end = breakPoint;
                }
            }

            String chunk = cleanedContent.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            int nextStart = end - chunkOverlap;
            if (nextStart <= start) {
                nextStart = end;
            }
            start = nextStart;
        }

        return chunks;
    }

    /**
     * 深度清理文本，移除所有可能导致 PostgreSQL 错误的非法字符
     * 
     * 包括：控制字符、空字符、BOM标记等
     */
    private String deepCleanText(String input) {
        if (input == null) {
            return null;
        }
        // 移除所有 PostgreSQL UTF-8 不支持的字符
        return input
            .replaceAll("[\\u0000-\\u0008]", "")   // 控制字符 0x00-0x08
            .replaceAll("[\\u000B-\\u000C]", "")   // 垂直制表符和换页符
            .replaceAll("[\\u000E-\\u001F]", "")   // 控制字符 0x0E-0x1F
            .replaceAll("[\\u007F-\\u009F]", "")   // DEL 和扩展控制字符
            .replaceAll("\\uFEFF", "")             // BOM 标记
            .trim();
    }

    /**
     * 保存分块并生成向量
     */
    public void saveChunksAndVectors(Document document, List<String> chunks) {
        log.info("开始保存分块和向量: documentId={}, chunkCount={}", document.getId(), chunks.size());

        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < chunks.size(); i++) {
            // 清理分块内容，移除非法字符
            String chunkContent = deepCleanText(chunks.get(i));
            
            // 如果清理后内容为空，跳过
            if (chunkContent == null || chunkContent.isEmpty()) {
                log.warn("分块 {} 清理后内容为空，跳过", i);
                failCount++;
                continue;
            }

            DocumentChunk chunk = DocumentChunk.builder()
                    .document(document)
                    .chunkIndex(i)
                    .content(chunkContent)
                    .contentLength(chunkContent.length())
                    .build();
            chunk = chunkRepository.save(chunk);

            try {
                // 检查分块内容
                if (chunkContent == null || chunkContent.trim().isEmpty()) {
                    log.warn("分块 {} 内容为空，跳过", i);
                    failCount++;
                    continue;
                }
                
                log.debug("分块 {} 开始向量化，长度: {} 字符", i, chunkContent.length());
                
                // 添加延迟避免 API 限流（每次请求间隔 1 秒）
                if (i > 0) {
                    Thread.sleep(1000);
                }
                
                float[] fullVector = embeddingModel.embed(chunkContent).content().vector();
                
                // 使用 MRL 截断向量到目标维度
                float[] truncatedVector = mrlService.truncateVector(fullVector);
                String vectorStr = VectorUtils.vectorToString(truncatedVector);

                // 使用单独的事务服务插入向量记录
                UUID vectorId = UUID.randomUUID();
                vectorStorageService.insertVectorRecord(
                        vectorId,
                        chunk.getId(),
                        document.getId(),
                        vectorStr,
                        truncatedVector.length,
                        "Qwen/Qwen3-Embedding-0.6B"
                );

                successCount++;
                log.info("分块 {} 向量化完成 ({}维 -> {}维)", i, fullVector.length, truncatedVector.length);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("处理被中断");
                break;
            } catch (Exception e) {
                failCount++;
                log.error("分块 {} 向量化失败，内容长度: {}, 错误: {}", i, chunkContent.length(), e.getMessage(), e);
                // 继续处理下一个分块，不中断整个流程
            }
        }
        
        log.info("分块向量化完成: 成功={}, 失败={}", successCount, failCount);
        
        // 如果所有分块都失败了，抛出异常
        if (successCount == 0 && chunks.size() > 0) {
            throw VectorOperationException.embeddingError(documentId,
                "所有分块向量化都失败了，请检查嵌入服务配置");
        }
    }

}
