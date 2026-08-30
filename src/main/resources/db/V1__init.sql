create extension if not exists pgcrypto;

create table questions (
    id uuid primary key default gen_random_uuid(),
    topic text not null,
    difficulty text not null,
    question_text text not null,
    code_snippet text,
    options jsonb not null,
    correct_option_id text not null,
    explanations jsonb not null,
    content_hash text unique not null,
    created_at timestamptz default now()
);

create table user_seen_questions (
    user_id uuid not null,
    question_hash text not null,
    seen_at timestamptz default now(),
    answered_correctly boolean,
    primary key (user_id, question_hash)
);

create table daily_sets (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null,
    date date not null,
    question_ids uuid[] not null
);

create table user_stats (
    user_id uuid not null,
    topic text not null,
    correct_count int default 0,
    total_count int default 0,
    last_attempted timestamptz,
    primary key (user_id, topic)
);

create index idx_questions_topic on questions(topic);
create index idx_seen_user on user_seen_questions(user_id, seen_at desc);
create index idx_daily_sets_user_date on daily_sets(user_id, date desc);