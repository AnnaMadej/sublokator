drop table if exists hibernate_sequence;

CREATE TABLE hibernate_sequence (
  next_val bigint DEFAULT NULL
);

insert into hibernate_sequence (next_val) values (1);