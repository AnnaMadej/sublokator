drop table if exists media;

create table media(
id bigint AUTO_INCREMENT not null,
name varchar(255) not null unique,
primary key (id)
);

alter table MEDIUM_CONNECTIONS
add column medium_id bigint not null;

alter table MEDIUM_CONNECTIONS
add foreign key (medium_Id) references media (id);