# --- !Ups

create table api_key (
  id                            varchar(40) not null,
  value                         varchar(255),
  constraint pk_api_key primary key (id)
);

create index ix_api_key_value on api_key (value);


# --- !Downs

drop table if exists api_key;

drop index ix_api_key_value on api_key;
