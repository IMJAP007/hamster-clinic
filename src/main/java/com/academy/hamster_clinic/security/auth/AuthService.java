package com.academy.hamster_clinic.security.auth;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.model.entity.Role;
import com.academy.hamster_clinic.model.entity.User;
import com.academy.hamster_clinic.model.repository.RoleRepository;
import com.academy.hamster_clinic.model.repository.UserRepository;
import com.academy.hamster_clinic.security.JwtService;
import com.academy.hamster_clinic.security.auth.dto.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("Пользователь с таким логином уже существует");
        }

        Role defaultRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseThrow(() -> new IllegalStateException(
                        "Роль ROLE_DOCTOR не найдена — проверьте миграцию V4"));

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName());
        user.setEnabled(true);
        user.setRoles(Set.of(defaultRole));

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, null); // Новое семейство

        return buildResponse(user, accessToken, refreshToken);
    }


    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        User user = (User) auth.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, null); // Новое семейство

        return buildResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RefreshResult result = refreshTokenService.rotateRefreshToken(rawRefreshToken);

        User user = result.user();
        String newAccessToken = jwtService.generateToken(user);

        return buildResponse(user, newAccessToken, result.newRefreshToken());
    }

    private AuthResponse buildResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken, 
                refreshToken, 
                "Bearer",
                user.getUsername(),
                user.getFullName(),
                user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}