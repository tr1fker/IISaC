CREATE SCHEMA notification;
CREATE TABLE notification.notifications (id varchar PRIMARY KEY, recipient_id varchar NOT NULL, message varchar NOT NULL, status varchar NOT NULL, created_at varchar NOT NULL);
INSERT INTO notification.notifications VALUES ('n1','u1','Payment completed','SENT','2026-01-10T12:01:00Z');
CREATE TABLE notification.processed_events (event_id varchar PRIMARY KEY);
