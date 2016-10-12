# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table users (
  id                            uuid not null,
  email                         varchar(255),
  name                          varchar(255),
  active                        boolean,
  email_validated               boolean,
  constraint uq_users_email unique (email),
  constraint pk_users primary key (id)
);


# --- !Downs

drop table if exists users;

