package com.greenchain.backend.controller;

import com.greenchain.backend.dto.AuthProfileResponse;
import com.greenchain.backend.dto.PasswordResetRequest;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        String email = loginUser.getEmail() != null ? loginUser.getEmail().trim() : "";
        String username = loginUser.getUsername() != null ? loginUser.getUsername().trim() : "";

        // 优先按邮箱登录；为空时回退用户名，兼容旧前端请求
        User user = null;
        if (!email.isEmpty()) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        if (user == null && !username.isEmpty()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            String profileEmail = user.getEmail() != null ? user.getEmail() : "";
            return ResponseEntity.ok(new AuthProfileResponse(user.getUsername(), profileEmail));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Username / password incorrect");
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 按用户名查询公开资料（仅 username/email），用于前端补全本地会话中的邮箱；演示环境接口。
     */
    @GetMapping(value = "/account/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthProfileResponse> accountByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    String email = user.getEmail() != null ? user.getEmail() : "";
                    return ResponseEntity.ok(new AuthProfileResponse(user.getUsername(), email));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        if (user.getRole() == null) {
            user.setRole(User.Role.VIEWER);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * 浏览器地址栏访问会得到 GET，本接口实际只接受 POST；避免被误判为“静态资源不存在”。
     */
    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPasswordGet() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.ALLOW, HttpMethod.POST.name())
                .contentType(MediaType.TEXT_PLAIN)
                .body("此地址仅支持 POST（application/json），字段：email、newPassword、confirmPassword。请在页面表单提交，勿在地址栏打开。");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        String email = request.email() != null ? request.email().trim() : "";
        String newPassword = request.newPassword() != null ? request.newPassword() : "";
        String confirmPassword = request.confirmPassword() != null ? request.confirmPassword() : "";

        if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("请填写邮箱与新密码");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("两次输入的新密码不一致");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到该邮箱对应的用户");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("新密码不可以与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("密码已更新，请使用新密码登录。");
    }
}