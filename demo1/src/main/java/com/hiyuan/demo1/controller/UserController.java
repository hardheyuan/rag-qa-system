package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.dto.user.UpdateUserRequest;
import com.hiyuan.demo1.dto.user.UserListResponse;
import com.hiyuan.demo1.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 用户管理控制器
 *
 * 提供管理员管理用户的完整API接口
 * - 查询用户列表
 * - 更新用户信息
 * - 删除用户
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表（管理员权限）
     */
    @GetMapping
    public ApiResponse<List<UserListResponse>> getUsers() {
        log.info("管理员获取用户列表");
        List<UserListResponse> users = userService.getAllUsers();
        log.info("返回 {} 个用户", users.size());
        return ApiResponse.success(users);
    }

    /**
     * 更新用户信息（管理员权限）
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserListResponse> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request) {
        log.info("管理员更新用户: userId={}", userId);
        UserListResponse updatedUser = userService.updateUser(userId, request);
        log.info("用户更新成功: userId={}", userId);
        return ApiResponse.success(updatedUser);
    }

    /**
     * 删除用户（管理员权限）
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable UUID userId) {
        log.info("管理员删除用户: userId={}", userId);
        userService.deleteUser(userId);
        log.info("用户删除成功: userId={}", userId);
        return ApiResponse.success(null);
    }
}
