# --- !Ups

ALTER TABLE api_key ADD number_of_allowed_uuids INT NOT NULL DEFAULT 50;
CREATE TABLE allowed_uuid(
  id                            varchar(40) not null,
  constraint pk_application primary key (id)
)
ALTER TABLE api_key ADD CONSTRAINT fk_allowed_uuid FOREIGN KEY (id) REFERENCES allowed_uuid (id) ON DELETE CASCADE ON UPDATE CASCADE

# --- !Downs

ALTER TABLE api_key DROP COLUMN number_of_allowed_uuids
DROP TABLE allowed_uuid
ALTER TABLE api_key DROP FOREIGN KEY fk_allowed_uuid