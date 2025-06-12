package com.example.notificationservice.repositories;

import com.example.notificationservice.models.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}