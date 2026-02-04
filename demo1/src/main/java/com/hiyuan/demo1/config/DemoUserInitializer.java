package com.hiyuan.demo1.config;

import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 初始化演示账号
 *
 * 确保 student/teacher/admin 三个账号存在且密码为 123456
 * 仅修复这些固定账号，避免影响其他真实用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test") // 在测试环境下不运行
public class DemoUserInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, UserRole> demoUsers = Map.of(
                "student", UserRole.STUDENT,
                "teacher", UserRole.TEACHER,
                "admin", UserRole.ADMIN
        );

        demoUsers.forEach((username, role) -> ensureDemoUser(username, role));
    }

    private void ensureDemoUser(String username, UserRole role) {
        String email = username + "@example.com";

        userRepository.findByUsername(username).ifPresentOrElse(user -> {
            boolean updated = false;

            if (user.getRole() == null) {
                user.setRole(role);
                updated = true;
            }

            if (user.getPassword() == null ||
                !passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                updated = true;
            }

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                user.setEmail(email);
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
                log.info("Demo user updated: {}", username);
            }
        }, () -> {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(role)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("Demo user created: {}", username);
        });
    }
}
