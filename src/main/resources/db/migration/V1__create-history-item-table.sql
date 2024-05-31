create table history(
  id varchar(255) not null,
  payment_id varchar(255),
  nickname varchar(255),
  recipient_id varchar(255),
  amount varchar(255),
  authorization_timestamp timestamp with time zone,
  message text,
  attachments jsonb,
  goals jsonb,
  reel_results jsonb
)
