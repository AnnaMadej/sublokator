CREATE TABLE readings (
  id bigint NOT NULL,
  date date DEFAULT NULL,
  reading double DEFAULT NULL,
  meter_id bigint DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (meter_id) REFERENCES medium_meters (id)
)