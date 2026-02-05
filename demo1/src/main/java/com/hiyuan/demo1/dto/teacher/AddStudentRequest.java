package com.hiyuan.demo1.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加学生请求 DTO
 * 
 * 用于教师添加单个学生到班级
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStudentRequest {
    
    /**
     * 学生标识符（用户名或邮箱）
     */
    @NotBlank(message = "用户名或邮箱不能为空")
    private String identifier;
}
