-- Extension jo range-overlap checks ko GiST index ke through allow karta hai
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Sirf active (BOOKED) appointments pe constraint lagana hai,
-- cancelled appointments ko dobara book karne dena hai
ALTER TABLE appointments
ADD CONSTRAINT no_overlapping_appointments
EXCLUDE USING gist (
    doctor_id WITH =,
    tsrange(start_time, end_time) WITH &&
) WHERE (status = 'BOOKED');