package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自定义用户详情服务
 * 
 * 实现 Spring Security 的 UserDetailsService 接口
 * 用于从数据库加载用户信息
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        return new UserPrincipal(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }

    /**
     * 通过用户ID加载用户
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(java.util.UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }

        return new UserPrincipal(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }
}
