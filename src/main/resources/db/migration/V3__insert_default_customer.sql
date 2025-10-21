-- V3: Insert default customer and their cart

INSERT INTO customer (firstname, lastname, email, address)
VALUES ('John', 'Doe', 'john@example.com', '123 Main St');

INSERT INTO cart (customer_id, created_at)
SELECT id, NOW() FROM customer WHERE email = 'john@example.com';
