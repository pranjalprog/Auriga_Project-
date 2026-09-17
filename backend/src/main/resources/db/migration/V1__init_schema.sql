CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100)
);

CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20)
);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id),
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    cancelled_at TIMESTAMP,
    cancellation_fee NUMERIC(10,2) DEFAULT 0,
    CONSTRAINT chk_time_order CHECK (end_time > start_time)
);

CREATE INDEX idx_appt_doctor_time ON appointments (doctor_id, start_time);
CREATE INDEX idx_patient_name ON patients (name);
