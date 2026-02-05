package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.dto.teacher.AddStudentRequest;
import com.hiyuan.demo1.dto.teacher.BatchAddStudentResponse;
import com.hiyuan.demo1.dto.teacher.DocumentAccessResponse;
import com.hiyuan.demo1.dto.teacher.QaHistoryResponse;
import com.hiyuan.demo1.dto.teacher.StudentDetailResponse;
import com.hiyuan.demo1.dto.teacher.StudentListResponse;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.service.TeacherStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 教师学生管理控制器
 * 
 * 提供教师管理班级学生的REST API接口，包括：
 * - 获取学生列表（分页、搜索）
 * - 获取学生详情
 * - 添加学生到班级
 * - 批量添加学生
 * - 移除学生从班级
 * - 获取学生问答历史
 * - 获取学生文档访问记录
 * - 导出学生数据报表
 * 
 * 所有端点都需要TEACHER角色权限，通过@PreAuthorize注解在类级别进行控制。
 * 教师ID从JWT token中提取，确保教师只能访问自己班级的学生数据。
 * 
 * @author 开发团队
 * @version 1.0.0
 * 
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
 */
@Slf4j
@RestController
@RequestMapping("/teacher/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherStudentController {

    private final TeacherStudentService teacherStudentService;

    /**
     * 获取学生列表（分页）
     * 
     * GET /teacher/students
     * 
     * 支持按用户名或真实姓名搜索，支持分页。
     * 默认按加入班级时间降序排序（最近加入的学生优先）。
     * 
     * @param search 搜索关键词（可选，按用户名或真实姓名搜索）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认20）
     * @param authentication 认证信息（用于提取教师ID）
     * @return 分页的学生列表响应
     * 
     * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 10.2, 10.3
     */
    @GetMapping
    public ApiResponse<Page<StudentListResponse>> getStudentList(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求学生列表, 搜索: {}, 页码: {}, 每页: {}", teacherId, search, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentListResponse> students = teacherStudentService.getStudentList(teacherId, search, pageable);
        
        log.info("返回 {} 条学生记录, 共 {} 条", students.getNumberOfElements(), students.getTotalElements());
        return ApiResponse.success(students);
    }

    /**
     * 获取学生详情
     * 
     * GET /teacher/students/{id}
     * 
     * 返回学生的基本信息、统计数据和最近30天活动摘要。
     * 如果学生不属于该教师的班级，返回403错误。
     * 
     * @param id 学生用户ID
     * @param authentication 认证信息（用于提取教师ID）
     * @return 学生详情响应
     * 
     * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 10.2, 10.4
     */
    @GetMapping("/{id}")
    public ApiResponse<StudentDetailResponse> getStudentDetail(
            @PathVariable UUID id,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求学生详情: {}", teacherId, id);
        
        StudentDetailResponse detail = teacherStudentService.getStudentDetail(teacherId, id);
        
        log.info("成功返回学生 {} 的详情", id);
        return ApiResponse.success(detail);
    }

    /**
     * 添加学生到班级
     * 
     * POST /teacher/students
     * 
     * 通过用户名或邮箱添加学生到教师的班级。
     * 验证目标用户必须是学生角色，且不能重复添加。
     * 
     * @param request 添加学生请求（包含学生的用户名或邮箱）
     * @param authentication 认证信息（用于提取教师ID）
     * @return 成功响应
     * 
     * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 10.2
     */
    @PostMapping
    public ApiResponse<Void> addStudent(
            @RequestBody @Valid AddStudentRequest request,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求添加学生: {}", teacherId, request.getIdentifier());
        
        teacherStudentService.addStudent(teacherId, request.getIdentifier());
        
        log.info("成功添加学生 {} 到教师 {} 的班级", request.getIdentifier(), teacherId);
        return ApiResponse.success("学生添加成功", null);
    }

    /**
     * 批量添加学生到班级
     * 
     * POST /teacher/students/batch
     * 
     * 通过上传CSV或Excel文件批量添加学生。
     * 文件中每行包含一个学生标识符（用户名或邮箱）。
     * 已存在的关联会被跳过，无效的标识符会被记录但不影响其他学生的添加。
     * 
     * @param file 学生列表文件（CSV或Excel格式）
     * @param authentication 认证信息（用于提取教师ID）
     * @return 批量添加结果摘要
     * 
     * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 10.2
     */
    @PostMapping("/batch")
    public ApiResponse<BatchAddStudentResponse> batchAddStudents(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求批量添加学生, 文件名: {}", teacherId, file.getOriginalFilename());
        
        BatchAddStudentResponse result = teacherStudentService.batchAddStudents(teacherId, file);
        
        log.info("批量添加完成 - 成功: {}, 跳过: {}, 失败: {}", 
                result.getSuccessCount(), result.getSkippedCount(), result.getFailedCount());
        return ApiResponse.success(result);
    }

    /**
     * 从班级移除学生
     * 
     * DELETE /teacher/students/{id}
     * 
     * 删除教师与学生之间的关联关系。
     * 注意：此操作只删除关联记录，不会删除学生的用户账号或学习记录。
     * 
     * @param id 学生用户ID
     * @param authentication 认证信息（用于提取教师ID）
     * @return 成功响应
     * 
     * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 10.2
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeStudent(
            @PathVariable UUID id,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求移除学生: {}", teacherId, id);
        
        teacherStudentService.removeStudent(teacherId, id);
        
        log.info("成功从教师 {} 的班级移除学生 {}", teacherId, id);
        return ApiResponse.success("学生移除成功", null);
    }

    /**
     * 获取学生问答历史（分页）
     * 
     * GET /teacher/students/{id}/qa-history
     * 
     * 返回学生的问答历史记录，支持日期范围过滤和分页。
     * 默认按提问时间降序排序（最新的问答在前）。
     * 
     * @param id 学生用户ID
     * @param startDate 开始日期（可选，ISO格式）
     * @param endDate 结束日期（可选，ISO格式）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认20）
     * @param authentication 认证信息（用于提取教师ID）
     * @return 分页的问答历史响应
     * 
     * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 10.2, 10.4
     */
    @GetMapping("/{id}/qa-history")
    public ApiResponse<Page<QaHistoryResponse>> getStudentQaHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求学生 {} 的问答历史, 日期范围: {} - {}, 页码: {}, 每页: {}", 
                teacherId, id, startDate, endDate, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QaHistoryResponse> history = teacherStudentService.getStudentQaHistory(
                teacherId, id, startDate, endDate, pageable);
        
        log.info("返回 {} 条问答记录, 共 {} 条", history.getNumberOfElements(), history.getTotalElements());
        return ApiResponse.success(history);
    }

    /**
     * 获取学生文档访问记录（分页）
     * 
     * GET /teacher/students/{id}/document-access
     * 
     * 返回学生的文档访问记录，聚合同一文档的多次访问。
     * 默认按最后访问时间降序排序（最近访问的在前）。
     * 
     * @param id 学生用户ID
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认20）
     * @param authentication 认证信息（用于提取教师ID）
     * @return 分页的文档访问记录响应
     * 
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 10.2, 10.4
     */
    @GetMapping("/{id}/document-access")
    public ApiResponse<Page<DocumentAccessResponse>> getStudentDocumentAccess(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求学生 {} 的文档访问记录, 页码: {}, 每页: {}", teacherId, id, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentAccessResponse> access = teacherStudentService.getStudentDocumentAccess(
                teacherId, id, pageable);
        
        log.info("返回 {} 条文档访问记录, 共 {} 条", access.getNumberOfElements(), access.getTotalElements());
        return ApiResponse.success(access);
    }

    /**
     * 导出学生数据报表
     * 
     * POST /teacher/students/export
     * 
     * 生成并下载学生数据报表，支持CSV和Excel格式。
     * 报表包含学生基本信息、总提问数、总文档访问数和最后活动时间。
     * 支持日期范围过滤活动数据。
     * 
     * @param startDate 开始日期（可选，ISO格式，用于过滤活动数据）
     * @param endDate 结束日期（可选，ISO格式，用于过滤活动数据）
     * @param format 导出格式（csv或excel，默认csv）
     * @param authentication 认证信息（用于提取教师ID）
     * @return 报表文件响应
     * 
     * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 10.2
     */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportStudentReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "csv") String format,
            Authentication authentication) {
        
        UUID teacherId = extractTeacherId(authentication);
        log.info("教师 {} 请求导出学生报表, 日期范围: {} - {}, 格式: {}", teacherId, startDate, endDate, format);
        
        byte[] reportData = teacherStudentService.exportStudentReport(teacherId, startDate, endDate, format);
        
        // 生成文件名（包含时间戳）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "student_report_" + timestamp;
        
        // 根据格式设置Content-Type和文件扩展名
        String contentType;
        String extension;
        if ("excel".equalsIgnoreCase(format)) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            extension = ".xlsx";
        } else {
            contentType = "text/csv";
            extension = ".csv";
        }
        
        log.info("成功生成学生报表, 文件名: {}{}, 大小: {} 字节", filename, extension, reportData.length);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + extension + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(reportData);
    }

    /**
     * 从认证信息中提取教师ID
     * 
     * 从JWT token中解析出的UserPrincipal获取教师的用户ID。
     * 此方法确保所有操作都使用正确的教师身份进行权限验证。
     * 
     * @param authentication Spring Security认证信息
     * @return 教师用户ID
     * 
     * Requirements: 10.2
     */
    private UUID extractTeacherId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}
