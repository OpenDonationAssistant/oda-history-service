alter table history add actions jsonb;
update history set actions = '[]'::jsonb;
