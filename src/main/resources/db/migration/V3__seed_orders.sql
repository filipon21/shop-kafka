INSERT INTO orders (id, customer_id, status, total, created_at)
VALUES (1, 1, 'PENDING', 489.97, CURRENT_TIMESTAMP);

INSERT INTO orders (id, customer_id, status, total, created_at)
VALUES (2, 2, 'FAILED', 899.00, CURRENT_TIMESTAMP);

INSERT INTO orders (id, customer_id, status, total, created_at)
VALUES (3, 3, 'PROCESSED', 499.00, CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_id, order_id, quantity, price)
VALUES (1, 1, 1, 2, 199.99);

INSERT INTO order_items (id, product_id, order_id, quantity, price)
VALUES (2, 2, 1, 1, 89.99);

INSERT INTO order_items (id, product_id, order_id, quantity, price)
VALUES (3, 3, 2, 1, 899.00);

INSERT INTO order_items (id, product_id, order_id, quantity, price)
VALUES (4, 4, 3, 2, 249.50);

ALTER TABLE orders ALTER COLUMN id RESTART WITH 100;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 100;
