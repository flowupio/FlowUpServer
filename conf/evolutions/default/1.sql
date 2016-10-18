# --- !Ups

create table linked_account (
  id                            varchar(40) not null,
  user_id                       varchar(40),
  provider_user_id              varchar(255),
  provider_key                  varchar(255),
  constraint pk_linked_account primary key (id)
);

create table user (
  id                            varchar(40) not null,
  email                         varchar(255),
  name                          varchar(255),
  active                        tinyint(1) default 0,
  email_validated               tinyint(1) default 0,
  constraint uq_user_email unique (email),
  constraint pk_user primary key (id)
);

alter table linked_account add constraint fk_linked_account_user_id foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_linked_account_user_id on linked_account (user_id);


# --- !Downs

alter table linked_account drop foreign key fk_linked_account_user_id;
drop index ix_linked_account_user_id on linked_account;

drop table if exists linked_account;

drop table if exists user;

