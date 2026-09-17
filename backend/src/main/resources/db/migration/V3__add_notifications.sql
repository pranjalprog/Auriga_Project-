CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments(id),
    type VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    -- Prevents sending the same reminder twice for the same appointment
    CONSTRAINT uq_notification_appt_type UNIQUE (appointment_id, type)
);