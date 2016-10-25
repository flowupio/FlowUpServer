# --- !Ups

ALTER TABLE user ADD grafana_password varchar(255);

# --- !Downs

ALTER TABLE user DROP COLUMN grafana_password;
