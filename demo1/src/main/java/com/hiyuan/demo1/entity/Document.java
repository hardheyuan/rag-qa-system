package com.hiyuan.demo1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hiyuan.demo1.enums.DocumentStatus;
import com.hiyuan.demo1.enums.FileType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档实体类 - 映射数据库中的 t_document 表
 * 
 * 这个类是RAG系统的核心实体之一，代表用户上传的文档
 * 
 * 主要功能：
 * 1. 存储文档的基本信息（文件名、大小、类型等）
 * 2. 跟踪文档的处理状态（上传中、处理中、成功、失败）
 * 3. 记录文档的处理时间和错误信息
 * 4. 关联文档的分块和向量记录
 * 
 * 文档处理流程：
 * 1. UPLOADING - 文件上传中
 * 2. PROCESSING - 后台处理中（解析、分块、向量化）
 * 3. SUCCESS - 处理成功，可以用于问答
 * 4. FAILED - 处理失败，记录错误信息
 * 
 * 支持的文件类型：
 * - PDF: 使用PDFBox解析
 * - DOCX: 使用Apache POI解析
 * - PPTX: 使用Apache POI解析
 * - TXT: 直接读取文本
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Entity  // JPA注解：标识这是一个数据库实体类
@Table(name = "t_document", uniqueConstraints = {
        // 唯一约束：同一用户不能上传同名文件
        @UniqueConstraint(columnNames = { "user_id", "filename" })
})
@Getter  // Lombok：自动生成getter方法
@Setter  // Lombok：自动生成setter方法
@NoArgsConstructor  // Lombok：生成无参构造函数
@AllArgsConstructor // Lombok：生成全参构造函数
@Builder // Lombok：生成建造者模式代码
public class Document extends BaseEntity {

    /**
     * 所属用户
     * 
     * 关联关系：多对一 (Many-to-One)
     * - 多个文档可以属于同一个用户
     * - 每个文档必须有一个所属用户
     * 
     * 数据库设计：
     * - 外键关联到t_user表的id字段
     * - 不能为空（nullable = false）
     * - 懒加载（LAZY）：只有访问时才查询用户信息
     * 
     * 业务意义：
     * - 实现用户数据隔离
     * - 支持多用户系统
     * - 便于权限控制和数据统计
     */
    @ManyToOne(fetch = FetchType.LAZY)  // 多对一关系，懒加载
    @JoinColumn(name = "user_id", nullable = false)  // 外键列名
    @JsonIgnore  // 避免序列化时循环引用
    private User user;

    /**
     * 文件名
     * 
     * 业务规则：
     * - 必填字段，不能为空
     * - 最大长度255字符
     * - 同一用户下文件名唯一（通过数据库约束保证）
     * - 保留原始文件名，便于用户识别
     * 
     * 使用场景：
     * - 前端显示文件列表
     * - 文件下载时的默认名称
     * - 问答时显示引用来源
     */
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /**
     * 文件存储路径
     * 
     * 存储规则：
     * - 相对路径，如：uploads/user_123/document.pdf
     * - 按用户ID分目录存储，避免文件名冲突
     * - 最大长度500字符，支持深层目录结构
     * 
     * 路径格式示例：
     * - uploads/{userId}/{filename}
     * - uploads/550e8400-e29b-41d4-a716-446655440000/Java教程.pdf
     * 
     * 注意事项：
     * - 生产环境建议使用对象存储（如AWS S3）
     * - 需要考虑文件备份和容灾
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     * 
     * 用途：
     * - 前端显示文件大小信息
     * - 存储空间统计和管理
     * - 上传限制检查（当前限制50MB）
     * 
     * 数据类型：Long
     * - 支持最大9,223,372,036,854,775,807字节
     * - 约等于9EB（艾字节），足够使用
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件类型枚举
     * 
     * 支持的类型：
     * - PDF: PDF文档
     * - DOCX: Word文档
     * - PPTX: PowerPoint演示文稿
     * - TXT: 纯文本文件
     * 
     * 存储方式：
     * - 使用枚举类型，确保数据一致性
     * - 数据库中存储为字符串（EnumType.STRING）
     * - 便于后续扩展新的文件类型
     */
    @Enumerated(EnumType.STRING)  // 枚举以字符串形式存储
    @Column(name = "file_type", length = 50)
    private FileType fileType;

    /**
     * 文档处理状态
     * 
     * 状态流转：
     * UPLOADING → PROCESSING → SUCCESS/FAILED
     * 
     * 状态说明：
     * - UPLOADING: 文件上传中，用户刚提交文件
     * - PROCESSING: 后台处理中（解析、分块、向量化）
     * - SUCCESS: 处理成功，可以用于问答
     * - FAILED: 处理失败，查看errorMessage了解原因
     * 
     * 前端展示：
     * - UPLOADING: 显示上传进度条
     * - PROCESSING: 显示"处理中"状态，预计30-50秒
     * - SUCCESS: 显示"已完成"，绿色状态
     * - FAILED: 显示"处理失败"，红色状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default  // Builder模式的默认值
    private DocumentStatus status = DocumentStatus.UPLOADING;

    /**
     * 错误信息
     * 
     * 记录文档处理失败的详细原因：
     * - 文件格式不支持
     * - 文件损坏无法解析
     * - 向量化服务异常
     * - 数据库存储失败
     * 
     * 数据类型：TEXT
     * - 支持长文本存储
     * - 可以记录完整的异常堆栈信息
     * 
     * 使用场景：
     * - 前端显示错误提示
     * - 开发调试和问题排查
     * - 系统监控和告警
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 上传时间
     * 
     * 记录用户上传文件的时间点
     * - 默认为当前时间
     * - 用于文件列表排序
     * - 用于数据统计和分析
     */
    @Column(name = "uploaded_at")
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    /**
     * 处理完成时间
     * 
     * 记录文档处理完成的时间点
     * - 只有状态为SUCCESS或FAILED时才设置
     * - 可以计算处理耗时：processedAt - uploadedAt
     * - 用于性能监控和优化
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 分块数量
     * 
     * 记录文档被分成多少个文本块
     * - 默认为0（未处理时）
     * - 处理成功后更新为实际分块数
     * - 用于统计和性能分析
     * 
     * 分块规则：
     * - 每块约1000字符
     * - 块之间有100字符重叠
     * - 一个10页PDF大约产生20-30个分块
     */
    @Column(name = "chunk_count")
    @Builder.Default
    private Integer chunkCount = 0;

    /**
     * 文档描述
     * 
     * 用户可选填的文档描述信息
     * - 最大长度500字符
     * - 帮助用户识别和管理文档
     * - 可用于文档搜索和分类
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 文档的文本分块列表
     * 
     * 关联关系：一对多 (One-to-Many)
     * - 一个文档可以有多个文本分块
     * - 分块是RAG系统的基础单元
     * 
     * 级联操作：
     * - 删除文档时，自动删除所有分块
     * - 孤儿删除：如果分块不再关联文档，自动删除
     * 
     * 使用场景：
     * - 查看文档的所有分块内容
     * - 统计分块数量
     * - 调试向量化结果
     */
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore  // 避免序列化时循环引用
    private List<DocumentChunk> chunks = new ArrayList<>();

    /**
     * 文档的向量记录列表
     * 
     * 关联关系：一对多 (One-to-Many)
     * - 一个文档对应多个向量记录
     * - 每个分块对应一个向量记录
     * - 向量记录用于相似度搜索
     * 
     * 数据一致性：
     * - chunks.size() 应该等于 vectorRecords.size()
     * - 都等于 chunkCount 字段的值
     */
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore  // 避免序列化时循环引用
    private List<VectorRecord> vectorRecords = new ArrayList<>();
    
    /**
     * JPA 钩子：在保存到数据库前自动清理非法字符
     * 
     * 这是最后一道防线，确保即使通过 Builder 或其他方式创建的对象
     * 在持久化前也会被清理
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.filename = cleanForPostgres(this.filename);
        this.filePath = cleanForPostgres(this.filePath);
        this.errorMessage = cleanForPostgres(this.errorMessage);
        this.description = cleanForPostgres(this.description);
    }
    
    /**
     * 深度清理字符串，移除所有 PostgreSQL UTF-8 不支持的字符
     * 
     * 包括：控制字符、空字符、BOM标记等
     */
    private String cleanForPostgres(String input) {
        if (input == null) {
            return null;
        }
        return input
            .replaceAll("[\u0000-\u0008]", "")   // 控制字符 0x00-0x08
            .replaceAll("[\u000B-\u000C]", "")   // 垂直制表符和换页符
            .replaceAll("[\u000E-\u001F]", "")   // 控制字符 0x0E-0x1F
            .replaceAll("[\u007F-\u009F]", "")   // DEL 和扩展控制字符
            .replaceAll("\uFEFF", "");             // BOM 标记
    }
    
    /**
     * 自定义 setter：清理 filename 中的空字符
     */
    public void setFilename(String filename) {
        this.filename = cleanForPostgres(filename);
    }
    
    /**
     * 自定义 setter：清理 filePath 中的空字符
     */
    public void setFilePath(String filePath) {
        this.filePath = cleanForPostgres(filePath);
    }
    
    /**
     * 自定义 setter：清理 errorMessage 中的空字符
     * 
     * 这是最关键的字段，因为 OCR 错误信息最常包含空字符
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = cleanForPostgres(errorMessage);
    }
    
    /**
     * 自定义 setter：清理 description 中的空字符
     */
    public void setDescription(String description) {
        this.description = cleanForPostgres(description);
    }
    
    /*
     * 使用示例：
     * 
     * 1. 创建新文档记录：
     * Document doc = Document.builder()
     *     .user(user)
     *     .filename("Java教程.pdf")
     *     .filePath("uploads/user_123/Java教程.pdf")
     *     .fileSize(2048576L)  // 2MB
     *     .fileType(FileType.PDF)
     *     .status(DocumentStatus.UPLOADING)
     *     .description("Java基础教程")
     *     .build();
     * documentRepository.save(doc);
     * 
     * 2. 更新处理状态：
     * doc.setStatus(DocumentStatus.PROCESSING);
     * documentRepository.save(doc);
     * 
     * // 处理完成后
     * doc.setStatus(DocumentStatus.SUCCESS);
     * doc.setProcessedAt(LocalDateTime.now());
     * doc.setChunkCount(25);  // 分成25块
     * documentRepository.save(doc);
     * 
     * 3. 处理失败时：
     * doc.setStatus(DocumentStatus.FAILED);
     * doc.setErrorMessage("文件格式不支持");
     * doc.setProcessedAt(LocalDateTime.now());
     * documentRepository.save(doc);
     * 
     * 4. 查询用户的成功文档：
     * List<Document> successDocs = documentRepository
     *     .findByUserAndStatus(user, DocumentStatus.SUCCESS);
     * 
     * 5. 统计处理时间：
     * Duration processingTime = Duration.between(
     *     doc.getUploadedAt(), 
     *     doc.getProcessedAt()
     * );
     * System.out.println("处理耗时: " + processingTime.getSeconds() + "秒");
     */
}
