CREATE SCHEMA merchant;
CREATE TABLE merchant.merchants (id varchar PRIMARY KEY, user_id varchar NOT NULL, name varchar NOT NULL, status varchar NOT NULL);
INSERT INTO merchant.merchants VALUES ('m1','u2','Online Store','ACTIVE');
