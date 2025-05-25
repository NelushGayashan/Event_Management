-- V3__Create_attendance_table.sql
CREATE TABLE attendance (
    event_id UUID NOT NULL REFERENCES events(id),
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'GOING',
    responded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (event_id, user_id)
);
ALTER TABLE attendance
ADD COLUMN deleted_at TIMESTAMP NULL;

CREATE INDEX idx_attendance_deleted_at ON attendance(deleted_at);
CREATE INDEX idx_attendance_event_id ON attendance(event_id);
CREATE INDEX idx_attendance_user_id ON attendance(user_id);
CREATE INDEX idx_attendance_status ON attendance(status);
