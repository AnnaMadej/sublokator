alter table medium_meters
add active_since date not null;

alter table medium_meters
add active_until date default null;