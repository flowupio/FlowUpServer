# --- !Ups

ALTER TABLE organization ADD billing_id VARCHAR(40) DEFAULT null;
UPDATE organization SET billing_id=UUID() WHERE billing_id IS NULL;


# --- !Downs

ALTER TABLE organization DROP COLUMN billing_id;