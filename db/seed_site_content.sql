USE inkspell_db;
INSERT IGNORE INTO site_content VALUES
 ('featured_badge','THE HIGH LIBRARIAN\'S CHOSEN TOME'),
 ('intro','A quiet corner of the world, entirely your own. Gather the stories that call to you, linger between their pages, and leave a little of yourself in every chapter.'),
 ('recommendations','Your next favorite story is already waiting. Your favorite genres and reading journeys help us find a doorway that feels like yours.');
INSERT INTO site_testimonials(quote,name,label,is_placeholder,sort_order)
 SELECT 'A place for the stories I carry with me, and the ones I have yet to find.','Elara Vale','Fictional fantasy reader',1,1 WHERE NOT EXISTS (SELECT 1 FROM site_testimonials WHERE name='Elara Vale');
INSERT INTO site_testimonials(quote,name,label,is_placeholder,sort_order)
 SELECT 'There is something lovely about watching a small shelf become a map of your reading life.','Rowan Ash','Fictional mystery reader',1,2 WHERE NOT EXISTS (SELECT 1 FROM site_testimonials WHERE name='Rowan Ash');
INSERT INTO site_testimonials(quote,name,label,is_placeholder,sort_order)
 SELECT 'One more chapter, one more doorway. This is my little ritual at the end of the day.','Mira Wren','Fictional classics reader',1,3 WHERE NOT EXISTS (SELECT 1 FROM site_testimonials WHERE name='Mira Wren');
