CREATE TABLE insurers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    cnpj VARCHAR(14),
    active BOOLEAN NOT NULL
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(255),
    phone VARCHAR(50),
    birth_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE policies (
    id UUID PRIMARY KEY,
    policy_number VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    monthly_premium NUMERIC(19, 2),
    notes TEXT,
    customer_id UUID NOT NULL,
    insurer_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_policies_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_policies_insurer FOREIGN KEY (insurer_id) REFERENCES insurers (id)
);
