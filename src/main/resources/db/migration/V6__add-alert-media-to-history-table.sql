alter table history add alert_media jsonb;
update history set alert_media = '{}'::jsonb;
