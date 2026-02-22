alter table history rename column authorization_timestamp to event_timestamp;

alter table history add event_type varchar(255);
update history set event_type = 'payment';

alter table history rename column payment_id to origin_id;
update history set origin_id = external_id where external_id is not null;
alter table history drop column external_id;

alter table history add vote jsonb;
