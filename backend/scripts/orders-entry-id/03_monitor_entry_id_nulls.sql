SELECT COUNT(*) AS null_entry_id_count
FROM orders
WHERE entry_id IS NULL;

SELECT
    o.id,
    o.order_number,
    o.user_id,
    o.event_course_id,
    o.event_pace_id,
    o.order_status,
    o.created_at
FROM orders o
WHERE o.entry_id IS NULL
ORDER BY o.created_at ASC
LIMIT 20;

SELECT
    o.order_status,
    COUNT(*) AS null_entry_id_rows
FROM orders o
WHERE o.entry_id IS NULL
GROUP BY o.order_status
ORDER BY null_entry_id_rows DESC, o.order_status ASC;

SELECT
    o.id,
    o.order_number,
    o.user_id,
    o.event_course_id,
    o.event_pace_id,
    o.created_at
FROM orders o
LEFT JOIN event_course ec
    ON ec.id = o.event_course_id
LEFT JOIN entry e
    ON e.user_id = o.user_id
   AND e.event_id = ec.event_id
WHERE o.entry_id IS NULL
  AND e.id IS NULL
ORDER BY o.created_at ASC
LIMIT 20;
