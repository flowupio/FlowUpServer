# --- !Ups

CREATE UNIQUE INDEX application_by_package_and_org_id ON application (app_package, organization_id);

# --- !Downs

DROP INDEX application_by_package_and_org_id ON application;