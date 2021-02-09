drop table if exists charges;

create table charges(
id bigint AUTO_INCREMENT not null,
name varchar(255) not null unique,
measurable bit not null,
primary key (id)
);

alter table charges
add column medium_id bigint not null;

alter table charges
add foreign key (medium_Id) references media (id);