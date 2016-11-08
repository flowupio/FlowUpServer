# --- !Ups

ALTER TABLE api_key ADD enabled tinyint(1) DEFAULT 1;

# --- !Downs

ALTER TABLE api_key DROP COLUMN enabled;