CREATE TABLE expenses (
    id uuid NOT NULL,
    name text NOT NULL,
    amount_in_cents int8 NOT NULL,
    payer_id uuid NULL,
    activity_id uuid NOT NULL,
    created_at timestamptz NULL,
    updated_at timestamptz NULL,
    CONSTRAINT expenses_pkey PRIMARY KEY (id)
);
