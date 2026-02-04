package com.hiyuan.demo1.enums;

/**
 * 文档处理状态枚举
 * 
 * 这个枚举定义了文档在RAG系统中的各种处理状态
 * 用于跟踪文档从上传到可用于问答的完整生命周期
 * 
 * 状态流转图：
 * UPLOADING → PROCESSING → SUCCESS/FAILED/PARTIAL
 * 
 * 前端显示建议：
 * - UPLOADING: 蓝色进度条，显示"上传中..."
 * - PROCESSING: 黄色进度条，显示"处理中，预计30-50秒"
 * - SUCCESS: 绿色勾号，显示"处理完成"
 * - FAILED: 红色叉号，显示"处理失败"
 * - PARTIAL: 橙色警告，显示"部分成功"
 * 
 * @author 开发团队
 * @version 1.0.0
 */
public enum DocumentStatus {
    
    /**
     * 上传中
     * 
     * 状态说明：
     * - 用户刚刚提交文件，文件正在上传到服务器
     * - 此时文件可能还没有完全传输完成
     * - 数据库中已创建Document记录，但处理尚未开始
     * 
     * 持续时间：通常几秒到几分钟（取决于文件大小和网络速度）
     * 
     * 下一状态：PROCESSING（上传完成后自动转换）
     * 
     * 前端处理：
     * - 显示上传进度条
     * - 禁用相关操作按钮
     * - 提示用户不要关闭页面
     */
    UPLOADING,

    /**
     * 处理中（解析、分块、向量化）
     * 
     * 状态说明：
     * - 文件上传完成，后台正在异步处理
     * - 包含多个子步骤：文件解析 → 文本清洗 → 分块 → 向量化 → 存储
     * - 这是最耗时的阶段，需要调用外部API（HuggingFace）
     * 
     * 处理步骤详解：
     * 1. 文件解析：使用PDFBox/POI解析文档内容
     * 2. 文本清洗：去除特殊字符，规范化文本
     * 3. 文本分块：按1000字符分块，100字符重叠
     * 4. 向量化：调用HuggingFace API生成768维向量
     * 5. 数据存储：保存分块和向量到PostgreSQL
     * 
     * 持续时间：30-50秒（取决于文档大小和外部API响应速度）
     * 
     * 下一状态：SUCCESS/FAILED/PARTIAL
     * 
     * 前端处理：
     * - 显示处理进度和当前步骤
     * - 预估剩余时间
     * - 允许用户进行其他操作
     */
    PROCESSING,

    /**
     * 处理成功
     * 
     * 状态说明：
     * - 文档已完全处理完成，所有分块都成功向量化
     * - 文档可以用于RAG问答系统
     * - 所有数据已正确存储到数据库
     * 
     * 数据完整性：
     * - Document.chunkCount > 0
     * - DocumentChunk表中有对应记录
     * - VectorRecord表中有对应的向量记录
     * - Document.processedAt已设置
     * 
     * 前端处理：
     * - 显示绿色成功状态
     * - 显示分块数量统计
     * - 允许基于此文档进行问答
     * - 可以查看文档详情
     */
    SUCCESS,

    /**
     * 处理失败
     * 
     * 状态说明：
     * - 文档处理过程中发生不可恢复的错误
     * - 无法用于RAG问答系统
     * - 错误详情记录在Document.errorMessage中
     * 
     * 常见失败原因：
     * - 文件格式不支持或文件损坏
     * - 文件内容为空或无法提取文本
     * - 向量化服务异常（HuggingFace API错误）
     * - 数据库存储异常
     * - 系统资源不足
     * 
     * 数据状态：
     * - Document.errorMessage包含错误详情
     * - Document.processedAt已设置
     * - 可能有部分DocumentChunk记录（取决于失败阶段）
     * 
     * 前端处理：
     * - 显示红色错误状态
     * - 显示错误信息给用户
     * - 提供重新上传选项
     * - 记录错误日志用于分析
     */
    FAILED,

    /**
     * 部分成功（部分分块处理失败）
     * 
     * 状态说明：
     * - 文档大部分内容处理成功，但有少量分块失败
     * - 可以用于RAG问答，但可能缺少部分内容
     * - 这是一种降级处理策略，保证系统可用性
     * 
     * 典型场景：
     * - 文档中有特殊格式内容无法解析
     * - 部分分块向量化失败（网络超时等）
     * - 部分分块存储失败
     * - 文档过大，部分内容被截断
     * 
     * 数据状态：
     * - Document.chunkCount > 0（但可能小于预期）
     * - 部分DocumentChunk和VectorRecord记录存在
     * - Document.errorMessage包含部分失败的详情
     * - Document.processedAt已设置
     * 
     * 前端处理：
     * - 显示橙色警告状态
     * - 提示用户"部分内容可能缺失"
     * - 允许基于此文档进行问答
     * - 显示成功/失败的分块统计
     * - 提供重新处理选项
     */
    PARTIAL
    
    /*
     * 使用示例：
     * 
     * 1. 状态检查：
     * if (document.getStatus() == DocumentStatus.SUCCESS) {
     *     // 文档可以用于问答
     *     enableQaFeature(document);
     * }
     * 
     * 2. 前端状态显示：
     * switch (document.getStatus()) {
     *     case UPLOADING:
     *         return "上传中...";
     *     case PROCESSING:
     *         return "处理中，预计30-50秒";
     *     case SUCCESS:
     *         return "处理完成";
     *     case FAILED:
     *         return "处理失败：" + document.getErrorMessage();
     *     case PARTIAL:
     *         return "部分成功，可能缺少部分内容";
     * }
     * 
     * 3. 数据库查询：
     * // 查询可用于问答的文档
     * List<Document> availableDocs = documentRepository
     *     .findByUserAndStatusIn(user, Arrays.asList(
     *         DocumentStatus.SUCCESS, 
     *         DocumentStatus.PARTIAL
     *     ));
     * 
     * 4. 状态统计：
     * Map<DocumentStatus, Long> statusCount = documents.stream()
     *     .collect(Collectors.groupingBy(
     *         Document::getStatus, 
     *         Collectors.counting()
     *     ));
     */
}
