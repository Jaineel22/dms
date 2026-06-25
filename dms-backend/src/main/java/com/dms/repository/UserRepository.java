package com.dms.repository;

import com.dms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    // ─── Existence checks ────────────────────────────────────────────────────

    Boolean existsByEmail(String email);

    Boolean existsByEmployeeId(String employeeId);

    /** Used during update to exclude the record being updated from uniqueness check. */
    Boolean existsByEmailAndIdNot(String email, Long id);

    Boolean existsByEmployeeIdAndIdNot(String employeeId, Long id);

    // ─── List queries ─────────────────────────────────────────────────────────

    List<User> findByRoleId(Long roleId);

    List<User> findByDepartmentId(Long departmentId);

    List<User> findByIsActiveTrue();

    List<User> findByManagerId(Long managerId);

    // ─── Paginated queries ────────────────────────────────────────────────────

    Page<User> findByRoleNameAndIsActiveTrue(String roleName, Pageable pageable);

    Page<User> findByDepartmentIdAndIsActiveTrue(Long departmentId, Pageable pageable);

    Page<User> findAllByIsActiveTrue(Pageable pageable);

    // ─── Search ───────────────────────────────────────────────────────────────

    @Query("""
            SELECT u FROM User u
            WHERE u.isActive = true
              AND (
                    LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // ─── Count helpers ────────────────────────────────────────────────────────

    Long countByIsActiveTrue();

    Long countByDepartmentIdAndIsActiveTrue(Long departmentId);

    Long countByRoleId(Long roleId);

    // ─── Lockout helpers ──────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = u.loginAttempts + 1 WHERE u.id = :userId")
    void incrementLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockUser(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    // ─── Last login ───────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    // ─── Department membership check (for department delete guard) ────────────

    Boolean existsByDepartmentIdAndIsActiveTrue(Long departmentId);
}