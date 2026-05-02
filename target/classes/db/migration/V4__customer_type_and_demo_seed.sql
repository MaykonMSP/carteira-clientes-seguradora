ALTER TABLE customers
    ADD COLUMN customer_type VARCHAR(20) NOT NULL DEFAULT 'PESSOA_FISICA';

ALTER TABLE customers
    ADD COLUMN cnpj VARCHAR(14);

ALTER TABLE customers
    ALTER COLUMN cpf DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_customers_cnpj
    ON customers (cnpj)
    WHERE cnpj IS NOT NULL;

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_type
    CHECK (customer_type IN ('PESSOA_FISICA', 'PESSOA_JURIDICA'));

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_cnpj_digits
    CHECK (cnpj IS NULL OR cnpj ~ '^[0-9]{14}$');

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_document_by_type
    CHECK (
        (customer_type = 'PESSOA_FISICA' AND cpf IS NOT NULL AND cnpj IS NULL)
        OR
        (customer_type = 'PESSOA_JURIDICA' AND cnpj IS NOT NULL AND cpf IS NULL)
    );

INSERT INTO insurers (id, name, cnpj, active)
VALUES
    ('88888888-8888-8888-8888-888888888888', 'Horizonte Seguros', '12345678000195', true),
    ('99999999-9999-9999-9999-999999999999', 'Norte Sul Companhia de Seguros', '22345678000149', true),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Safra Vida e Saude Seguros', '32345678000100', true);

INSERT INTO customers (id, full_name, customer_type, cpf, cnpj, email, phone, birth_date, created_at, updated_at)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Marina Costa', 'PESSOA_FISICA', '11144477735', NULL, 'marina.costa@example.test', '+55 11 91000-0101', '1988-11-08', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Roberto Almeida', 'PESSOA_FISICA', '29537958806', NULL, 'roberto.almeida@example.test', '+55 21 92000-0202', '1976-04-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Clara Menezes', 'PESSOA_FISICA', '15481420701', NULL, 'clara.menezes@example.test', '+55 31 93000-0303', '1995-09-02', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Oficina Modelo Ltda', 'PESSOA_JURIDICA', NULL, '42345678000156', 'contato@oficinamodelo.example.test', '+55 41 94000-0404', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Clinica Ficticia Bem Estar Ltda', 'PESSOA_JURIDICA', NULL, '52345678000100', 'administrativo@clinicabemestar.example.test', '+55 51 95000-0505', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO policies (id, policy_number, type, status, start_date, end_date, monthly_premium, notes, customer_id, insurer_id, created_at, updated_at)
VALUES
    ('10101010-1010-1010-1010-101010101010', 'POL-2026-0004', 'AUTO', 'VIGENTE', CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE + INTERVAL '325 days', 245.90, 'Seguro auto completo para cliente pessoa fisica.', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '88888888-8888-8888-8888-888888888888', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('20202020-2020-2020-2020-202020202020', 'POL-2026-0005', 'RESIDENCIAL', 'VENCIDA', CURRENT_DATE - INTERVAL '420 days', CURRENT_DATE - INTERVAL '55 days', 89.70, 'Apolice residencial encerrada para demonstracao.', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('30303030-3030-3030-3030-303030303030', 'POL-2026-0006', 'EMPRESARIAL', 'VIGENTE', CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE + INTERVAL '18 days', 890.00, 'Seguro empresarial proximo do vencimento.', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '99999999-9999-9999-9999-999999999999', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('40404040-4040-4040-4040-404040404040', 'POL-2026-0007', 'VIDA', 'VIGENTE', CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '710 days', 132.45, 'Seguro de vida individual vigente.', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50505050-5050-5050-5050-505050505050', 'POL-2026-0008', 'SAUDE', 'VIGENTE', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '25 days', 1490.00, 'Plano de saude empresarial proximo do vencimento.', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('60606060-6060-6060-6060-606060606060', 'POL-2026-0009', 'AUTO', 'VENCIDA', CURRENT_DATE - INTERVAL '500 days', CURRENT_DATE - INTERVAL '120 days', 218.35, 'Seguro auto vencido para teste de filtros.', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('70707070-7070-7070-7070-707070707070', 'POL-2026-0010', 'RESIDENCIAL', 'VIGENTE', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '7 days', 74.90, 'Seguro residencial proximo do vencimento.', '44444444-4444-4444-4444-444444444444', '99999999-9999-9999-9999-999999999999', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
