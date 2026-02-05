# Requirements Document

## Introduction

本文档定义了教育AI系统中教师端学生管理功能的需求。该功能允许教师管理自己班级的学生，查看学生的学习数据和问答历史，并生成学生数据报表。系统基于Spring Boot后端和Vue 3前端，使用PostgreSQL数据库存储数据。

## Glossary

- **System**: 教育AI系统的教师端学生管理模块
- **Teacher**: 具有TEACHER角色的用户，可以管理自己班级的学生
- **Student**: 具有STUDENT角色的用户，属于一个或多个教师的班级
- **Class_Association**: 教师与学生之间的关联关系
- **Learning_Record**: 学生的学习数据，包括问答历史和文档访问记录
- **Student_Report**: 学生数据的导出报表
- **Admin**: 具有ADMIN角色的用户，拥有系统最高权限

## Requirements

### Requirement 1: 教师-学生关联管理

**User Story:** 作为教师，我想要建立和管理与学生的关联关系，以便我能够跟踪和管理自己班级的学生。

#### Acceptance Criteria

1. THE System SHALL create a Class_Association table to store teacher-student relationships
2. WHEN a teacher adds a student to their class, THE System SHALL create a new Class_Association record
3. WHEN a teacher removes a student from their class, THE System SHALL delete the corresponding Class_Association record
4. THE System SHALL ensure each Class_Association contains teacher_id, student_id, and creation_timestamp
5. WHEN querying class associations, THE System SHALL return only associations where the requesting teacher is the owner

### Requirement 2: 学生列表查询

**User Story:** 作为教师，我想要查看自己班级的所有学生列表，以便我能够了解班级的学生构成。

#### Acceptance Criteria

1. WHEN a teacher requests their student list, THE System SHALL return only students associated with that teacher
2. THE System SHALL include student basic information: username, email, real_name, and enrollment_date
3. WHEN displaying student list, THE System SHALL support pagination with configurable page size
4. WHEN a teacher searches for students, THE System SHALL filter results by username or real_name
5. THE System SHALL sort student list by enrollment_date in descending order by default

### Requirement 3: 学生详细信息查询

**User Story:** 作为教师，我想要查看单个学生的详细信息和学习数据，以便我能够了解学生的学习情况。

#### Acceptance Criteria

1. WHEN a teacher requests student details, THE System SHALL verify the student belongs to that teacher's class
2. IF a student does not belong to the requesting teacher, THEN THE System SHALL return an authorization error
3. THE System SHALL return student profile information including username, email, real_name, and registration_date
4. THE System SHALL return student's total question count and total document access count
5. THE System SHALL return student's recent activity summary for the last 30 days

### Requirement 4: 学生问答历史查询

**User Story:** 作为教师，我想要查看学生的问答历史记录，以便我能够了解学生的学习内容和问题。

#### Acceptance Criteria

1. WHEN a teacher requests student QA history, THE System SHALL verify the student belongs to that teacher's class
2. THE System SHALL return question text, answer text, and timestamp for each QA record
3. THE System SHALL support pagination for QA history with configurable page size
4. THE System SHALL sort QA history by timestamp in descending order
5. WHEN filtering QA history, THE System SHALL support date range filtering

### Requirement 5: 学生文档访问记录查询

**User Story:** 作为教师，我想要查看学生访问了哪些文档，以便我能够了解学生的学习资源使用情况。

#### Acceptance Criteria

1. WHEN a teacher requests student document access records, THE System SHALL verify the student belongs to that teacher's class
2. THE System SHALL return document title, access timestamp, and access count for each document
3. THE System SHALL support pagination for document access records
4. THE System SHALL sort records by access timestamp in descending order
5. THE System SHALL aggregate multiple accesses to the same document

### Requirement 6: 添加学生到班级

**User Story:** 作为教师，我想要添加学生到我的班级，以便我能够扩展我的班级规模。

#### Acceptance Criteria

1. WHEN a teacher adds a student by username or email, THE System SHALL verify the target user has STUDENT role
2. IF the target user is not a student, THEN THE System SHALL return a validation error
3. WHEN adding a student, THE System SHALL check if the association already exists
4. IF the association already exists, THEN THE System SHALL return a duplicate error
5. WHEN a student is successfully added, THE System SHALL create a Class_Association record with current timestamp

### Requirement 7: 批量添加学生

**User Story:** 作为教师，我想要批量添加多个学生到我的班级，以便我能够快速建立班级。

#### Acceptance Criteria

1. WHEN a teacher uploads a student list file, THE System SHALL parse CSV or Excel format
2. THE System SHALL validate each student identifier (username or email) in the file
3. WHEN processing batch additions, THE System SHALL skip existing associations without error
4. THE System SHALL return a summary report showing successful additions, skipped duplicates, and failed validations
5. IF any validation fails, THE System SHALL continue processing remaining students

### Requirement 8: 移除学生从班级

**User Story:** 作为教师，我想要从我的班级移除学生，以便我能够管理班级成员变动。

#### Acceptance Criteria

1. WHEN a teacher removes a student, THE System SHALL verify the Class_Association exists
2. IF the association does not exist, THEN THE System SHALL return a not found error
3. WHEN removing a student, THE System SHALL delete only the Class_Association record
4. THE System SHALL NOT delete the student's User account or learning records
5. WHEN a student is removed, THE System SHALL return a success confirmation

### Requirement 9: 学生数据报表导出

**User Story:** 作为教师，我想要导出学生数据报表，以便我能够进行离线分析和存档。

#### Acceptance Criteria

1. WHEN a teacher requests a student report, THE System SHALL generate a CSV or Excel file
2. THE System SHALL include student basic information, total questions, total document accesses, and last activity date
3. WHEN generating reports, THE System SHALL include only students in the teacher's class
4. THE System SHALL support filtering by date range for activity data
5. THE System SHALL return the report file with appropriate content-type headers for download

### Requirement 10: 权限控制

**User Story:** 作为系统架构师，我想要确保教师只能访问自己班级的学生数据，以便保护学生隐私和数据安全。

#### Acceptance Criteria

1. WHEN a teacher accesses any student-related endpoint, THE System SHALL verify the teacher's JWT token
2. THE System SHALL extract teacher_id from the authenticated JWT token
3. WHEN querying student data, THE System SHALL filter results by teacher_id in Class_Association
4. IF a teacher attempts to access another teacher's student, THEN THE System SHALL return a 403 Forbidden error
5. THE System SHALL NOT allow teachers to modify student roles or delete student accounts

### Requirement 11: 前端学生列表界面

**User Story:** 作为教师，我想要在前端看到清晰的学生列表界面，以便我能够方便地浏览和管理学生。

#### Acceptance Criteria

1. WHEN a teacher navigates to student management page, THE System SHALL display a table with student information
2. THE System SHALL display columns: username, real_name, email, total_questions, last_activity, and actions
3. THE System SHALL provide search input for filtering by username or real_name
4. THE System SHALL provide pagination controls at the bottom of the table
5. THE System SHALL provide action buttons for viewing details and removing students

### Requirement 12: 前端学生详情界面

**User Story:** 作为教师，我想要在前端看到学生的详细信息和学习数据，以便我能够深入了解学生情况。

#### Acceptance Criteria

1. WHEN a teacher clicks on a student, THE System SHALL navigate to student detail page
2. THE System SHALL display student profile section with basic information
3. THE System SHALL display statistics cards showing total questions and document accesses
4. THE System SHALL display a tabbed interface for QA history and document access records
5. THE System SHALL provide a back button to return to student list

### Requirement 13: 前端添加学生界面

**User Story:** 作为教师，我想要通过友好的界面添加学生，以便我能够轻松扩展班级。

#### Acceptance Criteria

1. WHEN a teacher clicks add student button, THE System SHALL display a modal dialog
2. THE System SHALL provide input fields for username or email
3. THE System SHALL provide a file upload option for batch addition
4. WHEN submitting the form, THE System SHALL display loading indicator
5. WHEN addition completes, THE System SHALL show success message and refresh student list

### Requirement 14: 前端数据导出功能

**User Story:** 作为教师，我想要通过界面导出学生数据，以便我能够方便地获取报表文件。

#### Acceptance Criteria

1. WHEN a teacher clicks export button, THE System SHALL display export options dialog
2. THE System SHALL provide date range selector for filtering activity data
3. THE System SHALL provide format selector for CSV or Excel
4. WHEN export is triggered, THE System SHALL download the file to browser
5. THE System SHALL display success message after download completes

### Requirement 15: 前端错误处理

**User Story:** 作为教师，我想要看到清晰的错误提示，以便我能够理解操作失败的原因。

#### Acceptance Criteria

1. WHEN an API request fails, THE System SHALL display an error notification
2. THE System SHALL show user-friendly error messages in Chinese
3. IF authorization fails, THEN THE System SHALL display "无权限访问" message
4. IF validation fails, THEN THE System SHALL display specific validation error messages
5. THE System SHALL auto-dismiss success notifications after 3 seconds
