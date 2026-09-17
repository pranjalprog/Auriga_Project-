package com.auriga.clinic.repository;

import com.auriga.clinic.model.Notification;
import com.auriga.clinic.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    // Check before insert to avoid duplicate notifications
    boolean existsByAppointmentIdAndType(
            Long appointmentId,
            NotificationType type
    );
}