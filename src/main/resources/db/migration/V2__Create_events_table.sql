CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    host_id UUID NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    location VARCHAR(500),
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,

    CONSTRAINT fk_events_host FOREIGN KEY (host_id) REFERENCES users(id),
    CONSTRAINT chk_event_times CHECK (end_time > start_time)
);

CREATE INDEX idx_events_host_id ON events(host_id);
CREATE INDEX idx_events_start_time ON events(start_time);
CREATE INDEX idx_events_visibility ON events(visibility);
CREATE INDEX idx_events_location ON events(location);
CREATE INDEX idx_events_deleted_at ON events(deleted_at);