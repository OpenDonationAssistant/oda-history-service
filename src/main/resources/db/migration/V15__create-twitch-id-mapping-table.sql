create table twitch_id_mapping(
  recipient_id varchar(255) not null,
  refresh_token_id UUID not null,
  constraint twitch_id_mapping_pkey primary key (recipient_id)
);
