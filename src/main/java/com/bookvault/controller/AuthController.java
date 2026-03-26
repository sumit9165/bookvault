package com.bookvault.controller;

import com.bookvault.dto.ApiResponse;
import com.bookvault.dto.AuthDto;
import com.bookvault.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpServletResponse response) {

        AuthDto.AuthResponse authResponse = authService.login(request);
        setJwtCookie(response, authResponse.getAccessToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request,
            HttpServletResponse response) {

        AuthDto.AuthResponse authResponse = authService.register(request);
        setJwtCookie(response, authResponse.getAccessToken());
        return ResponseEntity.status(201).body(ApiResponse.created(authResponse, "Registration successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> refresh(
            @Valid @RequestBody AuthDto.RefreshTokenRequest request,
            HttpServletResponse response) {

        AuthDto.AuthResponse authResponse = authService.refreshToken(request);
        setJwtCookie(response, authResponse.getAccessToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (userDetails != null) {
            authService.logout(userDetails.getUsername());
        }
        clearJwtCookie(response);
        request.getSession(false);
        return ResponseEntity.ok(ApiResponse.message("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails instanceof com.bookvault.entity.User user) {
            AuthDto.UserInfo info = AuthDto.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(info));
        }
        return ResponseEntity.ok(ApiResponse.error("Not authenticated", 401));
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 hours
        response.addCookie(cookie);
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
