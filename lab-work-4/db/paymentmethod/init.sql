CREATE SCHEMA paymentmethod;
CREATE TABLE paymentmethod.payment_methods (id varchar PRIMARY KEY, client_id varchar NOT NULL, type varchar NOT NULL, last_four varchar NOT NULL, status varchar NOT NULL);
INSERT INTO paymentmethod.payment_methods VALUES ('pm1','u1','CARD','4242','ACTIVE');
