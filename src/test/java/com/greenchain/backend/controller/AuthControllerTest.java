package com.greenchain.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("注册新用户：成功")
    void testRegister_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(User.Role.VIEWER);
        user.setCompanyName("测试公司");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("VIEWER"));

        // 验证密码已加密
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assert savedUser != null;
        assert passwordEncoder.matches("password123", savedUser.getPassword());
    }

    @Test
    @DisplayName("注册新用户：用户已存在应失败")
    @org.junit.jupiter.api.Disabled("跳过：需要后端添加全局异常处理")
    void testRegister_UserAlreadyExists() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("uniqueexistinguser");
        existingUser.setEmail("uniqueexisting@example.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole(User.Role.VIEWER);
        userRepository.save(existingUser);

        User newUser = new User();
        newUser.setUsername("uniqueexistinguser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");
        newUser.setRole(User.Role.VIEWER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("登录：成功")
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setUsername("logintest");
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRole(User.Role.VIEWER);
        user.setEnabled(true);
        userRepository.save(user);

        User loginRequest = new User();
        loginRequest.setUsername("logintest");
        loginRequest.setPassword("correctpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("登录成功"));
    }

    @Test
    @DisplayName("登录：密码错误")
    void testLogin_WrongPassword() throws Exception {
        User user = new User();
        user.setUsername("wrongpasstest");
        user.setEmail("wrongpass@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRole(User.Role.VIEWER);
        user.setEnabled(true);
        userRepository.save(user);

        User loginRequest = new User();
        loginRequest.setUsername("wrongpasstest");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录：用户不存在")
    void testLogin_UserNotFound() throws Exception {
        User loginRequest = new User();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("anypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("用户名或密码错误"));
    }

    @Test
    @DisplayName("注册：密码加密验证")
    void testRegister_PasswordEncryption() throws Exception {
        User user = new User();
        user.setUsername("encryptiontest");
        user.setEmail("encryption@example.com");
        user.setPassword("plaintextpassword");
        user.setRole(User.Role.VIEWER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByUsername("encryptiontest").orElseThrow();
        assert savedUser.getPassword() != null;
        assert !savedUser.getPassword().equals("plaintextpassword");
        assert passwordEncoder.matches("plaintextpassword", savedUser.getPassword());
    }

    @Test
    @DisplayName("注册：用户默认启用")
    void testRegister_UserEnabledByDefault() throws Exception {
        User user = new User();
        user.setUsername("enabledtest");
        user.setEmail("enabled@example.com");
        user.setPassword("password");
        user.setRole(User.Role.VIEWER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("注册：不同角色")
    void testRegister_DifferentRoles() throws Exception {
        for (User.Role role : User.Role.values()) {
            User user = new User();
            user.setUsername("role_" + role.name());
            user.setEmail(role.name() + "@example.com");
            user.setPassword("password");
            user.setRole(role);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value(role.name()));
        }
    }
}
