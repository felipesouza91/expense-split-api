CREATE TABLE activity_participants (
    id uuid NOT NULL,
    activity_id uuid NOT NULL,
    user_id uuid NOT NULL,
    joined_at timestamptz NULL,
    CONSTRAINT activity_participants_pkey PRIMARY KEY (id),
    CONSTRAINT "uq:activity_participants.activity_id+activity_participants.user" UNIQUE (activity_id, user_id)
);