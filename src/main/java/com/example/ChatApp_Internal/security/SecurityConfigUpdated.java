//package com.example.ChatApp_Internal.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize
//@RequiredArgsConstructor
//public class SecurityConfigUpdated {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // Public endpoints
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/public/**").permitAll()
//                        .requestMatchers("/error").permitAll()
//                        .requestMatchers("/health").permitAll()
//
//                        // Admin endpoints - require ADMIN role
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//
//                        // User endpoints - require authentication
//                        .requestMatchers("/api/me/**").authenticated()
//                        .requestMatchers("/api/users/**").authenticated()
//                        .requestMatchers("/api/files/**").authenticated()
//
//                        // All other requests require authentication
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}
