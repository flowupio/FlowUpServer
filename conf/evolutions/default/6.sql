# --- !Ups

ALTER TABLE user ADD grafana_user_id varchar(20);

# --- !Downs

ALTER TABLE user DROP COLUMN grafana_user_id;
