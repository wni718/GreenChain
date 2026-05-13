package com.greenchain.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/reset-password",
                                "/api/auth/account/**")
                        .permitAll()

                        // VIEWER - read-only access to public data
                        .requestMatchers(HttpMethod.GET, "/api/viewer/**").hasRole("VIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**")
                        .hasAnyRole("VIEWER", "SUSTAINABILITY_MANAGER", "SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/carbon/total")
                        .hasAnyRole("VIEWER", "SUSTAINABILITY_MANAGER", "SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/**")
                        .hasAnyRole("VIEWER", "SUSTAINABILITY_MANAGER", "SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/suppliers/**")
                        .hasAnyRole("VIEWER", "SUSTAINABILITY_MANAGER", "SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/recommend/**")
                        .hasAnyRole("VIEWER", "SUSTAINABILITY_MANAGER", "SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/transport-modes/**")
                        .hasAnyRole("VIEWER", "SUSTAINABILITY_MANAGER", "SUPPLIER", "ADMIN")

                        // SUPPLIER - can create/update their own shipments
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/**").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/shipments/*/update").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/**").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/suppliers").hasAnyRole("SUPPLIER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/me").hasAnyRole("SUPPLIER", "ADMIN")

                        // ADMIN - full access including audit logs
                        // Export endpoint is accessible to all authenticated users, other audit-log
                        // endpoints are ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/audit-logs/export").authenticated()
                        .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/suppliers/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/generate-users").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/link-user-manual").hasRole("ADMIN")

                        // SUSTAINABILITY_MANAGER - same as VIEWER plus some management capabilities
                        .requestMatchers("/api/carbon/calculate").hasAnyRole("SUSTAINABILITY_MANAGER", "ADMIN")
                        .requestMatchers("/api/carbon/optimize/**").hasAnyRole("SUSTAINABILITY_MANAGER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.realmName("GreenChain"));

        return http.build();
    }
}