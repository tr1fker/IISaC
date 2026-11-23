CREATE SCHEMA analytics;
CREATE TABLE analytics.payment_statistics (id varchar PRIMARY KEY, merchant_id varchar NOT NULL, period_from varchar NOT NULL, period_to varchar NOT NULL, payment_count int NOT NULL, total_amount numeric(15,2) NOT NULL, refund_amount numeric(15,2) NOT NULL, revenue numeric(15,2) NOT NULL);
INSERT INTO analytics.payment_statistics VALUES ('s1','m1','2026-01-01','2026-01-31',1,125.50,25.00,100.50),('s2','m1','2026-02-01','2026-02-28',0,0.00,0.00,0.00);
