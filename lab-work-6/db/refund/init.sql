CREATE SCHEMA refund;
CREATE TABLE refund.refunds (id varchar PRIMARY KEY, payment_id varchar NOT NULL, amount numeric(15,2) NOT NULL, currency varchar NOT NULL, status varchar NOT NULL, created_at varchar NOT NULL);
INSERT INTO refund.refunds VALUES ('r1','p1',25.00,'BYN','SUCCESS','2026-01-12T09:00:00Z');
CREATE TABLE refund.outbox (event_id varchar PRIMARY KEY, payment_id varchar NOT NULL, payload text NOT NULL, created_at timestamptz NOT NULL DEFAULT now(), published boolean NOT NULL DEFAULT false);
