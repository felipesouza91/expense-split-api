CREATE TABLE activities (
    id uuid NOT NULL,
    name text NOT NULL,
    activity_date timestamptz NOT NULL,
    created_at timestamptz NULL,
    updated_at timestamptz NULL,
    CONSTRAINT activities_pkey PRIMARY KEY (id)
);