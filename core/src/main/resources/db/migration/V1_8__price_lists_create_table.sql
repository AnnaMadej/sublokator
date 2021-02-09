drop table if exists price_lists;

create table price_lists(
id bigint AUTO_INCREMENT not null,
name varchar(255) not null unique,
valid_since date not null,
valid_until date not null,
primary key (id)
);

alter table price_lists
add column medium_id bigint not null;

alter table price_lists
add foreign key (medium_Id) references media (id);