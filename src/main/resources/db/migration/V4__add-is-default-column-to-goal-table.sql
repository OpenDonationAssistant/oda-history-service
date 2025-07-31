alter table goal add is_default boolean;
update goal set is_default = false;
