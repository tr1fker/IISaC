CREATE SCHEMA account;
CREATE TABLE account.accounts (id varchar PRIMARY KEY, owner_id varchar NOT NULL, balance numeric(15,2) NOT NULL, currency varchar NOT NULL, status varchar NOT NULL);
INSERT INTO account.accounts VALUES ('a1','u1',500.00,'BYN','ACTIVE'),('a2','u2',125.50,'BYN','ACTIVE');
