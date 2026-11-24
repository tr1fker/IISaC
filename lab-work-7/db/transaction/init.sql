CREATE SCHEMA transaction;
CREATE TABLE transaction.transactions (id varchar PRIMARY KEY, payment_id varchar NOT NULL, client_id varchar NOT NULL, amount numeric(15,2) NOT NULL, currency varchar NOT NULL, type varchar NOT NULL, created_at varchar NOT NULL);
INSERT INTO transaction.transactions VALUES ('t1','p1','u1',125.50,'BYN','PAYMENT','2026-01-10T12:01:00Z'),('t2','p1','u1',25.00,'BYN','REFUND','2026-01-12T09:00:00Z');
CREATE TABLE transaction.processed_events (event_id varchar PRIMARY KEY);
