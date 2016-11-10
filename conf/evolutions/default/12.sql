# --- !Ups

ALTER TABLE api_key ADD number_of_allowed_uuids INT NOT NULL DEFAULT 50;
CREATE TABLE allowed_uuid(
  id                            VARCHAR(40) NOT NULL,
  creation_timestamp            DATETIME,
  CONSTRAINT pk_allowed_uuid PRIMARY KEY (id)
);

ALTER TABLE api_key ADD CONSTRAINT fk_allowed_uuid FOREIGN KEY (id) REFERENCES allowed_uuid (id) ON DELETE CASCADE;
CREATE INDEX created_at_index ON allowed_uuid (creation_timestamp);

# --- !Downs

ALTER TABLE api_key DROP COLUMN number_of_allowed_uuids;
DROP INDEX created_at_index ON allowed_uuid;
ALTER TABLE api_key DROP FOREIGN KEY fk_allowed_uuid;
DROP TABLE allowed_uuid;