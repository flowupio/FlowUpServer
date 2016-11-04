# --- !Ups

create table application (
  id                            varchar(40) not null,
  app_package                   varchar(255),
  organization_id               varchar(40),
  grafana_org_id                varchar(255),
  constraint pk_application primary key (id)
);

# --- !Downs

alter table application drop foreign key fk_application_organization_id;
drop index ix_application_organization_id on application;

drop table if exists application;

drop index ix_application_app_package on application;