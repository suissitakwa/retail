-- V2: Seed Data Insertion — populate initial categories & products

-- Insert Categories
INSERT INTO category (id, name, description) VALUES
  (nextval('category_seq'), 'Women', 'Clothing for women'),
  (nextval('category_seq'), 'Men', 'Clothing for men'),
  (nextval('category_seq'), 'Accessories', 'Belts, hats, and more');

-- Insert Products
INSERT INTO product (id, name, description, image_url, price, category_id) VALUES
  (nextval('product_seq'), 'Red Dress', 'Elegant red dress.', '/images/red-dress.jpg', 79.99, (SELECT id FROM category WHERE name = 'Women')),
  (nextval('product_seq'), 'Blue Shirt', 'Stylish blue shirt.', '/images/blue-shirt.jpg', 49.99, (SELECT id FROM category WHERE name = 'Men')),
  (nextval('product_seq'), 'Leather Belt', 'Genuine leather belt.', '/images/leather-belt.jpg', 29.99, (SELECT id FROM category WHERE name = 'Accessories'));
