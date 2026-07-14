package com.dms.repository;

import com.dms.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing user notification preferences.
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    /**
     * Finds notification preferences for a specific user.
     *
     * @param userId the user ID
     * @return Optional containing the preferences if found
     */
    Optional<NotificationPreference> findByUserId(Long userId);

    /**
     * Deletes notification preferences for a specific user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Checks if notification preferences exist for a specific user.
     *
     * @param userId the user ID
     * @return true if preferences exist, false otherwise
     */
    boolean existsByUserId(Long userId);
}