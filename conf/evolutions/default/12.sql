# --- !Ups

ALTER TABLE api_key ADD number_of_allowed_uuids INT NOT NULL DEFAULT 50;
CREATE TABLE allowed_uuid(
  id                            VARCHAR(40) NOT NULL,
  installation_uuid             VARCHAR(40) NOT NULL,
  created_at            DATETIME,
  api_key_id                    VARCHAR(40) NOT NULL,
  CONSTRAINT pk_allowed_uuid PRIMARY KEY (id)
);

ALTER TABLE allowed_uuid ADD CONSTRAINT fk_allowed_uuid FOREIGN KEY (api_key_id) REFERENCES api_key (id) ON DELETE CASCADE;
CREATE INDEX created_at_index ON allowed_uuid (api_key_id, created_at);

# --- !Downs

ALTER TABLE api_key DROP COLUMN number_of_allowed_uuids;
ALTER TABLE allowed_uuid DROP FOREIGN KEY fk_allowed_uuid;
DROP INDEX created_at_index ON allowed_uuid;
DROP TABLE allowed_uuid;