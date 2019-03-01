
# TODO add correct mysql....

create table routes
(
  id                varchar(255) not null,
  created_by        varchar(255),
  created_date      datetime     not null,
  deleted            bit,
  updated_by        varchar(255),
  updated_date      datetime     not null,
  version           bigint,
  arrival_time      datetime,
  departure_time    datetime,
  destiny_city_name varchar(255),
  destiny_country   varchar(255),
  origin_city_name  varchar(255),
  origin_country    varchar(255),
  primary key (id)

) engine = InnoDB