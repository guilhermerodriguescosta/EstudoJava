CREATE TABLE calculos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero1 DOUBLE PRECISION NOT NULL,
    numero2 DOUBLE PRECISION NOT NULL,
    operacao VARCHAR(20) NOT NULL,
    resultado DOUBLE PRECISION NOT NULL,
    CONSTRAINT uk_calculos_numeros_operacao UNIQUE (numero1, numero2, operacao),
    CONSTRAINT ck_calculos_operacao CHECK (operacao IN ('SOMA', 'SUBTRACAO', 'MULTIPLICACAO', 'DIVISAO'))
);

CREATE TABLE pedidos (
    id UUID PRIMARY KEY,
    descricao TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT ck_pedidos_status CHECK (status IN ('PENDING', 'PROCESSED'))
);
