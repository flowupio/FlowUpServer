# --- !Ups

ALTER TABLE api_key ADD min_android_sdk_supported VARCHAR(40) DEFAULT null;

# --- !Downs

ALTER TABLE api_key DROP COLUMN min_android_sdk_supported;