UPDATE insurers
SET cnpj = '11222333000181'
WHERE id = '11111111-1111-1111-1111-111111111111';

UPDATE insurers
SET cnpj = '04252011000110'
WHERE id = '22222222-2222-2222-2222-222222222222';

UPDATE customers
SET cpf = '52998224725'
WHERE id = '33333333-3333-3333-3333-333333333333';

UPDATE customers
SET cpf = '39053344705'
WHERE id = '44444444-4444-4444-4444-444444444444';

UPDATE policies
SET notes = 'Apolice auto anual.'
WHERE id = '55555555-5555-5555-5555-555555555555';

UPDATE policies
SET notes = 'Cobertura vida padrao.'
WHERE id = '66666666-6666-6666-6666-666666666666';

CREATE UNIQUE INDEX IF NOT EXISTS uk_insurers_cnpj
    ON insurers (cnpj)
    WHERE cnpj IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_policies_status
    ON policies (status);

CREATE INDEX IF NOT EXISTS idx_policies_type
    ON policies (type);

CREATE INDEX IF NOT EXISTS idx_policies_start_date
    ON policies (start_date);

CREATE INDEX IF NOT EXISTS idx_policies_end_date
    ON policies (end_date);

CREATE INDEX IF NOT EXISTS idx_policies_customer_id
    ON policies (customer_id);

CREATE INDEX IF NOT EXISTS idx_policies_insurer_id
    ON policies (insurer_id);

ALTER TABLE insurers
    ADD CONSTRAINT chk_insurers_cnpj_digits
    CHECK (cnpj IS NULL OR cnpj ~ '^[0-9]{14}$');

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_cpf_digits
    CHECK (cpf ~ '^[0-9]{11}$');

ALTER TABLE policies
    ADD CONSTRAINT chk_policies_date_range
    CHECK (end_date >= start_date);

ALTER TABLE policies
    ADD CONSTRAINT chk_policies_monthly_premium_positive
    CHECK (monthly_premium IS NULL OR monthly_premium > 0);
