-- V1: Schema Initialization — Create all tables and sequences

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS category_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS product_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS customer_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS inventory_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS cart_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS cart_items_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS retail_order_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS order_item_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS payment_seq INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS notification_seq INCREMENT BY 50;

-- Create tables
CREATE TABLE IF NOT EXISTS  customer (
    id SERIAL PRIMARY KEY,
    firstname VARCHAR(255),
    lastname VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    address VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS category (
    id          INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('category_seq'),
    name        VARCHAR(255),
    description TEXT
);

CREATE TABLE IF NOT EXISTS product (
    id                  INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('product_seq'),
    name                VARCHAR(255),
    description         TEXT,
    image_url           TEXT,
    price               NUMERIC(10,2),
    category_id         INTEGER REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS inventory (
    id          INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('inventory_seq'),
    product_id  INTEGER UNIQUE REFERENCES product(id),
    quantity    INTEGER NOT NULL CHECK (quantity >= 0),
    last_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart (
    id          INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('cart_seq'),
    customer_id INTEGER UNIQUE REFERENCES customer(id),
    created_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id          INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('cart_items_seq'),
    cart_id     INTEGER REFERENCES cart(id),
    product_id  INTEGER REFERENCES product(id),
    quantity    INTEGER,
    price       NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS retail_order (
    id                INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('retail_order_seq'),
    reference         VARCHAR(255) NOT NULL UNIQUE,
    total_amount      NUMERIC(10,2),
    payment_method    VARCHAR(50),
    stripe_session_id VARCHAR(255),
    status VARCHAR(50),
    customer_id       INTEGER NOT NULL REFERENCES customer(id),
    created_date      TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id          INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('order_items_seq'),
    order_id    INTEGER REFERENCES retail_order(id),
    product_id  INTEGER REFERENCES product(id),
    quantity    INTEGER,
    unit_price  NUMERIC(10,2)
);

CREATE TABLE IF NOT EXISTS payment (
    id         INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('payment_seq'),
    order_id   INTEGER UNIQUE REFERENCES retail_order(id),
    method     VARCHAR(50),
    amount     NUMERIC(10,2),
    status     VARCHAR(50),
    stripe_session_id VARCHAR(255),
     stripe_payment_intent_id VARCHAR(255),
    paid_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification (
    id         INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('notification_seq'),
    order_id   INTEGER REFERENCES retail_order(id),
    message    TEXT,
    sent_at    TIMESTAMP
);
