package com.dms.service.impl;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists failed-login bookkeeping (attempt counter + lockout) in its own,
 * independent transaction.
 *
 * <p>{@code AuthServiceImpl.login()} throws {@code InvalidCredentialsException}
 * right after recording a failed attempt, in the same {@code @Transactional}
 * method. Since that's an unchecked exception, Spring's default rollback rule
 * would roll back the entire transaction — including the attempt-counter and
 * lockout writes the throw is trying to report. Calling into this bean (a
 * separate proxy, {@code REQUIRES_NEW}) commits those writes independently of
 * whatever the caller's transaction ultimately does.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    /** Max consecutive failures before lockout. */
    private static final int  MAX_LOGIN_ATTEMPTS = 5;
    /** Lockout duration in minutes. */
    private static final long LOCKOUT_MINUTES    = 15L;

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(User user) {
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
