
drop table if exists routes;

create table if not exists routes
(
  id                varchar(255) not null
    primary key,
  created_by        varchar(255) null,
  created_date      datetime     null,
  deleted           bit          null,
  updated_by        varchar(255) null,
  updated_date      datetime     null,
  version           bigint       null,
  arrival_time      datetime     null,
  departure_time    datetime     null,
  destiny_city_name varchar(255) null,
  destiny_country   varchar(255) null,
  origin_city_name  varchar(255) null,
  origin_country    varchar(255) null
);


create index origin_info_idx on routes (origin_city_name, origin_country);

create index origin_country_idx on routes (origin_country);
create index destiny_country_idx on routes (destiny_country);

