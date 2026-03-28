UPDATE orders o
JOIN event_course ec
    ON ec.id = o.event_course_id
JOIN entry e
    ON e.user_id = o.user_id
   AND e.event_id = ec.event_id
SET o.entry_id = e.id
WHERE o.entry_id IS NULL;
