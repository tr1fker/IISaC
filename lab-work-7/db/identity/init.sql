CREATE SCHEMA identity;
CREATE TABLE identity.users (id varchar PRIMARY KEY, name varchar NOT NULL, email varchar NOT NULL, role varchar NOT NULL);
INSERT INTO identity.users VALUES ('u1','Anna Ivanova','anna@example.com','CLIENT'),('u2','Pavel Petrov','pavel@example.com','MERCHANT');
CREATE UNIQUE INDEX users_email_unique ON identity.users (lower(email));
INSERT INTO identity.users(id,name,email,role) VALUES ('admin','Administrator','admin@example.com','ADMIN');
