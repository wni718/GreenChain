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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("Register: duplicate username returns 400")
    void testRegister_DuplicateUsername_400() throws Exception {
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
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    @DisplayName("Register: duplicate email returns 400")
    void testRegister_DuplicateEmail_400() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("u1");
        existingUser.setEmail("dup@example.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole(User.Role.VIEWER);
        userRepository.save(existingUser);

        User newUser = new User();
        newUser.setUsername("u2");
        newUser.setEmail("dup@example.com");
        newUser.setPassword("password123");
        newUser.setRole(User.Role.VIEWER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
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
                .andExpect(jsonPath("$.username").value("logintest"))
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    @DisplayName("登录：使用邮箱成功")
    void testLogin_WithEmail_Success() throws Exception {
        User user = new User();
        user.setUsername("emailLoginUser");
        user.setEmail("email-login@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRole(User.Role.VIEWER);
        user.setEnabled(true);
        userRepository.save(user);

        User loginRequest = new User();
        loginRequest.setEmail("email-login@example.com");
        loginRequest.setPassword("correctpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("emailLoginUser"))
                .andExpect(jsonPath("$.email").value("email-login@example.com"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    @DisplayName("按用户名查询公开资料")
    void testAccountByUsername() throws Exception {
        User user = new User();
        user.setUsername("accttest");
        user.setEmail("acct@example.com");
        user.setPassword(passwordEncoder.encode("p"));
        user.setRole(User.Role.VIEWER);
        userRepository.save(user);

        mockMvc.perform(get("/api/auth/account/accttest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("accttest"))
                .andExpect(jsonPath("$.email").value("acct@example.com"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
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
                .andExpect(content().string("Username / password incorrect"));
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
                .andExpect(content().string("Username / password incorrect"));
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

    @Test
    @DisplayName("重置密码：成功后可使用新密码校验")
    void testResetPassword_Success() throws Exception {
        User u = new User();
        u.setUsername("reset_u1");
        u.setEmail("reset1@example.com");
        u.setPassword(passwordEncoder.encode("oldSecret"));
        u.setRole(User.Role.VIEWER);
        userRepository.save(u);

        Map<String, String> body = Map.of(
                "email", "reset1@example.com",
                "newPassword", "newSecret",
                "confirmPassword", "newSecret"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password updated")));

        User updated = userRepository.findByEmail("reset1@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("newSecret", updated.getPassword()));
        assertFalse(passwordEncoder.matches("oldSecret", updated.getPassword()));
    }

    @Test
    @DisplayName("重置密码：新密码与旧密码相同则拒绝")
    void testResetPassword_SameAsOldRejected() throws Exception {
        User u = new User();
        u.setUsername("reset_u2");
        u.setEmail("reset2@example.com");
        u.setPassword(passwordEncoder.encode("samePass"));
        u.setRole(User.Role.VIEWER);
        userRepository.save(u);

        Map<String, String> body = Map.of(
                "email", "reset2@example.com",
                "newPassword", "samePass",
                "confirmPassword", "samePass"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The new password must be different from the old password."));
    }

    @Test
    @DisplayName("重置密码：两次新密码不一致")
    void testResetPassword_ConfirmMismatch() throws Exception {
        User u = new User();
        u.setUsername("reset_u3");
        u.setEmail("reset3@example.com");
        u.setPassword(passwordEncoder.encode("p"));
        u.setRole(User.Role.VIEWER);
        userRepository.save(u);

        Map<String, String> body = Map.of(
                "email", "reset3@example.com",
                "newPassword", "a",
                "confirmPassword", "b"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The two new passwords do not match."));
    }
}
