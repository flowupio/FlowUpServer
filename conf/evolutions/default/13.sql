# --- !Ups

CREATE INDEX delete_created_at_index ON allowed_uuid (created_at);

# --- !Downs

DROP INDEX delete_created_at_index ON allowed_uuid;