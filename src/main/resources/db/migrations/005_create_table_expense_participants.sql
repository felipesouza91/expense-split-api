
CREATE TABLE expense_participants (
    id uuid NOT NULL,
    expense_id uuid NOT NULL,
    user_id uuid NOT NULL,
    amount_owed_in_cents int8 NOT NULL,
    added_at timestamptz NULL,
    CONSTRAINT expense_participants_pkey PRIMARY KEY (id),
    CONSTRAINT "uq:expense_participants.expense_id+expense_participants.user_id" UNIQUE (expense_id, user_id)
);


