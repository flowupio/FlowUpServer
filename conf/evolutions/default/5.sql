# --- !Ups

ALTER TABLE organization ADD grafana_id varchar(255), ADD google_account varchar(255);

# --- !Downs

ALTER TABLE organization DROP COLUMN grafana_id, DROP COLUMN google_account;
