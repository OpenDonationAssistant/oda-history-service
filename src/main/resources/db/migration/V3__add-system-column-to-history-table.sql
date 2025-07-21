alter table history add system varchar(255);
alter table history add external_id varchar(255);
update history set system = 'ODA';
