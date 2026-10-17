CREATE TABLE customer (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(320) NOT NULL,
    phone      VARCHAR(32),
    status     VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version    BIGINT NOT NULL
);

CREATE UNIQUE INDEX uq_customer_email_lower ON customer (LOWER(email));
CREATE INDEX idx_customer_status ON customer (status);

CREATE TABLE address (
    id          UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    zip_code    VARCHAR(16) NOT NULL,
    street      VARCHAR(255) NOT NULL,
    number      VARCHAR(32) NOT NULL,
    complement  VARCHAR(255),
    district    VARCHAR(255) NOT NULL,
    city        VARCHAR(255) NOT NULL,
    state       VARCHAR(2) NOT NULL,
    country     VARCHAR(2) NOT NULL,
    is_default  BOOLEAN NOT NULL,
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE INDEX idx_address_customer ON address (customer_id);

-- Garante no máximo um endereço padrão por cliente.
-- Não usamos um UNIQUE INDEX parcial porque índice único no Postgres é checado
-- de imediato, a cada comando. Ao trocar o endereço padrão, o novo é marcado
-- antes de o antigo ser desmarcado no mesmo flush, então por um instante existem
-- dois padrões para o cliente e o índice recusaria a operação.
-- A constraint EXCLUDE pode ser DEFERRABLE: a checagem acontece só no commit,
-- quando o estado já está consistente (um padrão). O resultado é o mesmo de um
-- UNIQUE (customer_id) WHERE is_default, mas sem quebrar na troca de padrão.
ALTER TABLE address
    ADD CONSTRAINT uq_address_single_default
    EXCLUDE (customer_id WITH =) WHERE (is_default)
    DEFERRABLE INITIALLY DEFERRED;
