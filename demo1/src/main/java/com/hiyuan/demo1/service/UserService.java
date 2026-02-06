package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.user.UpdateUserRequest;
import com.hiyuan.demo1.dto.user.UserListResponse;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户管理服务
 *
 * 提供用户管理的业务逻辑，包括查询、更新、删除用户
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 获取所有用户列表
     *
     * @return 用户列表响应
     */
    public List<UserListResponse> getAllUsers() {
        log.info("获取所有用户列表");
        return userRepository.findAll().stream()
                .map(UserListResponse::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查找用户
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
    }

    /**
     * 更新用户信息
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的用户响应
     */
    @Transactional
    public UserListResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("更新用户信息: userId={}, request={}", userId, request);

        User user = findById(userId);

        // 更新用户名
        if (request.getName() != null && !request.getName().isEmpty()) {
            // 检查新用户名是否已被其他用户使用
            if (!request.getName().equals(user.getUsername())) {
                if (userRepository.existsByUsername(request.getName())) {
                    throw new BusinessException("用户名已被使用: " + request.getName());
                }
                user.setUsername(request.getName());
            }
        }

        // 更新邮箱
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // 检查新邮箱是否已被其他用户使用
            if (!request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new BusinessException("邮箱已被使用: " + request.getEmail());
                }
                user.setEmail(request.getEmail());
            }
        }

        // 更新角色
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                UserRole newRole = UserRole.fromString(request.getRole());
                user.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("无效的角色: " + request.getRole());
            }
        }

        // 更新状态
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            boolean isActive = "Active".equalsIgnoreCase(request.getStatus());
            user.setIsActive(isActive);
        }

        userRepository.save(user);
        log.info("用户更新成功: userId={}", userId);

        return UserListResponse.fromUser(user);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("删除用户: userId={}", userId);

        User user = findById(userId);

        // 防止删除最后一个管理员
        if (user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new BusinessException("不能删除最后一个管理员");
            }
        }

        userRepository.delete(user);
        log.info("用户删除成功: userId={}", userId);
    }
}
