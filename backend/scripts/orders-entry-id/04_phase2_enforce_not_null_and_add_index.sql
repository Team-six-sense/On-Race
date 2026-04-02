ALTER TABLE orders
    MODIFY COLUMN entry_id BIGINT NOT NULL;

CREATE INDEX idx_orders_user_id_order_status_entry_id
    ON orders (user_id, order_status, entry_id);
