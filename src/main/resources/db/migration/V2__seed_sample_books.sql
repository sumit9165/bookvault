-- ═══════════════════════════════════════════════════════════════
-- BookVault - V2 Sample Seed Data (Development Only)
-- ═══════════════════════════════════════════════════════════════

-- Sample books (no PDF — for demo purposes)
INSERT IGNORE INTO books (title, author, isbn, version_release, description, genre, publisher, language, page_count, is_public, view_count, created_at, updated_at)
VALUES
('Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '978-0132350884', '1st Edition', 'A book about writing clean, maintainable software code. This book is packed with real examples of messy code.', 'Programming', 'Prentice Hall', 'English', 431, true, 0, NOW(), NOW()),
('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', '978-0135957059', '20th Anniversary', 'The Pragmatic Programmer is one of those rare tech books you will read, re-read, and read again over the years.', 'Programming', 'Addison-Wesley', 'English', 352, true, 0, NOW(), NOW()),
('Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', '978-0201633610', '1st Edition', 'Capturing a wealth of experience about the design of object-oriented software, four top-notch designers present a catalog of simple and succinct solutions.', 'Programming', 'Addison-Wesley', 'English', 395, true, 0, NOW(), NOW()),
('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262046305', '4th Edition', 'The book covers a broad range of algorithms in depth, yet makes their design and analysis accessible to all levels of readers.', 'Computer Science', 'MIT Press', 'English', 1312, true, 0, NOW(), NOW()),
('Dune', 'Frank Herbert', '978-0441013593', 'Original', 'Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, heir to a noble family tasked with ruling an inhospitable world.', 'Science Fiction', 'Ace Books', 'English', 688, true, 0, NOW(), NOW()),
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', '1st Edition', 'A wide-ranging exploration of the ways in which biology and history have defined us and enhanced our understanding of what it means to be human.', 'History', 'Harper', 'English', 443, true, 0, NOW(), NOW());
