package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.teacher.BatchAddStudentResponse;
import com.hiyuan.demo1.dto.teacher.BatchError;
import com.hiyuan.demo1.dto.teacher.DocumentAccessResponse;
import com.hiyuan.demo1.dto.teacher.QaHistoryResponse;
import com.hiyuan.demo1.dto.teacher.RecentActivitySummary;
import com.hiyuan.demo1.dto.teacher.StudentDetailResponse;
import com.hiyuan.demo1.dto.teacher.StudentListResponse;
import com.hiyuan.demo1.dto.teacher.StudentReportData;
import com.hiyuan.demo1.entity.ClassAssociation;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.exception.AuthorizationException;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.ClassAssociationRepository;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.util.StudentFileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 教师学生管理服务
 * 
 * 提供教师管理班级学生的核心业务逻辑，包括：
 * - 添加学生到班级
 * - 移除学生从班级
 * - 查询学生列表和详情
 * - 查询学生学习记录
 * - 导出学生数据报表
 * 
 * 所有操作都会验证教师的权限，确保教师只能访问自己班级的学生数据。
 * 
 * @author 开发团队
 * @version 1.0.0
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherStudentService {

    private final ClassAssociationRepository classAssociationRepository;
    private final UserRepository userRepository;
    private final QaHistoryRepository qaHistoryRepository;
    private final StudentFileParser studentFileParser;

    /**
     * 添加学生到班级
     * 
     * 业务流程：
     * 1. 根据用户名或邮箱查找目标用户
     * 2. 验证目标用户角色为STUDENT
     * 3. 检查关联是否已存在（防止重复添加）
     * 4. 创建ClassAssociation记录
     * 
     * @param teacherId 教师用户ID
     * @param identifier 学生的用户名或邮箱
     * @throws BusinessException 当用户不存在、不是学生角色或已在班级中时抛出
     * 
     * 使用示例：
     * teacherStudentService.addStudent(teacherId, "student@example.com");
     * teacherStudentService.addStudent(teacherId, "studentUsername");
     * 
     * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
     */
    @Transactional
    public void addStudent(UUID teacherId, String identifier) {
        log.info("教师 {} 尝试添加学生: {}", teacherId, identifier);

        // 1. 查找目标用户（通过用户名或邮箱）
        User targetUser = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", identifier);
                    return BusinessException.notFound("用户", identifier);
                });

        // 2. 验证角色 - 只能添加学生角色的用户
        if (targetUser.getRole() != UserRole.STUDENT) {
            log.warn("目标用户 {} 不是学生角色，当前角色: {}", identifier, targetUser.getRole());
            throw new BusinessException("目标用户不是学生角色");
        }

        // 3. 检查重复 - 防止重复添加同一学生
        if (classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, targetUser.getId())) {
            log.warn("学生 {} 已在教师 {} 的班级中", targetUser.getUsername(), teacherId);
            throw new BusinessException("该学生已在您的班级中");
        }

        // 4. 创建关联记录
        ClassAssociation association = ClassAssociation.builder()
                .teacher(userRepository.getReferenceById(teacherId))
                .student(targetUser)
                .build();
        
        classAssociationRepository.save(association);
        
        log.info("成功添加学生 {} 到教师 {} 的班级", targetUser.getUsername(), teacherId);
    }

    /**
     * 批量添加学生到班级
     * 
     * 业务流程：
     * 1. 使用StudentFileParser解析上传的文件（支持CSV和Excel格式）
     * 2. 对每个学生标识符：
     *    - 查找用户（通过用户名或邮箱）
     *    - 验证用户角色为STUDENT
     *    - 检查关联是否已存在（已存在则跳过）
     *    - 创建ClassAssociation记录
     * 3. 即使部分条目失败，也继续处理剩余条目
     * 4. 返回详细的处理结果摘要
     * 
     * @param teacherId 教师用户ID
     * @param file 上传的学生列表文件（CSV或Excel格式）
     * @return 批量添加结果摘要，包含成功、跳过和失败的详细信息
     * @throws BusinessException 当文件为空或格式不支持时抛出
     * 
     * 使用示例：
     * BatchAddStudentResponse result = teacherStudentService.batchAddStudents(teacherId, file);
     * System.out.println("成功: " + result.getSuccessCount());
     * System.out.println("跳过: " + result.getSkippedCount());
     * System.out.println("失败: " + result.getFailedCount());
     * 
     * Requirements: 7.2, 7.3, 7.4, 7.5
     */
    @Transactional
    public BatchAddStudentResponse batchAddStudents(UUID teacherId, MultipartFile file) {
        log.info("教师 {} 开始批量添加学生, 文件名: {}", teacherId, file.getOriginalFilename());

        // 1. 解析文件获取学生标识符列表
        List<String> identifiers = studentFileParser.parseStudentFile(file);
        
        log.info("从文件中解析出 {} 个学生标识符", identifiers.size());

        // 2. 初始化结果列表
        List<String> successList = new ArrayList<>();
        List<String> skippedList = new ArrayList<>();
        List<BatchError> failedList = new ArrayList<>();

        // 3. 获取教师引用（用于创建关联）
        User teacher = userRepository.getReferenceById(teacherId);

        // 4. 逐个处理学生标识符
        for (String identifier : identifiers) {
            try {
                processStudentIdentifier(teacherId, teacher, identifier, successList, skippedList, failedList);
            } catch (Exception e) {
                // 捕获所有异常，确保继续处理剩余学生
                log.error("处理学生标识符 {} 时发生未预期的错误: {}", identifier, e.getMessage());
                failedList.add(BatchError.builder()
                        .identifier(identifier)
                        .reason("处理失败: " + e.getMessage())
                        .build());
            }
        }

        // 5. 构建并返回结果摘要
        BatchAddStudentResponse response = BatchAddStudentResponse.builder()
                .totalProcessed(identifiers.size())
                .successCount(successList.size())
                .skippedCount(skippedList.size())
                .failedCount(failedList.size())
                .successList(successList)
                .skippedList(skippedList)
                .failedList(failedList)
                .build();

        log.info("批量添加学生完成 - 总数: {}, 成功: {}, 跳过: {}, 失败: {}", 
                response.getTotalProcessed(), 
                response.getSuccessCount(), 
                response.getSkippedCount(), 
                response.getFailedCount());

        return response;
    }

    /**
     * 处理单个学生标识符
     * 
     * 业务逻辑：
     * 1. 查找用户（通过用户名或邮箱）
     * 2. 验证用户角色为STUDENT
     * 3. 检查关联是否已存在
     * 4. 创建ClassAssociation记录
     * 
     * @param teacherId 教师ID
     * @param teacher 教师用户引用
     * @param identifier 学生标识符（用户名或邮箱）
     * @param successList 成功列表（用于记录成功添加的标识符）
     * @param skippedList 跳过列表（用于记录已存在关联的标识符）
     * @param failedList 失败列表（用于记录失败的标识符和原因）
     */
    private void processStudentIdentifier(UUID teacherId, User teacher, String identifier,
                                          List<String> successList, List<String> skippedList,
                                          List<BatchError> failedList) {
        // 1. 查找目标用户（通过用户名或邮箱）
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(identifier, identifier);
        
        if (userOptional.isEmpty()) {
            log.debug("用户不存在: {}", identifier);
            failedList.add(BatchError.builder()
                    .identifier(identifier)
                    .reason("用户不存在")
                    .build());
            return;
        }

        User targetUser = userOptional.get();

        // 2. 验证角色 - 只能添加学生角色的用户
        if (targetUser.getRole() != UserRole.STUDENT) {
            log.debug("目标用户 {} 不是学生角色，当前角色: {}", identifier, targetUser.getRole());
            failedList.add(BatchError.builder()
                    .identifier(identifier)
                    .reason("目标用户不是学生角色")
                    .build());
            return;
        }

        // 3. 检查关联是否已存在 - 已存在则跳过
        if (classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, targetUser.getId())) {
            log.debug("学生 {} 已在教师 {} 的班级中，跳过", identifier, teacherId);
            skippedList.add(identifier);
            return;
        }

        // 4. 创建关联记录
        ClassAssociation association = ClassAssociation.builder()
                .teacher(teacher)
                .student(targetUser)
                .build();
        
        classAssociationRepository.save(association);
        successList.add(identifier);
        
        log.debug("成功添加学生 {} 到教师 {} 的班级", identifier, teacherId);
    }

    /**
     * 从班级移除学生
     * 
     * 业务流程：
     * 1. 验证教师与学生之间的关联是否存在
     * 2. 删除ClassAssociation记录
     * 
     * 重要说明：
     * - 此方法只删除关联记录，不会删除学生的User账号
     * - 学生的学习记录（QaHistory等）也会保留
     * - 学生可以被其他教师重新添加到班级
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * @throws BusinessException 当关联不存在时抛出
     * 
     * 使用示例：
     * teacherStudentService.removeStudent(teacherId, studentId);
     * 
     * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
     */
    @Transactional
    public void removeStudent(UUID teacherId, UUID studentId) {
        log.info("教师 {} 尝试移除学生: {}", teacherId, studentId);

        // 1. 验证关联存在
        if (!classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, studentId)) {
            log.warn("关联不存在: 教师 {} 与学生 {}", teacherId, studentId);
            throw new BusinessException("关联不存在");
        }

        // 2. 删除关联记录（不会删除用户账号或学习记录）
        classAssociationRepository.deleteByTeacherIdAndStudentId(teacherId, studentId);
        
        log.info("成功从教师 {} 的班级移除学生 {}", teacherId, studentId);
    }

    /**
     * 获取教师的学生列表（分页）
     * 
     * 业务流程：
     * 1. 通过ClassAssociation过滤教师的学生
     * 2. 支持按用户名或真实姓名搜索（大小写不敏感）
     * 3. 支持分页和排序（默认按enrolledAt降序）
     * 4. 聚合统计数据（总提问数、最后活动时间）
     * 
     * @param teacherId 教师用户ID
     * @param search 搜索关键词（可选，按用户名或真实姓名搜索）
     * @param pageable 分页参数
     * @return 分页的学生列表响应
     * 
     * 使用示例：
     * Pageable pageable = PageRequest.of(0, 20);
     * Page<StudentListResponse> students = teacherStudentService.getStudentList(teacherId, "张", pageable);
     * 
     * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
     */
    @Transactional(readOnly = true)
    public Page<StudentListResponse> getStudentList(UUID teacherId, String search, Pageable pageable) {
        log.info("教师 {} 查询学生列表, 搜索关键词: {}, 分页: {}", teacherId, search, pageable);

        // 1. 确保默认排序为enrolledAt降序（最近加入的学生优先）
        Pageable sortedPageable = ensureDefaultSort(pageable);

        // 2. 获取教师的所有学生关联（分页）
        Page<ClassAssociation> associationPage = classAssociationRepository.findByTeacherId(teacherId, sortedPageable);

        // 3. 转换为StudentListResponse并聚合统计数据
        List<StudentListResponse> studentResponses = associationPage.getContent().stream()
                .map(association -> {
                    User student = association.getStudent();
                    
                    // 如果有搜索条件，过滤不匹配的学生
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase().trim();
                        String username = student.getUsername() != null ? student.getUsername().toLowerCase() : "";
                        // 注意：当前User实体没有realName字段，使用username作为替代
                        // 如果将来添加realName字段，需要在此处添加对realName的搜索
                        if (!username.contains(searchLower)) {
                            return null; // 不匹配，将被过滤
                        }
                    }
                    
                    return buildStudentListResponse(association, student);
                })
                .filter(response -> response != null) // 过滤掉不匹配搜索条件的学生
                .collect(Collectors.toList());

        // 4. 如果有搜索条件，需要重新计算分页信息
        // 注意：这种方式在大数据量时效率不高，建议后续优化为数据库层面的搜索
        if (search != null && !search.trim().isEmpty()) {
            // 获取所有匹配的学生数量（用于分页）
            long totalMatchingStudents = countMatchingStudents(teacherId, search);
            return new PageImpl<>(studentResponses, sortedPageable, totalMatchingStudents);
        }

        return new PageImpl<>(studentResponses, sortedPageable, associationPage.getTotalElements());
    }

    /**
     * 确保分页参数包含默认排序（enrolledAt降序）
     * 
     * @param pageable 原始分页参数
     * @return 包含默认排序的分页参数
     */
    private Pageable ensureDefaultSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "enrolledAt")
            );
        }
        return pageable;
    }

    /**
     * 构建学生列表响应对象
     * 
     * @param association 班级关联记录
     * @param student 学生用户
     * @return 学生列表响应
     */
    private StudentListResponse buildStudentListResponse(ClassAssociation association, User student) {
        UUID studentId = student.getId();
        
        // 获取总提问数
        long totalQuestions = qaHistoryRepository.countByUserId(studentId);
        
        // 获取最后活动时间
        LocalDateTime lastActivity = qaHistoryRepository.findLastActivityByUserId(studentId)
                .orElse(null);

        return StudentListResponse.builder()
                .id(studentId)
                .username(student.getUsername())
                .email(student.getEmail())
                .realName(null) // 当前User实体没有realName字段，设为null
                .enrolledAt(association.getEnrolledAt())
                .totalQuestions(totalQuestions)
                .lastActivity(lastActivity)
                .build();
    }

    /**
     * 统计匹配搜索条件的学生数量
     * 
     * @param teacherId 教师ID
     * @param search 搜索关键词
     * @return 匹配的学生数量
     */
    private long countMatchingStudents(UUID teacherId, String search) {
        String searchLower = search.toLowerCase().trim();
        
        return classAssociationRepository.findByTeacherId(teacherId).stream()
                .filter(association -> {
                    User student = association.getStudent();
                    String username = student.getUsername() != null ? student.getUsername().toLowerCase() : "";
                    return username.contains(searchLower);
                })
                .count();
    }

    /**
     * 获取学生详情
     * 
     * 业务流程：
     * 1. 验证教师有权访问该学生（通过ClassAssociation检查）
     * 2. 获取学生基本信息
     * 3. 计算统计数据（总提问数、文档访问数）
     * 4. 计算最近30天活动摘要
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * @return 学生详情响应
     * @throws AuthorizationException 当教师无权访问该学生时抛出
     * @throws BusinessException 当学生不存在时抛出
     * 
     * 使用示例：
     * StudentDetailResponse detail = teacherStudentService.getStudentDetail(teacherId, studentId);
     * 
     * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
     */
    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetail(UUID teacherId, UUID studentId) {
        log.info("教师 {} 查询学生详情: {}", teacherId, studentId);

        // 1. 验证教师有权访问该学生
        ClassAssociation association = classAssociationRepository.findByTeacherIdAndStudentId(teacherId, studentId)
                .orElseThrow(() -> {
                    log.warn("教师 {} 无权访问学生 {}", teacherId, studentId);
                    return new AuthorizationException("无权限访问该学生");
                });

        // 2. 获取学生信息
        User student = association.getStudent();

        // 3. 计算统计数据
        long totalQuestions = qaHistoryRepository.countByUserId(studentId);
        
        // 文档访问数：当前没有文档访问记录表，设为0
        // 注意：如果将来添加文档访问记录表，需要在此处更新统计逻辑
        long totalDocumentAccesses = 0L;

        // 4. 计算最近30天活动摘要
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        long questionsLast30Days = qaHistoryRepository.countByUserIdAndAskedAtAfter(studentId, thirtyDaysAgo);
        
        // 文档访问数（最近30天）：当前没有文档访问记录表，设为0
        long documentAccessesLast30Days = 0L;
        
        // 获取最后提问时间
        LocalDateTime lastQuestionAt = qaHistoryRepository.findLastActivityByUserId(studentId)
                .orElse(null);
        
        // 最后文档访问时间：当前没有文档访问记录表，设为null
        LocalDateTime lastDocumentAccessAt = null;

        RecentActivitySummary recentActivity = RecentActivitySummary.builder()
                .questionsLast30Days(questionsLast30Days)
                .documentAccessesLast30Days(documentAccessesLast30Days)
                .lastQuestionAt(lastQuestionAt)
                .lastDocumentAccessAt(lastDocumentAccessAt)
                .build();

        // 5. 构建并返回响应
        StudentDetailResponse response = StudentDetailResponse.builder()
                .id(studentId)
                .username(student.getUsername())
                .email(student.getEmail())
                .realName(null) // 当前User实体没有realName字段，设为null
                .registeredAt(student.getCreatedAt()) // 使用createdAt作为注册时间
                .enrolledAt(association.getEnrolledAt())
                .totalQuestions(totalQuestions)
                .totalDocumentAccesses(totalDocumentAccesses)
                .recentActivity(recentActivity)
                .build();

        log.info("成功获取学生 {} 的详情", studentId);
        return response;
    }

    /**
     * 获取学生问答历史（分页）
     * 
     * 业务流程：
     * 1. 验证教师有权访问该学生（通过ClassAssociation检查）
     * 2. 支持日期范围过滤（startDate和endDate参数）
     * 3. 支持分页和排序（默认按askedAt降序，最新的在前）
     * 4. 返回问题文本、答案文本和时间戳
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * @param startDate 开始日期（可选，包含）
     * @param endDate 结束日期（可选，包含）
     * @param pageable 分页参数
     * @return 分页的问答历史响应
     * @throws AuthorizationException 当教师无权访问该学生时抛出
     * 
     * 使用示例：
     * Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "askedAt"));
     * Page<QaHistoryResponse> history = teacherStudentService.getStudentQaHistory(
     *     teacherId, studentId, startDate, endDate, pageable);
     * 
     * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
     */
    @Transactional(readOnly = true)
    public Page<QaHistoryResponse> getStudentQaHistory(UUID teacherId, UUID studentId,
                                                        LocalDateTime startDate, LocalDateTime endDate,
                                                        Pageable pageable) {
        log.info("教师 {} 查询学生 {} 的问答历史, 日期范围: {} - {}, 分页: {}", 
                teacherId, studentId, startDate, endDate, pageable);

        // 1. 验证教师有权访问该学生
        verifyTeacherStudentAccess(teacherId, studentId);

        // 2. 确保默认排序为askedAt降序（最新的问答在前）
        Pageable sortedPageable = ensureQaHistoryDefaultSort(pageable);

        // 3. 查询问答历史（支持日期范围过滤）
        Page<com.hiyuan.demo1.entity.QaHistory> qaHistoryPage = qaHistoryRepository.findByUserIdAndDateRange(
                studentId, startDate, endDate, sortedPageable);

        // 4. 转换为QaHistoryResponse
        Page<QaHistoryResponse> responsePage = qaHistoryPage.map(qaHistory -> 
                QaHistoryResponse.builder()
                        .id(qaHistory.getId())
                        .question(qaHistory.getQuestion())
                        .answer(qaHistory.getAnswer())
                        .askedAt(qaHistory.getAskedAt())
                        .build()
        );

        log.info("成功获取学生 {} 的问答历史, 共 {} 条记录", studentId, responsePage.getTotalElements());
        return responsePage;
    }

    /**
     * 验证教师是否有权访问学生
     * 
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @throws AuthorizationException 当教师无权访问该学生时抛出
     */
    private void verifyTeacherStudentAccess(UUID teacherId, UUID studentId) {
        if (!classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, studentId)) {
            log.warn("教师 {} 无权访问学生 {}", teacherId, studentId);
            throw new AuthorizationException("无权限访问该学生");
        }
    }

    /**
     * 确保问答历史分页参数包含默认排序（askedAt降序）
     * 
     * @param pageable 原始分页参数
     * @return 包含默认排序的分页参数
     */
    private Pageable ensureQaHistoryDefaultSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "askedAt")
            );
        }
        return pageable;
    }

    /**
     * 获取学生文档访问记录（分页）
     * 
     * 业务流程：
     * 1. 验证教师有权访问该学生（通过ClassAssociation检查）
     * 2. 聚合同一文档的多次访问
     * 3. 支持分页和排序（默认按lastAccessAt降序，最近访问的在前）
     * 4. 返回文档标题、访问次数和最后访问时间
     * 
     * TODO: 当前系统没有文档访问记录跟踪表，此方法返回空页面。
     * 需要实现以下功能：
     * 1. 创建文档访问记录表（t_document_access）
     * 2. 在用户访问文档时记录访问日志
     * 3. 实现聚合查询，按文档分组统计访问次数和最后访问时间
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * @param pageable 分页参数
     * @return 分页的文档访问记录响应
     * @throws AuthorizationException 当教师无权访问该学生时抛出
     * 
     * 使用示例：
     * Pageable pageable = PageRequest.of(0, 20);
     * Page<DocumentAccessResponse> access = teacherStudentService.getStudentDocumentAccess(
     *     teacherId, studentId, pageable);
     * 
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
     */
    @Transactional(readOnly = true)
    public Page<DocumentAccessResponse> getStudentDocumentAccess(UUID teacherId, UUID studentId,
                                                                   Pageable pageable) {
        log.info("教师 {} 查询学生 {} 的文档访问记录, 分页: {}", teacherId, studentId, pageable);

        // 1. 验证教师有权访问该学生
        verifyTeacherStudentAccess(teacherId, studentId);

        // 2. 确保默认排序为lastAccessAt降序（最近访问的在前）
        Pageable sortedPageable = ensureDocumentAccessDefaultSort(pageable);

        // TODO: 当前系统没有文档访问记录跟踪表，返回空页面
        // 需要实现以下功能：
        // 1. 创建文档访问记录表（t_document_access），包含字段：
        //    - id: UUID 主键
        //    - user_id: UUID 用户ID（外键关联t_user）
        //    - document_id: UUID 文档ID（外键关联t_document）
        //    - accessed_at: TIMESTAMP 访问时间
        // 2. 在用户访问文档时（如查看文档、下载文档）记录访问日志
        // 3. 实现聚合查询，按文档分组统计：
        //    - 访问次数（COUNT）
        //    - 最后访问时间（MAX(accessed_at)）
        // 4. 按最后访问时间降序排序
        
        log.info("文档访问记录查询完成（当前返回空页面，待实现文档访问跟踪功能）");
        
        return new PageImpl<>(
                java.util.Collections.emptyList(),
                sortedPageable,
                0L
        );
    }

    /**
     * 确保文档访问记录分页参数包含默认排序（lastAccessAt降序）
     * 
     * @param pageable 原始分页参数
     * @return 包含默认排序的分页参数
     */
    private Pageable ensureDocumentAccessDefaultSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "lastAccessAt")
            );
        }
        return pageable;
    }

    /**
     * 导出学生数据报表
     * 
     * 业务流程：
     * 1. 获取教师的所有学生（通过ClassAssociation过滤）
     * 2. 为每个学生构建报表数据（支持日期范围过滤活动数据）
     * 3. 根据format参数生成CSV或Excel格式的报表
     * 4. 返回报表文件的字节数组
     * 
     * 报表包含字段：
     * - username: 用户名
     * - email: 邮箱
     * - realName: 真实姓名
     * - totalQuestions: 总提问数（如果指定日期范围，则只统计该范围内的数据）
     * - totalDocumentAccesses: 总文档访问数（如果指定日期范围，则只统计该范围内的数据）
     * - lastActivity: 最后活动时间
     * 
     * @param teacherId 教师用户ID
     * @param startDate 开始日期（可选，用于过滤活动数据）
     * @param endDate 结束日期（可选，用于过滤活动数据）
     * @param format 导出格式，支持 "csv" 或 "excel"，默认为 "csv"
     * @return 报表文件的字节数组
     * @throws BusinessException 当生成报表失败时抛出
     * 
     * 使用示例：
     * // 导出CSV格式报表（全部数据）
     * byte[] csvReport = teacherStudentService.exportStudentReport(teacherId, null, null, "csv");
     * 
     * // 导出Excel格式报表（指定日期范围）
     * byte[] excelReport = teacherStudentService.exportStudentReport(
     *     teacherId, 
     *     LocalDateTime.of(2024, 1, 1, 0, 0), 
     *     LocalDateTime.of(2024, 12, 31, 23, 59), 
     *     "excel"
     * );
     * 
     * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5
     */
    @Transactional(readOnly = true)
    public byte[] exportStudentReport(UUID teacherId, LocalDateTime startDate, LocalDateTime endDate, String format) {
        log.info("教师 {} 导出学生报表, 日期范围: {} - {}, 格式: {}", teacherId, startDate, endDate, format);

        // 1. 获取教师的所有学生关联
        List<ClassAssociation> associations = classAssociationRepository.findByTeacherId(teacherId);
        
        log.info("找到 {} 个学生关联", associations.size());

        // 2. 为每个学生构建报表数据
        List<StudentReportData> reportData = associations.stream()
                .map(association -> buildStudentReportData(association.getStudent(), startDate, endDate))
                .collect(Collectors.toList());

        // 3. 根据格式生成报表
        byte[] reportBytes;
        if ("excel".equalsIgnoreCase(format)) {
            reportBytes = generateExcelReport(reportData);
        } else {
            reportBytes = generateCsvReport(reportData);
        }

        log.info("成功生成学生报表, 格式: {}, 大小: {} 字节", format, reportBytes.length);
        return reportBytes;
    }

    /**
     * 构建单个学生的报表数据
     * 
     * @param student 学生用户
     * @param startDate 开始日期（可选，用于过滤活动数据）
     * @param endDate 结束日期（可选，用于过滤活动数据）
     * @return 学生报表数据
     */
    private StudentReportData buildStudentReportData(User student, LocalDateTime startDate, LocalDateTime endDate) {
        UUID studentId = student.getId();
        
        // 计算总提问数（支持日期范围过滤）
        long totalQuestions;
        if (startDate != null || endDate != null) {
            totalQuestions = qaHistoryRepository.countByUserIdAndDateRange(studentId, startDate, endDate);
        } else {
            totalQuestions = qaHistoryRepository.countByUserId(studentId);
        }
        
        // 文档访问数：当前没有文档访问记录表，设为0
        // TODO: 如果将来添加文档访问记录表，需要在此处更新统计逻辑
        long totalDocumentAccesses = 0L;
        
        // 获取最后活动时间
        LocalDateTime lastActivity = qaHistoryRepository.findLastActivityByUserId(studentId)
                .orElse(null);

        return StudentReportData.builder()
                .username(student.getUsername())
                .email(student.getEmail())
                .realName(null) // 当前User实体没有realName字段，设为null
                .totalQuestions(totalQuestions)
                .totalDocumentAccesses(totalDocumentAccesses)
                .lastActivity(lastActivity)
                .build();
    }

    /**
     * 生成CSV格式报表
     * 
     * CSV格式说明：
     * - 使用UTF-8编码（带BOM，确保Excel正确识别中文）
     * - 使用逗号作为分隔符
     * - 字段值包含逗号、引号或换行符时，使用双引号包裹
     * - 字段值中的双引号使用两个双引号转义
     * 
     * @param reportData 学生报表数据列表
     * @return CSV文件的字节数组
     */
    private byte[] generateCsvReport(List<StudentReportData> reportData) {
        log.debug("生成CSV格式报表, 学生数量: {}", reportData.size());
        
        StringBuilder csv = new StringBuilder();
        
        // 添加UTF-8 BOM，确保Excel正确识别中文
        csv.append('\uFEFF');
        
        // 添加表头
        csv.append("用户名,邮箱,真实姓名,总提问数,总文档访问数,最后活动时间\n");
        
        // 日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 添加数据行
        for (StudentReportData data : reportData) {
            csv.append(escapeCsvField(data.getUsername())).append(",");
            csv.append(escapeCsvField(data.getEmail())).append(",");
            csv.append(escapeCsvField(data.getRealName())).append(",");
            csv.append(data.getTotalQuestions() != null ? data.getTotalQuestions() : 0).append(",");
            csv.append(data.getTotalDocumentAccesses() != null ? data.getTotalDocumentAccesses() : 0).append(",");
            csv.append(data.getLastActivity() != null ? data.getLastActivity().format(formatter) : "").append("\n");
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 转义CSV字段值
     * 
     * 如果字段值包含逗号、引号或换行符，则使用双引号包裹，
     * 并将字段值中的双引号替换为两个双引号。
     * 
     * @param field 字段值
     * @return 转义后的字段值
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        // 如果字段包含逗号、引号或换行符，需要用双引号包裹
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // 将双引号替换为两个双引号
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return field;
    }

    /**
     * 生成Excel格式报表
     * 
     * Excel格式说明：
     * - 使用Apache POI生成XLSX格式（Office 2007+）
     * - 第一行为表头，使用粗体样式
     * - 自动调整列宽以适应内容
     * 
     * @param reportData 学生报表数据列表
     * @return Excel文件的字节数组
     * @throws BusinessException 当生成Excel失败时抛出
     */
    private byte[] generateExcelReport(List<StudentReportData> reportData) {
        log.debug("生成Excel格式报表, 学生数量: {}", reportData.size());
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("学生数据报表");
            
            // 创建表头样式（粗体）
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户名", "邮箱", "真实姓名", "总提问数", "总文档访问数", "最后活动时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 日期时间格式化器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // 添加数据行
            int rowNum = 1;
            for (StudentReportData data : reportData) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(data.getUsername() != null ? data.getUsername() : "");
                row.createCell(1).setCellValue(data.getEmail() != null ? data.getEmail() : "");
                row.createCell(2).setCellValue(data.getRealName() != null ? data.getRealName() : "");
                row.createCell(3).setCellValue(data.getTotalQuestions() != null ? data.getTotalQuestions() : 0);
                row.createCell(4).setCellValue(data.getTotalDocumentAccesses() != null ? data.getTotalDocumentAccesses() : 0);
                row.createCell(5).setCellValue(data.getLastActivity() != null ? data.getLastActivity().format(formatter) : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入输出流
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成Excel报表失败: {}", e.getMessage(), e);
            throw new BusinessException("生成Excel报表失败: " + e.getMessage());
        }
    }
}
