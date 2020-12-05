create table medium_meters(
  id bigint NOT NULL,
  active bit NOT NULL,
  number varchar(255) DEFAULT NULL,
  unit_name varchar(255) DEFAULT NULL,
  connection_id bigint DEFAULT NULL,
  PRIMARY KEY (id),
  KEY conId (connection_id),
  CONSTRAINT conId FOREIGN KEY (connection_id) REFERENCES medium_connections (id)
  )