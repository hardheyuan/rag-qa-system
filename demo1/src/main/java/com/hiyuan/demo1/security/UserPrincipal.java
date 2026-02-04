package com.hiyuan.demo1.security;

import com.hiyuan.demo1.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Spring Security 用户主体
 * 
 * 封装认证后的用户信息，用于 Spring Security 上下文
 * 实现 UserDetails 接口以兼容 Spring Security 标准
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String username;
    private String password;
    private UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getSpringSecurityRole()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 获取角色（兼容前端）
     */
    public String getRole() {
        return role.getEnglishName();
    }

    /**
     * 检查是否有指定角色权限
     */
    public boolean hasRole(UserRole requiredRole) {
        return role.hasRole(requiredRole);
    }
}
