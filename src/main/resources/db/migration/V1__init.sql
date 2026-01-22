create table registrations (
  id bigserial primary key,
  username varchar(50) not null unique,
  created_at timestamp not null default now()
);

create table contact_messages (
  id bigserial primary key,
  full_name varchar(50) not null,
  email varchar(100) not null,
  phone varchar(10) not null,
  birth_date date not null,
  message varchar(500) not null,
  created_at timestamp not null default now()
);
