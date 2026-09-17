package com.auriga.clinic.service;

import com.auriga.clinic.model.*;
import com.auriga.clinic.repository.AppointmentRepository;
import com.auriga.clinic.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final AppointmentRepository appointmentRepo;
    private final NotificationRepository notificationRepo;

    public NotificationService(AppointmentRepository appointmentRepo,
                                NotificationRepository notificationRepo) {
        this.appointmentRepo = appointmentRepo;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    public void sendTodaysReminders(LocalDateTime clockNow) {
        LocalDate today = clockNow.toLocalDate();

        List<Appointment> todaysAppointments = appointmentRepo.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
                .filter(a -> a.getStartTime().toLocalDate().equals(today))
                .toList();

        for (Appointment appt : todaysAppointments) {

            // Check before inserting to avoid duplicate notifications
            boolean alreadySent = notificationRepo
                    .existsByAppointmentIdAndType(
                            appt.getId(),
                            NotificationType.APPOINTMENT_REMINDER
                    );

            if (alreadySent) {
                continue;
            }

            Notification notification = Notification.builder()
                    .appointmentId(appt.getId())
                    .type(NotificationType.APPOINTMENT_REMINDER)
                    .message(
                            "Reminder: " + appt.getPatient().getName() +
                            " has an appointment with " +
                            appt.getDoctor().getName() +
                            " at " + appt.getStartTime()
                    )
                    .createdAt(clockNow)
                    .build();

            notificationRepo.save(notification);
        }
    }

    public List<Notification> getOutbox() {
        return notificationRepo.findAllByOrderByCreatedAtDesc();
    }
}