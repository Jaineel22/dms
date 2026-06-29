package com.dms.security.service;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a {@link UserDetails} by email (used as the Spring Security username).
     * The returned object carries the role as a {@link SimpleGrantedAuthority}.
     *
     * @param email the user's email address
     * @throws UsernameNotFoundException if no user exists with that email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException(
                            "User not found with email: " + email);
                });

        return buildUserDetails(user);
    }

    /**
     * Loads a {@link UserDetails} by the user's primary key.
     * Useful inside services that already resolved a userId from the JWT subject.
     *
     * @param userId the user's primary key
     * @throws UsernameNotFoundException if no user exists with that ID
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new UsernameNotFoundException(
                            "User not found with id: " + userId);
                });

        return buildUserDetails(user);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private UserDetails buildUserDetails(User user) {
        boolean accountNonLocked = isAccountNonLocked(user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(
                        new SimpleGrantedAuthority(user.getRole().getName())))
                .accountExpired(false)
                .accountLocked(!accountNonLocked)
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                .build();
    }

    private boolean isAccountNonLocked(User user) {
        if (user.getLockedUntil() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(user.getLockedUntil());
    }
}