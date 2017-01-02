# --- !Ups

ALTER TABLE organization ADD billing_id VARCHAR(40) DEFAULT null;

# --- !Downs

ALTER TABLE organization DROP COLUMN billing_id;