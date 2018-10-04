# --- !Ups

ALTER TABLE user ADD created_at DATETIME;

# --- !Downs

ALTER TABLE user DROP COLUMN created_at;
