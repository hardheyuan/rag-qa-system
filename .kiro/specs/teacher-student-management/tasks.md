# Implementation Plan: Teacher Student Management

## Overview

本实现计划将教师端学生管理功能分解为一系列增量式的编码任务。每个任务都建立在前一个任务的基础上，最终将所有组件集成在一起。实现将按照后端优先的顺序进行，先完成数据层和服务层，再实现API层，最后完成前端界面。

## Tasks

- [x] 1. 创建数据库表和实体类
  - 创建ClassAssociation实体类，包含teacher、student和enrolledAt字段
  - 创建数据库迁移脚本（SQL）用于创建t_class_association表
  - 添加唯一约束和索引以优化查询性能
  - _Requirements: 1.1, 1.4_

- [ ] 2. 创建Repository层
  - [x] 2.1 实现ClassAssociationRepository接口
    - 定义查询方法：findByTeacherId, existsByTeacherIdAndStudentId等
    - 添加分页查询支持
    - _Requirements: 1.5, 2.1_
  
  - [ ]* 2.2 编写ClassAssociationRepository的属性测试
    - **Property 1: Association Creation**
    - **Property 2: Association Deletion**
    - **Property 3: Association Uniqueness**
    - **Validates: Requirements 1.2, 1.3, 1.4, 6.3, 6.4**

- [x] 3. 创建DTO类
  - 创建StudentListResponse, StudentDetailResponse, AddStudentRequest等DTO
  - 创建BatchAddStudentRequest和BatchAddStudentResponse
  - 创建QaHistoryResponse和DocumentAccessResponse
  - _Requirements: 2.2, 3.3, 4.2, 5.2_

- [ ] 4. 实现Service层核心功能
  - [x] 4.1 实现添加学生功能
    - 验证目标用户角色为STUDENT
    - 检查关联是否已存在
    - 创建ClassAssociation记录
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [ ]* 4.2 编写添加学生的属性测试
    - **Property 5: Role Validation**
    - **Property 3: Association Uniqueness**
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4**
  
  - [x] 4.3 实现移除学生功能
    - 验证关联存在
    - 删除ClassAssociation记录
    - 确保不删除User账号和学习记录
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [ ]* 4.4 编写移除学生的属性测试
    - **Property 7: User Account Preservation**
    - **Property 29: Association Existence Check**
    - **Validates: Requirements 8.1, 8.2, 8.4**

- [ ] 5. 实现Service层查询功能
  - [x] 5.1 实现学生列表查询
    - 通过ClassAssociation过滤教师的学生
    - 支持搜索（按用户名或真实姓名）
    - 支持分页和排序
    - 聚合统计数据（总提问数、最后活动时间）
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [ ]* 5.2 编写学生列表查询的属性测试
    - **Property 4: Data Isolation**
    - **Property 8: Student List Response Structure**
    - **Property 12: Pagination Consistency**
    - **Property 13: Student List Sorting**
    - **Property 16: Search Filtering**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
  
  - [x] 5.3 实现学生详情查询
    - 验证教师有权访问该学生
    - 返回学生基本信息
    - 计算统计数据（总提问数、文档访问数）
    - 计算最近30天活动摘要
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [ ]* 5.4 编写学生详情查询的属性测试
    - **Property 6: Authorization Check**
    - **Property 9: Student Detail Response Structure**
    - **Property 18: Recent Activity Calculation**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

- [ ] 6. 实现学习记录查询功能
  - [x] 6.1 实现问答历史查询
    - 验证教师有权访问该学生
    - 支持日期范围过滤
    - 支持分页和排序
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [ ]* 6.2 编写问答历史查询的属性测试
    - **Property 10: QA History Response Structure**
    - **Property 14: QA History Sorting**
    - **Property 17: Date Range Filtering**
    - **Validates: Requirements 4.2, 4.4, 4.5**
  
  - [x] 6.3 实现文档访问记录查询
    - 验证教师有权访问该学生
    - 聚合同一文档的多次访问
    - 支持分页和排序
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [ ]* 6.4 编写文档访问记录查询的属性测试
    - **Property 11: Document Access Response Structure**
    - **Property 15: Document Access Sorting**
    - **Property 19: Document Access Aggregation**
    - **Validates: Requirements 5.2, 5.4, 5.5**

- [x] 7. Checkpoint - 确保所有测试通过
  - 运行所有单元测试和属性测试
  - 确认所有核心功能正常工作
  - 如有问题请询问用户

- [ ] 8. 实现批量添加功能
  - [x] 8.1 实现CSV/Excel文件解析
    - 支持CSV和Excel格式
    - 提取学生标识符（用户名或邮箱）
    - _Requirements: 7.1_
  
  - [x] 8.2 实现批量添加逻辑
    - 验证每个学生标识符
    - 跳过已存在的关联
    - 继续处理即使部分失败
    - 返回详细的处理结果摘要
    - _Requirements: 7.2, 7.3, 7.4, 7.5_
  
  - [ ]* 8.3 编写批量添加的属性测试
    - **Property 20: Batch Addition Idempotency**
    - **Property 21: Batch Addition Error Resilience**
    - **Property 22: CSV File Parsing**
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**

- [ ] 9. 实现数据导出功能
  - [x] 9.1 实现报表生成逻辑
    - 过滤教师的学生
    - 支持日期范围过滤
    - 生成CSV或Excel格式
    - 包含所有必需字段
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [ ]* 9.2 编写报表生成的属性测试
    - **Property 23: Report Data Filtering**
    - **Property 24: Report Content Completeness**
    - **Property 25: Report Date Filtering**
    - **Validates: Requirements 9.2, 9.3, 9.4**

- [ ] 10. 实现Controller层
  - [x] 10.1 创建TeacherStudentController
    - 实现所有REST端点
    - 添加@PreAuthorize注解确保只有教师可访问
    - 从JWT token提取教师ID
    - 处理分页和排序参数
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [ ]* 10.2 编写Controller的单元测试
    - 测试权限控制（只有教师可访问）
    - 测试参数验证
    - 测试错误响应格式
    - _Requirements: 10.1, 10.4, 10.5_
  
  - [ ]* 10.3 编写Controller的属性测试
    - **Property 26: JWT Token Validation**
    - **Property 27: Teacher ID Extraction**
    - **Property 28: Permission Restriction**
    - **Validates: Requirements 10.1, 10.2, 10.5**

- [x] 11. Checkpoint - 后端集成测试
  - 运行所有后端测试
  - 使用Postman或curl测试所有API端点
  - 确认权限控制正常工作
  - 如有问题请询问用户

- [ ] 12. 创建前端Store
  - [x] 12.1 实现teacherStudentStore (Pinia)
    - 定义state（students, currentStudent, pagination等）
    - 实现actions（fetchStudents, addStudent, removeStudent等）
    - 处理API调用和错误
    - _Requirements: 2.1, 3.1, 6.1, 8.1, 9.1_
  
  - [ ]* 12.2 编写Store的单元测试
    - 测试API调用成功场景
    - 测试错误处理
    - 测试状态更新

- [ ] 13. 创建学生列表视图
  - [x] 13.1 实现TeacherStudentListView.vue
    - 创建表格显示学生列表
    - 实现搜索功能
    - 实现分页控制
    - 添加操作按钮（查看详情、移除）
    - 添加顶部按钮（添加学生、导出报表）
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_
  
  - [ ]* 13.2 编写视图的组件测试
    - 测试表格渲染
    - 测试搜索交互
    - 测试按钮点击事件

- [ ] 14. 创建学生详情视图
  - [x] 14.1 实现TeacherStudentDetailView.vue
    - 显示学生基本信息卡片
    - 显示统计数据卡片
    - 实现标签页（问答历史、文档访问）
    - 实现分页加载
    - 添加返回按钮
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
  
  - [ ]* 14.2 编写视图的组件测试
    - 测试信息卡片渲染
    - 测试标签页切换
    - 测试分页加载

- [ ] 15. 创建添加学生模态框
  - [x] 15.1 实现AddStudentModal.vue
    - 创建模态框组件
    - 实现单个添加表单
    - 实现批量添加文件上传
    - 显示批量添加结果
    - 处理成功和错误状态
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_
  
  - [ ]* 15.2 编写模态框的组件测试
    - 测试表单提交
    - 测试文件上传
    - 测试结果显示

- [ ] 16. 创建导出报表模态框
  - [x] 16.1 实现ExportReportModal.vue
    - 创建模态框组件
    - 实现日期范围选择器
    - 实现格式选择器
    - 触发文件下载
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_
  
  - [ ]* 16.2 编写模态框的组件测试
    - 测试日期选择
    - 测试格式选择
    - 测试导出触发

- [x] 17. 配置路由
  - 在Vue Router中添加教师学生管理路由
  - 配置路由守卫确保只有教师可访问
  - 添加面包屑导航
  - _Requirements: 11.1, 12.1_

- [ ] 18. 实现错误处理和用户反馈
  - [x] 18.1 在前端添加全局错误处理
    - 拦截API错误响应
    - 显示友好的中文错误消息
    - 实现自动消失的成功通知
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_
  
  - [ ]* 18.2 编写错误处理的单元测试
    - 测试不同错误码的处理
    - 测试错误消息显示
    - 测试通知自动消失

- [x] 19. 样式优化
  - 确保界面风格与现有管理员界面一致
  - 使用Tailwind CSS实现响应式设计
  - 添加加载状态和骨架屏
  - 优化移动端显示
  - _Requirements: 11.1, 12.1, 13.1, 14.1_

- [x] 20. Final Checkpoint - 端到端测试
  - 在浏览器中测试完整用户流程
  - 测试添加学生、查看详情、移除学生
  - 测试批量导入和数据导出
  - 测试权限控制和错误处理
  - 确认所有功能正常工作
  - 如有问题请询问用户

## Notes

- 任务标记为 `*` 的是可选的测试任务，可以跳过以加快MVP开发
- 每个任务都引用了具体的需求，便于追溯
- Checkpoint任务确保增量验证
- 属性测试验证通用正确性属性
- 单元测试验证特定示例和边界情况
