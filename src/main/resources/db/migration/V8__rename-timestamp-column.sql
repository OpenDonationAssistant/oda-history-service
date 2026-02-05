alter table history rename column authorization_timestamp to timestamp;
alter table history add event varchar(255);
update history set event = 'payment';
