# --- !Ups

create table security_role (
  id                            varchar(40) not null,
  name                          varchar(255),
  constraint uq_security_role_name unique (name),
  constraint pk_security_role primary key (id)
);

create table user_security_role (
  user_id                       varchar(40) not null,
  security_role_id              varchar(40) not null,
  constraint pk_user_security_role primary key (user_id,security_role_id)
);

create table user_user_permission (
  user_id                       varchar(40) not null,
  user_permission_id            varchar(40) not null,
  constraint pk_user_user_permission primary key (user_id,user_permission_id)
);

create table user_permission (
  id                            varchar(40) not null,
  value                         varchar(255),
  constraint pk_user_permission primary key (id)
);

alter table user_security_role add constraint fk_user_security_role_user foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_user_security_role_user on user_security_role (user_id);

alter table user_security_role add constraint fk_user_security_role_security_role foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;
create index ix_user_security_role_security_role on user_security_role (security_role_id);

alter table user_user_permission add constraint fk_user_user_permission_user foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_user_user_permission_user on user_user_permission (user_id);

alter table user_user_permission add constraint fk_user_user_permission_user_permission foreign key (user_permission_id) references user_permission (id) on delete restrict on update restrict;
create index ix_user_user_permission_user_permission on user_user_permission (user_permission_id);



alter table user_security_role drop foreign key fk_user_security_role_user;
drop index ix_user_security_role_user on user_security_role;

alter table user_security_role drop foreign key fk_user_security_role_security_role;
drop index ix_user_security_role_security_role on user_security_role;

alter table user_user_permission drop foreign key fk_user_user_permission_user;
drop index ix_user_user_permission_user on user_user_permission;

alter table user_user_permission drop foreign key fk_user_user_permission_user_permission;
drop index ix_user_user_permission_user_permission on user_user_permission;

# --- !Downs

drop table if exists security_role;

drop table if exists user_security_role;

drop table if exists user_user_permission;

drop table if exists user_permission;

