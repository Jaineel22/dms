package com.dms.service.impl;

import com.dms.dto.request.LoginRequest;
import com.dms.dto.response.AuthResponse;
import com.dms.dto.response.UserResponse;
import com.dms.entity.User;
import com.dms.exception.InvalidCredentialsException;
import com.dms.mapper.UserMapper;
import com.dms.repository.UserRepository;
import com.dms.security.jwt.JwtTokenProvider;
import com.dms.service.AuthService;
import com.dms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** Max consecutive failures before lockout. */
    private static final int  MAX_LOGIN_ATTEMPTS = 5;
    /** Lockout duration in minutes. */
    private static final long LOCKOUT_MINUTES    = 15L;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      tokenProvider;
    private final UserRepository        userRepository;
    private final UserMapper            userMapper;
    private final SecurityUtils         securityUtils;

    // ─── login ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Pre-auth guards
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException("Account is disabled. Please contact an administrator.");
        }
        if (isLockedOut(user)) {
            throw new InvalidCredentialsException(
                    "Account is temporarily locked due to too many failed login attempts. "
                  + "Please try again after " + LOCKOUT_MINUTES + " minutes.");
        }

        // Authenticate
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException ex) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException ex) {
            throw new InvalidCredentialsException("Account is disabled. Please contact an administrator.");
        } catch (LockedException ex) {
            throw new InvalidCredentialsException("Account is temporarily locked.");
        }

        // Success — reset counters and record login timestamp
        userRepository.resetLoginAttempts(user.getId());
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        String token = tokenProvider.generateToken(authentication);

        // Re-fetch to get fresh state for the response
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        UserResponse userResponse = userMapper.toResponse(updatedUser);

        log.info("User [{}] logged in successfully", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMillis())
                .user(userResponse)
                .build();
    }

    // ─── getCurrentUser ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = securityUtils.getCurrentUser();
        return userMapper.toResponse(user);
    }

    // ─── logout ───────────────────────────────────────────────────────────────

    @Override
    public void logout(String token) {
        // Stateless JWT — no server-side invalidation in Phase 1.
        // Token-blacklist (Redis) can be wired here in a future phase.
        log.debug("Logout called — token will expire naturally (stateless JWT).");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private boolean isLockedOut(User user) {
        return user.getLockedUntil() != null
                && LocalDateTime.now().isBefore(user.getLockedUntil());
    }

    private void handleFailedLogin(User user) {
        int attempts = (user.getLoginAttempts() == null ? 0 : user.getLoginAttempts()) + 1;
        userRepository.incrementLoginAttempts(user.getId());

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
            userRepository.lockUser(user.getId(), lockUntil);
            log.warn("User [{}] locked until [{}] after {} failed attempts",
                    user.getEmail(), lockUntil, attempts);
        } else {
            log.warn("Failed login attempt {} of {} for user [{}]",
                    attempts, MAX_LOGIN_ATTEMPTS, user.getEmail());
        }
    }
}