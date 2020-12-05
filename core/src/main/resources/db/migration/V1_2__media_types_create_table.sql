drop table if exists readings;
drop table if exists medium_meters;
drop table if exists medium_connections;
drop table if exists media_types;

CREATE TABLE media_types (
  id bigint NOT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) 