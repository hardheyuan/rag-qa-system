package com.hiyuan.demo1.enums;

/**
 * 支持的文件类型枚举
 * 
 * 这个枚举定义了RAG系统支持的所有文档格式
 * 每种格式都包含对应的MIME类型和文件扩展名
 * 
 * 支持的格式及其特点：
 * - PDF: 最常用的文档格式，保持原始排版
 * - DOCX: Microsoft Word现代格式，支持丰富的文本和表格
 * - PPTX: PowerPoint演示文稿，适合教学材料
 * 
 * 解析技术栈：
 * - PDF: Apache PDFBox 3.0.0
 * - DOCX/PPTX: Apache POI 5.0.0
 * 
 * 为什么不支持更多格式？
 * - 这些是最常见的教学文档格式
 * - 解析库成熟稳定，错误率低
 * - 可以覆盖90%以上的使用场景
 * 
 * @author 开发团队
 * @version 1.0.0
 */
public enum FileType {
    
    /**
     * PDF 文档格式
     * 
     * 特点：
     * - 最常用的文档分享格式
     * - 保持原始排版和格式
     * - 跨平台兼容性好
     * - 文件大小相对较小
     * 
     * 解析能力：
     * - 提取纯文本内容
     * - 识别页码信息
     * - 处理多列布局
     * - 支持中英文混合内容
     * 
     * 解析库：Apache PDFBox 3.0.0
     * - 开源免费，功能强大
     * - 支持PDF 1.7及以下版本
     * - 内存使用优化
     * 
     * 常见问题：
     * - 扫描版PDF（图片）无法提取文字
     * - 复杂表格可能解析错乱
     * - 加密PDF需要密码
     * 
     * 适用场景：
     * - 学术论文、教材、手册
     * - 政府文件、法律文档
     * - 技术文档、API文档
     */
    PDF("application/pdf", ".pdf"),

    /**
     * Microsoft Word 文档格式 (.docx)
     * 
     * 特点：
     * - Office 2007+的现代格式
     * - 基于XML，结构化存储
     * - 支持丰富的格式和样式
     * - 文件大小适中
     * 
     * 解析能力：
     * - 提取段落文本
     * - 处理表格数据
     * - 识别标题层级
     * - 提取图片标题（Alt文本）
     * 
     * 解析库：Apache POI 5.0.0
     * - Java领域最成熟的Office文档处理库
     * - 支持完整的DOCX格式
     * - 内存效率高
     * 
     * 注意事项：
     * - 不支持旧版DOC格式（建议转换为DOCX）
     * - 复杂的格式可能丢失
     * - 嵌入对象（如图表）只能提取文字部分
     * 
     * 适用场景：
     * - 教学讲义、课程资料
     * - 企业文档、报告
     * - 用户手册、说明书
     */
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),

    /**
     * Microsoft PowerPoint 演示文稿格式 (.pptx)
     * 
     * 特点：
     * - 演示文稿专用格式
     * - 幻灯片结构化组织
     * - 支持文字、图片、动画
     * - 教学场景使用频繁
     * 
     * 解析能力：
     * - 提取每张幻灯片的文本
     * - 处理标题和正文
     * - 提取演讲者备注
     * - 识别幻灯片顺序
     * 
     * 解析库：Apache POI 5.0.0
     * - 与DOCX使用相同的技术栈
     * - 支持完整的PPTX格式
     * - 可以访问幻灯片的所有文本元素
     * 
     * 特殊处理：
     * - 每张幻灯片作为独立的文本块
     * - 保留幻灯片编号信息
     * - 合并标题和内容文本
     * 
     * 适用场景：
     * - 课程PPT、培训材料
     * - 会议演示、产品介绍
     * - 学术报告、研究展示
     */
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");

    /**
     * MIME类型
     * 用于HTTP请求中的Content-Type头
     * 浏览器和服务器通过MIME类型识别文件格式
     */
    private final String mimeType;
    
    /**
     * 文件扩展名
     * 用于文件名识别和验证
     * 包含点号，如：.pdf, .docx, .pptx
     */
    private final String extension;

    /**
     * 构造函数
     * 
     * @param mimeType MIME类型
     * @param extension 文件扩展名
     */
    FileType(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    /**
     * 获取MIME类型
     * 
     * @return MIME类型字符串
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 获取文件扩展名
     * 
     * @return 扩展名字符串（包含点号）
     */
    public String getExtension() {
        return extension;
    }

    /**
     * 根据文件扩展名获取文件类型
     * 
     * 这个方法用于文件上传时识别文件类型
     * 支持大小写不敏感匹配
     * 
     * @param extension 文件扩展名（可以带点号或不带）
     * @return 对应的FileType，如果不支持则返回null
     * 
     * 使用示例：
     * FileType.fromExtension("pdf") → PDF
     * FileType.fromExtension(".PDF") → PDF
     * FileType.fromExtension("txt") → null
     */
    public static FileType fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        
        // 转换为小写并确保有点号前缀
        String ext = extension.toLowerCase();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        
        // 遍历所有支持的类型进行匹配
        for (FileType type : values()) {
            if (type.extension.equals(ext)) {
                return type;
            }
        }
        
        return null;  // 不支持的格式
    }

    /**
     * 根据MIME类型获取文件类型
     * 
     * 这个方法用于HTTP请求处理时识别文件类型
     * 通过Content-Type头判断文件格式
     * 
     * @param mimeType MIME类型字符串
     * @return 对应的FileType，如果不支持则返回null
     * 
     * 使用示例：
     * FileType.fromMimeType("application/pdf") → PDF
     * FileType.fromMimeType("text/plain") → null
     */
    public static FileType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        
        // 精确匹配MIME类型
        for (FileType type : values()) {
            if (type.mimeType.equals(mimeType)) {
                return type;
            }
        }
        
        return null;  // 不支持的MIME类型
    }

    /**
     * 检查文件名是否为支持的格式
     * 
     * 这个方法用于文件上传前的格式验证
     * 避免用户上传不支持的文件格式
     * 
     * @param filename 完整的文件名
     * @return true表示支持，false表示不支持
     * 
     * 使用示例：
     * FileType.isSupported("document.pdf") → true
     * FileType.isSupported("image.jpg") → false
     * FileType.isSupported("Document.PDF") → true (大小写不敏感)
     */
    public static boolean isSupported(String filename) {
        if (filename == null) {
            return false;
        }
        
        // 转换为小写进行匹配
        String lower = filename.toLowerCase();
        
        // 检查是否以支持的扩展名结尾
        for (FileType type : values()) {
            if (lower.endsWith(type.extension)) {
                return true;
            }
        }
        
        return false;  // 不支持的文件格式
    }
    
    /*
     * 使用示例：
     * 
     * 1. 文件上传验证：
     * @PostMapping("/upload")
     * public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file) {
     *     if (!FileType.isSupported(file.getOriginalFilename())) {
     *         return ResponseEntity.badRequest()
     *             .body("不支持的文件格式，请上传PDF、DOCX或PPTX文件");
     *     }
     *     // 继续处理...
     * }
     * 
     * 2. 根据文件名确定处理方式：
     * FileType type = FileType.fromExtension(filename);
     * switch (type) {
     *     case PDF:
     *         return parsePdf(file);
     *     case DOCX:
     *         return parseDocx(file);
     *     case PPTX:
     *         return parsePptx(file);
     *     default:
     *         throw new UnsupportedFileTypeException();
     * }
     * 
     * 3. 前端文件选择器配置：
     * <input type="file" 
     *        accept=".pdf,.docx,.pptx"
     *        onChange={handleFileSelect} />
     * 
     * 4. 文件类型统计：
     * Map<FileType, Long> typeCount = documents.stream()
     *     .collect(Collectors.groupingBy(
     *         Document::getFileType,
     *         Collectors.counting()
     *     ));
     */
}
