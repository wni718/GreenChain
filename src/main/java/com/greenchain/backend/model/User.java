package com.greenchain.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, SUSTAINABILITY_MANAGER, SUPPLIER, VIEWER

    private String companyName;
    private Boolean enabled = true;

    public enum Role {
        ADMIN, SUSTAINABILITY_MANAGER, SUPPLIER, VIEWER
    }

    // 明确提供常用 getter/setter，避免部分环境下 Lombok 注解处理器不可用导致编译/提示异常
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}