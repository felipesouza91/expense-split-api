
CREATE TABLE expense_payments (
id uuid NOT NULL,
expense_id uuid NOT NULL,
debtor_id uuid NOT NULL,
amount_paid_in_cents int8 NOT NULL,
paid_at timestamptz NULL,
CONSTRAINT expense_payments_pkey PRIMARY KEY (id)
);


-- public.expense_payments foreign keys
