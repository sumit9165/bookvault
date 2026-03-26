package com.bookvault.service;

import com.bookvault.dto.AuthDto;

public interface AuthService {
    AuthDto.AuthResponse login(AuthDto.LoginRequest request);
    AuthDto.AuthResponse register(AuthDto.RegisterRequest request);
    AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request);
    void logout(String username);
}
