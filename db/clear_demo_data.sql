USE inkspell_db;
START TRANSACTION;
DELETE FROM books WHERE is_demo=1 AND NOT EXISTS (SELECT 1 FROM user_library WHERE user_library.book_id=books.id);
DELETE FROM site_testimonials WHERE is_placeholder=1;
UPDATE books SET is_featured=1 WHERE id=(SELECT id FROM (SELECT id FROM books ORDER BY is_featured DESC,id LIMIT 1) chosen);
COMMIT;
