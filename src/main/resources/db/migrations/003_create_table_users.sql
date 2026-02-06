CREATE TABLE users (
    id uuid NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    password_hash text NOT NULL,
    created_at timestamptz NULL,
    updated_at timestamptz NULL,
    CONSTRAINT "uq:users.email" UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);