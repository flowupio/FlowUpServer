# --- !Ups

create table organization (
  id                            varchar(40) not null,
  name                          varchar(255),
  api_key_id                    varchar(40),
  constraint uq_organization_api_key_id unique (api_key_id),
  constraint pk_organization primary key (id)
);

create table organization_user (
  organization_id               varchar(40) not null,
  user_id                       varchar(40) not null,
  constraint pk_organization_user primary key (organization_id,user_id)
);

alter table organization add constraint fk_organization_api_key_id foreign key (api_key_id) references api_key (id) on delete restrict on update restrict;

alter table organization_user add constraint fk_organization_user_organization foreign key (organization_id) references organization (id) on delete restrict on update restrict;
create index ix_organization_user_organization on organization_user (organization_id);

alter table organization_user add constraint fk_organization_user_user foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_organization_user_user on organization_user (user_id);

# --- !Downs

alter table organization drop foreign key fk_organization_api_key_id;

alter table organization_user drop foreign key fk_organization_user_organization;
drop index ix_organization_user_organization on organization_user;

alter table organization_user drop foreign key fk_organization_user_user;
drop index ix_organization_user_user on organization_user;

drop table if exists organization;

drop table if exists organization_user;