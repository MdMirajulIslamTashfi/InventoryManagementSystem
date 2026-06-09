CREATE TABLE IF NOT EXISTS customer (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    gender        VARCHAR(10)  NOT NULL,
    date_of_birth DATE         NOT NULL,
    address       VARCHAR(255) NOT NULL,
    contact       VARCHAR(11)  NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL
    );