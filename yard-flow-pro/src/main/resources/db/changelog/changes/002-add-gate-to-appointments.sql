-- src/main/resources/db/changelog/changes/002-add-gate-to-appointments.sql
-- liquibase formatted sql

-- changeset luvkush:yardflowpro id:002-add-gate-to-appointments
-- comment: Add gate references to appointments table

ALTER TABLE appointments ADD COLUMN IF NOT EXISTS check_in_gate_id BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS check_out_gate_id BIGINT;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS completion_time TIMESTAMP;

-- Add foreign key constraints
-- First check if the constraint exists before adding it
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_appointment_check_in_gate') THEN
    ALTER TABLE appointments 
      ADD CONSTRAINT fk_appointment_check_in_gate 
      FOREIGN KEY (check_in_gate_id) REFERENCES gates(id);
  END IF;
  
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_appointment_check_out_gate') THEN
    ALTER TABLE appointments 
      ADD CONSTRAINT fk_appointment_check_out_gate 
      FOREIGN KEY (check_out_gate_id) REFERENCES gates(id);
  END IF;
END $$;