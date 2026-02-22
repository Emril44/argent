create table users (
                       id uuid primary key,
                       full_name varchar(200) not null,
                       email varchar(320) not null unique,
                       password_hash varchar(255) not null,
                       status varchar(20) not null,
                       created_at timestamptz not null default now()
);

create table wallets (
                         id uuid primary key,
                         owner_id uuid not null references users(id),
                         label varchar(100),
                         balance numeric(19, 2) not null default 0,
                         status varchar(20) not null,
                         created_at timestamptz not null default now()
);

create index idx_wallets_owner_id on wallets(owner_id);
