alter table history add metadata jsonb;
update history set metadata = '{}'::jsonb;
