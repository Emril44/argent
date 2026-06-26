create table transactions (
                        id uuid primary key,
                        source_id uuid,
                        destination_id uuid,
                        amount numeric(19, 2) not null check (amount > 0),
                        type varchar(20) not null,
                        status varchar(20) not null,
                        created_at timestamptz not null default now(),
                        processed_at timestamptz
);

create index idx_transactions_source_id on transactions(source_id);
create index idx_transactions_destination_id on transactions(destination_id);
