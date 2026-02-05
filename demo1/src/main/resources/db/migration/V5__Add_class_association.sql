-- =====================================================
-- V5: 添加班级关联表 (t_class_association)
-- =====================================================
-- 
-- 功能说明：
-- 创建教师-学生关联表，用于管理教师的班级成员
-- 
-- 业务背景：
-- - 教师需要管理自己班级的学生
-- - 一个教师可以有多个学生
-- - 一个学生可以属于多个教师的班级
-- - 需要记录学生加入班级的时间
-- 
-- 表结构：
-- - id: UUID主键
-- - teacher_id: 教师用户ID（外键）
-- - student_id: 学生用户ID（外键）
-- - enrolled_at: 学生加入班级的时间
-- - created_at: 记录创建时间
-- - updated_at: 记录更新时间
-- 
-- 约束：
-- - 唯一约束：同一教师不能重复添加同一学生
-- - 外键约束：级联删除，当用户被删除时关联记录也会被删除
-- 
-- 索引：
-- - idx_class_association_teacher: 优化按教师查询
-- - idx_class_association_student: 优化按学生查询
-- - idx_class_association_enrolled_at: 优化按加入时间排序
-- =====================================================

-- 创建班级关联表
CREATE TABLE t_class_association (
    -- 主键：使用UUID确保全局唯一
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- 教师用户ID：外键关联t_user表
    -- ON DELETE CASCADE: 当教师用户被删除时，自动删除相关关联记录
    teacher_id UUID NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    
    -- 学生用户ID：外键关联t_user表
    -- ON DELETE CASCADE: 当学生用户被删除时，自动删除相关关联记录
    student_id UUID NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    
    -- 学生加入班级的时间
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 记录创建时间（与BaseEntity保持一致）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 记录更新时间（与BaseEntity保持一致）
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 唯一约束：确保同一教师不能重复添加同一学生
    CONSTRAINT uk_teacher_student UNIQUE (teacher_id, student_id)
);

-- 添加表注释
COMMENT ON TABLE t_class_association IS '班级关联表 - 存储教师与学生的关联关系';
COMMENT ON COLUMN t_class_association.id IS '主键ID，UUID格式';
COMMENT ON COLUMN t_class_association.teacher_id IS '教师用户ID，外键关联t_user表';
COMMENT ON COLUMN t_class_association.student_id IS '学生用户ID，外键关联t_user表';
COMMENT ON COLUMN t_class_association.enrolled_at IS '学生加入班级的时间';
COMMENT ON COLUMN t_class_association.created_at IS '记录创建时间';
COMMENT ON COLUMN t_class_association.updated_at IS '记录更新时间';

-- 创建索引以优化查询性能

-- 索引1：按教师ID查询
-- 用途：查询某个教师的所有学生
-- 场景：教师查看自己班级的学生列表
CREATE INDEX idx_class_association_teacher ON t_class_association(teacher_id);

-- 索引2：按学生ID查询
-- 用途：查询某个学生属于哪些教师的班级
-- 场景：检查学生是否已在某教师班级中
CREATE INDEX idx_class_association_student ON t_class_association(student_id);

-- 索引3：按加入时间查询
-- 用途：按加入时间排序学生列表
-- 场景：显示最近加入的学生
CREATE INDEX idx_class_association_enrolled_at ON t_class_association(enrolled_at);
