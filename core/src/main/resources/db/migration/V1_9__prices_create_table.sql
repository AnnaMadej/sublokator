drop table if exists prices;

create table prices(
price_list_id bigint not null,
charge_id bigint not null,
name varchar(255) not null unique,
amount numeric not null,
tax_rate numeric not null,
PRIMARY KEY (price_list_id, charge_id)
);

alter table prices
add foreign key (price_list_id) references price_lists (id);

alter table prices
add foreign key (charge_id) references charges (id);