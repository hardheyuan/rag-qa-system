# Design Document: Teacher Student Management

## Overview

本设计文档描述了教育AI系统中教师端学生管理功能的技术实现方案。该功能允许教师管理自己班级的学生，查看学生的学习数据和问答历史，并生成学生数据报表。

### 核心目标

1. 建立教师-学生关联关系，支持教师管理自己的班级
2. 提供学生列表查询、详情查询、问答历史和文档访问记录查询
3. 支持单个和批量添加学生到班级
4. 支持移除学生从班级（不删除学生账号）
5. 支持导出学生数据报表
6. 确保数据隔离和权限控制，教师只能访问自己班级的学生

### 技术栈

- 后端：Spring Boot 3.x, Spring Security, Spring Data JPA
- 数据库：PostgreSQL 15+
- 前端：Vue 3, Pinia, Vue Router, Tailwind CSS
- 认证：JWT Token

## Architecture

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (Vue 3)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Student List │  │Student Detail│  │  Add Student │      │
│  │     View     │  │     View     │  │    Modal     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              TeacherStudentController                 │   │
│  │  - GET /teacher/students                             │   │
│  │  - GET /teacher/students/{id}                        │   │
│  │  - POST /teacher/students                            │   │
│  │  - DELETE /teacher/students/{id}                     │   │
│  │  - GET /teacher/students/{id}/qa-history             │   │
│  │  - GET /teacher/students/{id}/document-access        │   │
│  │  - POST /teacher/students/export                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           TeacherStudentService                      │   │
│  │  - Business logic for student management             │   │
│  │  - Authorization checks                              │   │
│  │  - Data aggregation and statistics                   │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Repository Layer                        │   │
│  │  - ClassAssociationRepository                        │   │
│  │  - UserRepository                                    │   │
│  │  - QaHistoryRepository                               │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                       │
│  - t_user (existing)                                         │
│  - t_class_association (new)                                 │
│  - t_qa_history (existing)                                   │
│  - t_document (existing)                                     │
└─────────────────────────────────────────────────────────────┘
```

### 数据流

1. **学生列表查询流程**：
   - 前端发送GET请求到 `/teacher/students`
   - JWT Filter提取教师ID
   - Controller调用Service查询学生列表
   - Service通过ClassAssociation表过滤教师的学生
   - 返回学生基本信息和统计数据

2. **添加学生流程**：
   - 前端发送POST请求到 `/teacher/students`
   - Service验证目标用户是否为学生角色
   - Service检查关联是否已存在
   - 创建ClassAssociation记录
   - 返回成功响应

3. **权限控制流程**：
   - 所有请求通过JWT Filter验证身份
   - Service层验证教师是否有权访问目标学生
   - 通过ClassAssociation表检查关联关系
   - 未授权访问返回403错误


## Components and Interfaces

### Backend Components

#### 1. Entity: ClassAssociation

新增实体类，表示教师-学生关联关系。

```java
@Entity
@Table(name = "t_class_association", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassAssociation extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column(name = "enrolled_at")
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();
}
```

#### 2. Repository: ClassAssociationRepository

```java
public interface ClassAssociationRepository extends JpaRepository<ClassAssociation, UUID> {
    
    // 查询教师的所有学生关联
    List<ClassAssociation> findByTeacherId(UUID teacherId);
    
    // 查询教师的所有学生关联（分页）
    Page<ClassAssociation> findByTeacherId(UUID teacherId, Pageable pageable);
    
    // 检查关联是否存在
    boolean existsByTeacherIdAndStudentId(UUID teacherId, UUID studentId);
    
    // 查找特定关联
    Optional<ClassAssociation> findByTeacherIdAndStudentId(UUID teacherId, UUID studentId);
    
    // 删除特定关联
    void deleteByTeacherIdAndStudentId(UUID teacherId, UUID studentId);
    
    // 统计教师的学生数量
    long countByTeacherId(UUID teacherId);
}
```

#### 3. DTO: StudentListResponse

```java
@Data
@Builder
public class StudentListResponse {
    private UUID id;
    private String username;
    private String email;
    private String realName;
    private LocalDateTime enrolledAt;
    private Long totalQuestions;
    private LocalDateTime lastActivity;
}
```

#### 4. DTO: StudentDetailResponse

```java
@Data
@Builder
public class StudentDetailResponse {
    private UUID id;
    private String username;
    private String email;
    private String realName;
    private LocalDateTime registeredAt;
    private LocalDateTime enrolledAt;
    private Long totalQuestions;
    private Long totalDocumentAccesses;
    private RecentActivitySummary recentActivity;
}

@Data
@Builder
class RecentActivitySummary {
    private Long questionsLast30Days;
    private Long documentAccessesLast30Days;
    private LocalDateTime lastQuestionAt;
    private LocalDateTime lastDocumentAccessAt;
}
```

#### 5. DTO: AddStudentRequest

```java
@Data
public class AddStudentRequest {
    @NotBlank(message = "用户名或邮箱不能为空")
    private String identifier; // username or email
}
```

#### 6. DTO: BatchAddStudentRequest

```java
@Data
public class BatchAddStudentRequest {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}
```

#### 7. DTO: BatchAddStudentResponse

```java
@Data
@Builder
public class BatchAddStudentResponse {
    private int totalProcessed;
    private int successCount;
    private int skippedCount;
    private int failedCount;
    private List<String> successList;
    private List<String> skippedList;
    private List<BatchError> failedList;
}

@Data
@Builder
class BatchError {
    private String identifier;
    private String reason;
}
```

#### 8. DTO: QaHistoryResponse

```java
@Data
@Builder
public class QaHistoryResponse {
    private UUID id;
    private String question;
    private String answer;
    private LocalDateTime askedAt;
}
```

#### 9. DTO: DocumentAccessResponse

```java
@Data
@Builder
public class DocumentAccessResponse {
    private UUID documentId;
    private String documentTitle;
    private Long accessCount;
    private LocalDateTime lastAccessAt;
}
```

#### 10. Service: TeacherStudentService

```java
@Service
@RequiredArgsConstructor
public class TeacherStudentService {
    
    private final ClassAssociationRepository classAssociationRepository;
    private final UserRepository userRepository;
    private final QaHistoryRepository qaHistoryRepository;
    
    // 获取教师的学生列表（分页）
    public Page<StudentListResponse> getStudentList(UUID teacherId, String search, Pageable pageable);
    
    // 获取学生详情
    public StudentDetailResponse getStudentDetail(UUID teacherId, UUID studentId);
    
    // 添加学生到班级
    public void addStudent(UUID teacherId, String identifier);
    
    // 批量添加学生
    public BatchAddStudentResponse batchAddStudents(UUID teacherId, MultipartFile file);
    
    // 移除学生从班级
    public void removeStudent(UUID teacherId, UUID studentId);
    
    // 获取学生问答历史
    public Page<QaHistoryResponse> getStudentQaHistory(UUID teacherId, UUID studentId, 
                                                        LocalDateTime startDate, LocalDateTime endDate, 
                                                        Pageable pageable);
    
    // 获取学生文档访问记录
    public Page<DocumentAccessResponse> getStudentDocumentAccess(UUID teacherId, UUID studentId, 
                                                                  Pageable pageable);
    
    // 导出学生数据报表
    public byte[] exportStudentReport(UUID teacherId, LocalDateTime startDate, LocalDateTime endDate, 
                                      String format);
    
    // 验证教师是否有权访问学生
    private void verifyTeacherStudentAccess(UUID teacherId, UUID studentId);
}
```


#### 11. Controller: TeacherStudentController

```java
@RestController
@RequestMapping("/teacher/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherStudentController {
    
    private final TeacherStudentService teacherStudentService;
    private final JwtTokenProvider jwtTokenProvider;
    
    // GET /teacher/students - 获取学生列表
    @GetMapping
    public ApiResponse<Page<StudentListResponse>> getStudentList(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentListResponse> students = teacherStudentService.getStudentList(teacherId, search, pageable);
        return ApiResponse.success(students);
    }
    
    // GET /teacher/students/{id} - 获取学生详情
    @GetMapping("/{id}")
    public ApiResponse<StudentDetailResponse> getStudentDetail(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        StudentDetailResponse detail = teacherStudentService.getStudentDetail(teacherId, id);
        return ApiResponse.success(detail);
    }
    
    // POST /teacher/students - 添加学生
    @PostMapping
    public ApiResponse<Void> addStudent(
            @RequestBody @Valid AddStudentRequest request,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        teacherStudentService.addStudent(teacherId, request.getIdentifier());
        return ApiResponse.success(null, "学生添加成功");
    }
    
    // POST /teacher/students/batch - 批量添加学生
    @PostMapping("/batch")
    public ApiResponse<BatchAddStudentResponse> batchAddStudents(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        BatchAddStudentResponse result = teacherStudentService.batchAddStudents(teacherId, file);
        return ApiResponse.success(result);
    }
    
    // DELETE /teacher/students/{id} - 移除学生
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeStudent(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        teacherStudentService.removeStudent(teacherId, id);
        return ApiResponse.success(null, "学生移除成功");
    }
    
    // GET /teacher/students/{id}/qa-history - 获取学生问答历史
    @GetMapping("/{id}/qa-history")
    public ApiResponse<Page<QaHistoryResponse>> getStudentQaHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<QaHistoryResponse> history = teacherStudentService.getStudentQaHistory(
                teacherId, id, startDate, endDate, pageable);
        return ApiResponse.success(history);
    }
    
    // GET /teacher/students/{id}/document-access - 获取学生文档访问记录
    @GetMapping("/{id}/document-access")
    public ApiResponse<Page<DocumentAccessResponse>> getStudentDocumentAccess(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentAccessResponse> access = teacherStudentService.getStudentDocumentAccess(
                teacherId, id, pageable);
        return ApiResponse.success(access);
    }
    
    // POST /teacher/students/export - 导出学生数据报表
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportStudentReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "csv") String format,
            Authentication authentication) {
        UUID teacherId = extractTeacherId(authentication);
        byte[] reportData = teacherStudentService.exportStudentReport(teacherId, startDate, endDate, format);
        
        String filename = "student_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String contentType = format.equalsIgnoreCase("excel") ? 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "text/csv";
        String extension = format.equalsIgnoreCase("excel") ? ".xlsx" : ".csv";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + extension + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(reportData);
    }
    
    private UUID extractTeacherId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getId();
    }
}
```

### Frontend Components

#### 1. Store: teacherStudentStore (Pinia)

```javascript
import { defineStore } from 'pinia'
import api from '@/api/interceptor'

export const useTeacherStudentStore = defineStore('teacherStudent', {
  state: () => ({
    students: [],
    currentStudent: null,
    pagination: {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    },
    loading: false,
    searchQuery: ''
  }),
  
  actions: {
    async fetchStudents(page = 0, search = '') {
      this.loading = true
      try {
        const response = await api.get('/teacher/students', {
          params: { page, size: this.pagination.size, search }
        })
        if (response.data.code === 200) {
          this.students = response.data.data.content
          this.pagination = {
            page: response.data.data.number,
            size: response.data.data.size,
            totalElements: response.data.data.totalElements,
            totalPages: response.data.data.totalPages
          }
        }
      } finally {
        this.loading = false
      }
    },
    
    async fetchStudentDetail(studentId) {
      this.loading = true
      try {
        const response = await api.get(`/teacher/students/${studentId}`)
        if (response.data.code === 200) {
          this.currentStudent = response.data.data
        }
      } finally {
        this.loading = false
      }
    },
    
    async addStudent(identifier) {
      const response = await api.post('/teacher/students', { identifier })
      if (response.data.code === 200) {
        await this.fetchStudents(this.pagination.page, this.searchQuery)
      }
      return response.data
    },
    
    async batchAddStudents(file) {
      const formData = new FormData()
      formData.append('file', file)
      const response = await api.post('/teacher/students/batch', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      if (response.data.code === 200) {
        await this.fetchStudents(this.pagination.page, this.searchQuery)
      }
      return response.data
    },
    
    async removeStudent(studentId) {
      const response = await api.delete(`/teacher/students/${studentId}`)
      if (response.data.code === 200) {
        await this.fetchStudents(this.pagination.page, this.searchQuery)
      }
      return response.data
    },
    
    async exportReport(startDate, endDate, format) {
      const response = await api.post('/teacher/students/export', null, {
        params: { startDate, endDate, format },
        responseType: 'blob'
      })
      return response.data
    }
  }
})
```


#### 2. View: TeacherStudentListView.vue

主要功能：
- 显示学生列表表格
- 搜索学生（按用户名或真实姓名）
- 分页控制
- 添加学生按钮（打开模态框）
- 批量导入按钮
- 导出报表按钮
- 查看详情按钮
- 移除学生按钮

界面布局：
- 顶部：标题、学生数量统计、刷新按钮、添加学生按钮、导出按钮
- 搜索栏：搜索输入框
- 表格：用户名、真实姓名、邮箱、总提问数、最后活动时间、操作列
- 底部：分页控制

#### 3. View: TeacherStudentDetailView.vue

主要功能：
- 显示学生基本信息卡片
- 显示统计数据卡片（总提问数、文档访问数）
- 显示最近30天活动摘要
- 标签页切换：问答历史、文档访问记录
- 返回按钮

界面布局：
- 顶部：返回按钮、学生姓名
- 信息区：基本信息卡片、统计卡片
- 标签页：
  - 问答历史：问题、答案、时间（分页）
  - 文档访问：文档标题、访问次数、最后访问时间（分页）

#### 4. Component: AddStudentModal.vue

主要功能：
- 单个添加：输入用户名或邮箱
- 批量添加：上传CSV/Excel文件
- 显示添加结果（成功、跳过、失败）
- 关闭模态框

界面布局：
- 标题：添加学生
- 标签页：单个添加、批量添加
- 单个添加：输入框、确认按钮
- 批量添加：文件上传区、上传按钮、结果显示
- 底部：取消按钮

#### 5. Component: ExportReportModal.vue

主要功能：
- 选择日期范围
- 选择导出格式（CSV/Excel）
- 触发导出下载

界面布局：
- 标题：导出学生报表
- 日期范围选择器
- 格式选择器（单选按钮）
- 底部：取消按钮、导出按钮

## Data Models

### Database Schema

#### 新增表：t_class_association

```sql
CREATE TABLE t_class_association (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_student UNIQUE (teacher_id, student_id)
);

CREATE INDEX idx_class_association_teacher ON t_class_association(teacher_id);
CREATE INDEX idx_class_association_student ON t_class_association(student_id);
CREATE INDEX idx_class_association_enrolled_at ON t_class_association(enrolled_at);
```

说明：
- `teacher_id`: 教师用户ID，外键关联t_user表
- `student_id`: 学生用户ID，外键关联t_user表
- `enrolled_at`: 学生加入班级的时间
- 唯一约束：同一教师不能重复添加同一学生
- 级联删除：当用户被删除时，相关关联记录也会被删除
- 索引：优化查询性能

### 现有表的使用

#### t_user
- 存储用户基本信息（用户名、邮箱、角色等）
- 通过role字段区分学生和教师
- 不需要修改表结构

#### t_qa_history
- 存储学生的问答历史
- 通过user_id关联到学生
- 用于统计学生提问数和查询问答记录
- 不需要修改表结构

#### t_document
- 存储文档信息
- 虽然当前设计中没有直接使用，但为未来扩展预留
- 可用于统计学生访问的文档（需要新增访问记录表）
- 不需要修改表结构

### 数据关系图

```
t_user (teacher)
    │
    │ 1:N
    ▼
t_class_association
    │
    │ N:1
    ▼
t_user (student)
    │
    │ 1:N
    ▼
t_qa_history
```


## Correctness Properties

*属性（Property）是指在系统所有有效执行中都应该成立的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性是人类可读规范和机器可验证正确性保证之间的桥梁。*

### Property Reflection

在编写属性之前，我们需要识别和消除冗余属性：

**冗余分析**：
1. 授权检查属性（3.1, 4.1, 5.1, 10.3）都在验证教师只能访问自己班级的学生，可以合并为一个通用属性
2. 分页属性（2.3, 4.3, 5.3）都在测试分页功能，可以合并为一个通用属性
3. 排序属性（2.5, 4.4, 5.4）都在测试排序功能，可以针对不同数据类型分别测试
4. 响应结构验证（2.2, 3.3, 4.2, 5.2）都在验证响应包含必需字段，可以为每个端点单独测试
5. 错误条件（3.2, 6.2, 6.4, 8.2, 10.4）都在测试特定错误场景，应该保留以确保错误处理正确

**保留的核心属性**：
- 关联管理：创建、删除、唯一性
- 数据隔离：教师只能访问自己的学生
- 角色验证：只能添加学生角色的用户
- 数据完整性：删除关联不影响用户账号
- 聚合正确性：统计数据准确
- 批量处理：批量添加的幂等性和错误恢复
- 文件生成：报表包含正确数据

### Properties

Property 1: Association Creation
*For any* teacher and student user, when the teacher adds the student to their class, a ClassAssociation record should be created with the correct teacher_id, student_id, and a non-null enrolled_at timestamp
**Validates: Requirements 1.2, 1.4, 6.5**

Property 2: Association Deletion
*For any* existing ClassAssociation, when the teacher removes the student, the association record should be deleted from the database
**Validates: Requirements 1.3, 8.3**

Property 3: Association Uniqueness
*For any* teacher and student pair, attempting to create a duplicate ClassAssociation should fail with a duplicate error
**Validates: Requirements 6.3, 6.4**

Property 4: Data Isolation
*For any* teacher, when querying students, class associations, QA history, or document access records, the results should only include data associated with that teacher's students
**Validates: Requirements 1.5, 2.1, 3.1, 4.1, 5.1, 10.3**

Property 5: Role Validation
*For any* user, attempting to add them as a student should succeed only if their role is STUDENT, otherwise it should fail with a validation error
**Validates: Requirements 6.1, 6.2**

Property 6: Authorization Check
*For any* teacher attempting to access a student's details, QA history, or document access records, the operation should succeed only if a ClassAssociation exists between them, otherwise it should return a 403 Forbidden error
**Validates: Requirements 3.2, 10.4**

Property 7: User Account Preservation
*For any* student, when removed from a teacher's class, the student's User account and all their QaHistory records should remain unchanged in the database
**Validates: Requirements 8.4**

Property 8: Student List Response Structure
*For any* student in a teacher's class, the student list response should include username, email, realName, enrolledAt, totalQuestions, and lastActivity fields
**Validates: Requirements 2.2**

Property 9: Student Detail Response Structure
*For any* student, the detail response should include profile information (username, email, realName, registeredAt, enrolledAt) and statistics (totalQuestions, totalDocumentAccesses, recentActivity)
**Validates: Requirements 3.3, 3.4**

Property 10: QA History Response Structure
*For any* QA record, the response should include question text, answer text, and timestamp
**Validates: Requirements 4.2**

Property 11: Document Access Response Structure
*For any* document access record, the response should include document title, access count, and last access timestamp
**Validates: Requirements 5.2**

Property 12: Pagination Consistency
*For any* paginated query (student list, QA history, document access), requesting page N with size S should return at most S items, and the total number of items across all pages should equal the totalElements count
**Validates: Requirements 2.3, 4.3, 5.3**

Property 13: Student List Sorting
*For any* student list query without explicit sorting, the results should be sorted by enrolledAt in descending order (most recent first)
**Validates: Requirements 2.5**

Property 14: QA History Sorting
*For any* QA history query, the results should be sorted by timestamp in descending order (most recent first)
**Validates: Requirements 4.4**

Property 15: Document Access Sorting
*For any* document access query, the results should be sorted by last access timestamp in descending order (most recent first)
**Validates: Requirements 5.4**

Property 16: Search Filtering
*For any* search query string, the student list results should only include students whose username or realName contains the search string (case-insensitive)
**Validates: Requirements 2.4**

Property 17: Date Range Filtering
*For any* QA history query with startDate and endDate parameters, the results should only include records where the timestamp falls within the specified range (inclusive)
**Validates: Requirements 4.5**

Property 18: Recent Activity Calculation
*For any* student, the recentActivity summary should accurately count questions and document accesses from the last 30 days
**Validates: Requirements 3.5**

Property 19: Document Access Aggregation
*For any* student's document access records, multiple accesses to the same document should be aggregated into a single record with the correct access count and most recent access timestamp
**Validates: Requirements 5.5**

Property 20: Batch Addition Idempotency
*For any* batch student addition operation, if the same file is uploaded multiple times, existing associations should be skipped without error, and only new associations should be created
**Validates: Requirements 7.3**

Property 21: Batch Addition Error Resilience
*For any* batch student addition with mixed valid and invalid identifiers, the operation should process all entries, create associations for valid students, and return a summary with success, skipped, and failed counts
**Validates: Requirements 7.2, 7.4, 7.5**

Property 22: CSV File Parsing
*For any* valid CSV file with student identifiers, the batch addition should correctly parse each line and extract the identifier
**Validates: Requirements 7.1**

Property 23: Report Data Filtering
*For any* student report export, the report should only include students in the teacher's class
**Validates: Requirements 9.3**

Property 24: Report Content Completeness
*For any* student in the report, the exported data should include username, email, realName, totalQuestions, totalDocumentAccesses, and lastActivity
**Validates: Requirements 9.2**

Property 25: Report Date Filtering
*For any* report export with date range parameters, the activity statistics should only count questions and document accesses within the specified date range
**Validates: Requirements 9.4**

Property 26: JWT Token Validation
*For any* request to a teacher student endpoint, the operation should fail with an authentication error if the JWT token is missing, expired, or invalid
**Validates: Requirements 10.1**

Property 27: Teacher ID Extraction
*For any* authenticated request, the teacher ID should be correctly extracted from the JWT token and used for all authorization checks
**Validates: Requirements 10.2**

Property 28: Permission Restriction
*For any* teacher, attempting to modify a student's role or delete a student's User account should fail with a permission denied error
**Validates: Requirements 10.5**

Property 29: Association Existence Check
*For any* remove student operation, if the ClassAssociation does not exist, the operation should fail with a not found error
**Validates: Requirements 8.1, 8.2**

Property 30: Remove Operation Success Response
*For any* successful remove student operation, the response should indicate success
**Validates: Requirements 8.5**


## Error Handling

### Exception Types

#### 1. AuthorizationException
- 场景：教师尝试访问不属于自己班级的学生
- HTTP状态码：403 Forbidden
- 错误消息：无权限访问该学生

#### 2. ResourceNotFoundException
- 场景：请求的学生或关联不存在
- HTTP状态码：404 Not Found
- 错误消息：学生不存在 / 关联不存在

#### 3. ValidationException
- 场景：输入数据验证失败
- HTTP状态码：400 Bad Request
- 错误消息：
  - 用户名或邮箱不能为空
  - 目标用户不是学生角色
  - 关联已存在

#### 4. FileProcessingException
- 场景：批量导入文件解析失败
- HTTP状态码：400 Bad Request
- 错误消息：文件格式不正确 / 文件解析失败

### Error Response Format

```json
{
  "code": 400,
  "message": "目标用户不是学生角色",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Service Layer Error Handling

```java
public void addStudent(UUID teacherId, String identifier) {
    // 1. 查找目标用户
    User targetUser = userRepository.findByUsernameOrEmail(identifier, identifier)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + identifier));
    
    // 2. 验证角色
    if (targetUser.getRole() != UserRole.STUDENT) {
        throw new ValidationException("目标用户不是学生角色");
    }
    
    // 3. 检查重复
    if (classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, targetUser.getId())) {
        throw new ValidationException("该学生已在您的班级中");
    }
    
    // 4. 创建关联
    ClassAssociation association = ClassAssociation.builder()
            .teacher(userRepository.getReferenceById(teacherId))
            .student(targetUser)
            .build();
    classAssociationRepository.save(association);
}

private void verifyTeacherStudentAccess(UUID teacherId, UUID studentId) {
    if (!classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, studentId)) {
        throw new AuthorizationException("无权限访问该学生");
    }
}
```

### Frontend Error Handling

```javascript
async addStudent(identifier) {
  try {
    const response = await api.post('/teacher/students', { identifier })
    if (response.data.code === 200) {
      ElMessage.success('学生添加成功')
      await this.fetchStudents()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error.response?.status === 403) {
      ElMessage.error('无权限执行此操作')
    } else if (error.response?.status === 404) {
      ElMessage.error('用户不存在')
    } else if (error.response?.status === 400) {
      ElMessage.error(error.response.data.message || '请求参数错误')
    } else {
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}
```

## Testing Strategy

### 测试方法

本功能采用双重测试策略：
1. **单元测试**：验证特定示例、边界情况和错误条件
2. **属性测试**：验证跨所有输入的通用属性

两种测试方法是互补的，共同确保全面覆盖。单元测试捕获具体的错误，属性测试验证一般正确性。

### 单元测试

单元测试应该专注于：
- 特定示例：演示正确行为的具体案例
- 集成点：组件之间的交互
- 边界情况和错误条件：特殊输入和异常场景

**避免编写过多的单元测试** - 属性测试已经处理了大量输入的覆盖。

#### Service Layer Tests

```java
@SpringBootTest
class TeacherStudentServiceTest {
    
    @Test
    void addStudent_Success() {
        // 测试成功添加学生的具体示例
    }
    
    @Test
    void addStudent_UserNotFound() {
        // 测试用户不存在的错误情况
    }
    
    @Test
    void addStudent_NotStudentRole() {
        // 测试目标用户不是学生角色的错误情况
    }
    
    @Test
    void addStudent_DuplicateAssociation() {
        // 测试重复添加的错误情况
    }
    
    @Test
    void removeStudent_Success() {
        // 测试成功移除学生的具体示例
    }
    
    @Test
    void removeStudent_NotFound() {
        // 测试关联不存在的错误情况
    }
    
    @Test
    void getStudentDetail_Unauthorized() {
        // 测试未授权访问的错误情况
    }
    
    @Test
    void batchAddStudents_MixedResults() {
        // 测试批量添加混合结果的具体示例
    }
}
```

#### Controller Layer Tests

```java
@WebMvcTest(TeacherStudentController.class)
class TeacherStudentControllerTest {
    
    @Test
    @WithMockUser(roles = "TEACHER")
    void getStudentList_ReturnsPagedResults() {
        // 测试分页查询返回正确格式
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentList_ForbiddenForStudent() {
        // 测试学生角色无法访问
    }
    
    @Test
    @WithMockUser(roles = "TEACHER")
    void addStudent_ValidRequest() {
        // 测试有效请求返回成功
    }
    
    @Test
    @WithMockUser(roles = "TEACHER")
    void addStudent_InvalidRequest() {
        // 测试无效请求返回错误
    }
}
```

### 属性测试

属性测试验证跨所有输入的通用属性。每个属性测试必须：
- 运行至少100次迭代（由于随机化）
- 引用其设计文档属性
- 使用标签格式：**Feature: teacher-student-management, Property {number}: {property_text}**

#### 属性测试库

使用 **jqwik** 进行Java属性测试：

```xml
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.7.4</version>
    <scope>test</scope>
</dependency>
```

#### Property Test Examples

```java
@PropertyTest(tries = 100)
@Tag("Feature: teacher-student-management, Property 1: Association Creation")
void associationCreation_CreatesRecordWithCorrectFields(
        @ForAll UUID teacherId,
        @ForAll UUID studentId) {
    // Feature: teacher-student-management, Property 1: Association Creation
    // For any teacher and student user, when the teacher adds the student to their class,
    // a ClassAssociation record should be created with the correct teacher_id, student_id,
    // and a non-null enrolled_at timestamp
    
    // Setup: Create teacher and student users
    User teacher = createUser(teacherId, UserRole.TEACHER);
    User student = createUser(studentId, UserRole.STUDENT);
    
    // Execute: Add student to class
    teacherStudentService.addStudent(teacherId, student.getUsername());
    
    // Verify: Association exists with correct fields
    Optional<ClassAssociation> association = 
            classAssociationRepository.findByTeacherIdAndStudentId(teacherId, studentId);
    
    assertThat(association).isPresent();
    assertThat(association.get().getTeacher().getId()).isEqualTo(teacherId);
    assertThat(association.get().getStudent().getId()).isEqualTo(studentId);
    assertThat(association.get().getEnrolledAt()).isNotNull();
}

@PropertyTest(tries = 100)
@Tag("Feature: teacher-student-management, Property 4: Data Isolation")
void dataIsolation_TeacherOnlySeesOwnStudents(
        @ForAll UUID teacher1Id,
        @ForAll UUID teacher2Id,
        @ForAll @Size(min = 1, max = 10) List<UUID> student1Ids,
        @ForAll @Size(min = 1, max = 10) List<UUID> student2Ids) {
    // Feature: teacher-student-management, Property 4: Data Isolation
    // For any teacher, when querying students, the results should only include
    // data associated with that teacher's students
    
    Assume.that(!teacher1Id.equals(teacher2Id));
    
    // Setup: Create two teachers with their own students
    User teacher1 = createUser(teacher1Id, UserRole.TEACHER);
    User teacher2 = createUser(teacher2Id, UserRole.TEACHER);
    
    List<User> students1 = student1Ids.stream()
            .map(id -> createUser(id, UserRole.STUDENT))
            .collect(Collectors.toList());
    
    List<User> students2 = student2Ids.stream()
            .map(id -> createUser(id, UserRole.STUDENT))
            .collect(Collectors.toList());
    
    // Add students to respective teachers
    students1.forEach(s -> teacherStudentService.addStudent(teacher1Id, s.getUsername()));
    students2.forEach(s -> teacherStudentService.addStudent(teacher2Id, s.getUsername()));
    
    // Execute: Query students for teacher1
    Page<StudentListResponse> teacher1Students = 
            teacherStudentService.getStudentList(teacher1Id, null, PageRequest.of(0, 100));
    
    // Verify: Only teacher1's students are returned
    Set<UUID> returnedIds = teacher1Students.getContent().stream()
            .map(StudentListResponse::getId)
            .collect(Collectors.toSet());
    
    Set<UUID> expectedIds = students1.stream()
            .map(User::getId)
            .collect(Collectors.toSet());
    
    assertThat(returnedIds).isEqualTo(expectedIds);
    assertThat(returnedIds).doesNotContainAnyElementsOf(
            students2.stream().map(User::getId).collect(Collectors.toSet()));
}

@PropertyTest(tries = 100)
@Tag("Feature: teacher-student-management, Property 5: Role Validation")
void roleValidation_OnlyStudentsCanBeAdded(
        @ForAll UUID teacherId,
        @ForAll UUID userId,
        @ForAll UserRole role) {
    // Feature: teacher-student-management, Property 5: Role Validation
    // For any user, attempting to add them as a student should succeed only if
    // their role is STUDENT, otherwise it should fail with a validation error
    
    // Setup: Create teacher and target user with specified role
    User teacher = createUser(teacherId, UserRole.TEACHER);
    User targetUser = createUser(userId, role);
    
    // Execute and Verify
    if (role == UserRole.STUDENT) {
        // Should succeed
        assertDoesNotThrow(() -> 
                teacherStudentService.addStudent(teacherId, targetUser.getUsername()));
    } else {
        // Should fail with validation error
        assertThrows(ValidationException.class, () -> 
                teacherStudentService.addStudent(teacherId, targetUser.getUsername()));
    }
}

@PropertyTest(tries = 100)
@Tag("Feature: teacher-student-management, Property 7: User Account Preservation")
void userAccountPreservation_RemovalDoesNotDeleteUser(
        @ForAll UUID teacherId,
        @ForAll UUID studentId) {
    // Feature: teacher-student-management, Property 7: User Account Preservation
    // For any student, when removed from a teacher's class, the student's User account
    // and all their QaHistory records should remain unchanged in the database
    
    // Setup: Create teacher, student, and some QA history
    User teacher = createUser(teacherId, UserRole.TEACHER);
    User student = createUser(studentId, UserRole.STUDENT);
    teacherStudentService.addStudent(teacherId, student.getUsername());
    
    List<QaHistory> qaRecords = createRandomQaHistory(studentId, 5);
    
    // Execute: Remove student from class
    teacherStudentService.removeStudent(teacherId, studentId);
    
    // Verify: User account still exists
    Optional<User> userAfterRemoval = userRepository.findById(studentId);
    assertThat(userAfterRemoval).isPresent();
    
    // Verify: QA history still exists
    List<QaHistory> qaAfterRemoval = qaHistoryRepository.findByUserId(studentId);
    assertThat(qaAfterRemoval).hasSize(qaRecords.size());
}
```

### Frontend Tests

前端测试使用 **Vitest** 和 **Vue Test Utils**：

```javascript
// Unit tests for store actions
describe('TeacherStudentStore', () => {
  it('fetches students successfully', async () => {
    // Test specific example of fetching students
  })
  
  it('handles fetch error gracefully', async () => {
    // Test error handling
  })
})

// Component tests
describe('TeacherStudentListView', () => {
  it('renders student table with data', () => {
    // Test UI rendering with specific data
  })
  
  it('opens add student modal on button click', () => {
    // Test UI interaction
  })
})
```

### 测试配置

每个属性测试必须配置为运行至少100次迭代：

```java
@PropertyTest(tries = 100)
```

这确保了通过随机化实现全面的输入覆盖。

