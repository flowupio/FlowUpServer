# --- !Ups

create table application (
  id                            varchar(40) not null,
  app_package                   varchar(255),
  organization_id               varchar(40),
  grafana_org_id                varchar(255),
  constraint pk_application primary key (id)
);

# --- !Downs

drop table if exists application;
