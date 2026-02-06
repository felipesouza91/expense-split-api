
ALTER TABLE expense_participants ADD CONSTRAINT expense_participants_expense_id_fkey FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE;
ALTER TABLE expense_participants ADD CONSTRAINT expense_participants_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


ALTER TABLE expenses ADD CONSTRAINT expenses_activity_id_fkey FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE;
ALTER TABLE expenses ADD CONSTRAINT expenses_payer_id_fkey FOREIGN KEY (payer_id) REFERENCES users(id);


ALTER TABLE expense_payments ADD CONSTRAINT expense_payments_debtor_id_fkey FOREIGN KEY (debtor_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE expense_payments ADD CONSTRAINT expense_payments_expense_id_fkey FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE;