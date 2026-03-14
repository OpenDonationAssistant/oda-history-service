alter table history add alerts jsonb;
update history set alerts = '{}'::jsonb;
