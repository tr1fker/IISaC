CREATE SCHEMA payment;
CREATE TABLE payment.payments (id varchar PRIMARY KEY, client_id varchar NOT NULL, merchant_id varchar NOT NULL, payment_method_id varchar NOT NULL, amount numeric(15,2) NOT NULL, currency varchar NOT NULL, status varchar NOT NULL, description varchar, created_at varchar NOT NULL);
INSERT INTO payment.payments VALUES ('p1','u1','m1','pm1',125.50,'BYN','SUCCESS','Order 1001','2026-01-10T12:00:00Z'),('p2','u1','m1','pm1',45.00,'BYN','CREATED','Order 1002','2026-02-15T10:30:00Z');
CREATE TABLE payment.outbox (event_id varchar PRIMARY KEY, payment_id varchar NOT NULL, payload text NOT NULL, created_at timestamptz NOT NULL DEFAULT now(), published boolean NOT NULL DEFAULT false);
CREATE TABLE payment.operations (payment_id varchar PRIMARY KEY, target_status varchar NOT NULL, state varchar NOT NULL, last_error varchar);
