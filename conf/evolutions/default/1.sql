# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table user (
  id                            varchar(40) not null,
  email                         varchar(255),
  name                          varchar(255),
  active                        tinyint(1) default 0,
  email_validated               tinyint(1) default 0,
  constraint uq_user_email unique (email),
  constraint pk_user primary key (id)
);


# --- !Downs

drop table if exists user;

