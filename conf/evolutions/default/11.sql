# --- !Ups

alter table application add constraint fk_application_organization_id foreign key (organization_id) references organization (id) on delete restrict on update restrict;
create index ix_application_organization_id on application (organization_id);
create index ix_application_app_package on application (app_package);
# --- !Downs

alter table application drop foreign key fk_application_organization_id;
drop index ix_application_organization_id on application;
drop index ix_application_app_package on application;