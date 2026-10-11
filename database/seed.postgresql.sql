INSERT INTO categories (id, name, slug, description, icon_name) VALUES
(1, 'Books & Textbooks', 'books', 'Engineering textbooks, semester reference books, guides and question banks', 'book'),
(2, 'Calculators', 'calculators', 'Scientific calculators, programmable calculators approved for exams', 'calculator'),
(3, 'Electronics', 'electronics', 'Multimeters, soldering kits, Arduino/Raspberry Pi boards and accessories', 'cpu'),
(4, 'Lab Equipment', 'lab-equipment', 'Vernier calipers, micrometers, breadboards, optical components', 'flask'),
(5, 'Bicycles', 'bicycles', 'Campus commuter cycles, geared and single-speed bikes', 'bike'),
(6, 'Lab Coats', 'lab-coats', 'College standard white and navy blue aprons/lab coats', 'shirt'),
(7, 'Stationery & Drawing Tools', 'stationery', 'Mini-drafters, roller scales, engineering graphics toolsets, rounders', 'pen-tool')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users
(id, student_id, full_name, email, password_hash, phone, department, year_of_study, role, verification_status, rejection_reason, api_token)
VALUES
(1, 'ADMIN-001', 'Prof. Sharma (Admin)', 'admin@campus.edu',
 '$2y$10$sXHC4wcCt5FQI6EVIrwNfOOJsXoknJcu/KsyG55epWXNoeV47Z7q2',
 '+91 98765 43210', 'Campus Administration', 0, 'admin', 'approved', NULL, NULL),
(2, '2023CS0104', 'Atharva Gokhale', 'atharva@campus.edu',
 '$2y$10$h5kM5gMI90s92nkKlkbcjuAXDbIejhJux2BqvoQ89mqzeDYq5IrSm',
 '+91 91234 56789', 'Computer Engineering', 3, 'student', 'approved', NULL, NULL),
(3, '2023ME0215', 'Janhavi Patil', 'janhavi@campus.edu',
 '$2y$10$h5kM5gMI90s92nkKlkbcjuAXDbIejhJux2BqvoQ89mqzeDYq5IrSm',
 '+91 92345 67890', 'Mechanical Engineering', 2, 'student', 'approved', NULL, NULL),
(4, '2024EC0318', 'Yash Deshmukh', 'yash@campus.edu',
 '$2y$10$h5kM5gMI90s92nkKlkbcjuAXDbIejhJux2BqvoQ89mqzeDYq5IrSm',
 '+91 93456 78901', 'Electronics & Comm.', 1, 'student', 'approved', NULL, NULL),
(5, '2025CS0499', 'Rohit Kulkarni', 'rohit@campus.edu',
 '$2y$10$h5kM5gMI90s92nkKlkbcjuAXDbIejhJux2BqvoQ89mqzeDYq5IrSm',
 '+91 94567 89012', 'Computer Science', 1, 'student', 'pending', NULL, NULL),
(6, '2022CV0042', 'Ananya Roy', 'ananya@campus.edu',
 '$2y$10$h5kM5gMI90s92nkKlkbcjuAXDbIejhJux2BqvoQ89mqzeDYq5IrSm',
 '+91 95678 90123', 'Civil Engineering', 4, 'student', 'rejected',
 'Invalid student ID card photo uploaded. Please re-apply with official photo ID.', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO listings
(id, seller_id, category_id, title, description, price, condition_state, image_url, status, views_count, contact_preference)
VALUES
(1, 2, 2, 'Casio fx-991EX Scientific Calculator',
 'Authentic Casio ClassWiz fx-991EX scientific calculator. Includes natural textbook display, 552 functions, dual power solar and battery. Gently used for 2 semesters in Engineering Mathematics and Physics. Working perfectly with original cover.',
 600.00, 'Good', 'assets/items/casio_calc.png', 'available', 48, 'chat'),
(2, 3, 6, 'Navy Blue Workshop Lab Coat (Size 38)',
 'Standard heavy cotton navy blue workshop lab coat. Washed and sanitized, no tears, 3 pockets and full buttons intact.',
 160.00, 'Good', 'assets/items/lab_coat.png', 'available', 27, 'chat'),
(3, 4, 7, 'Roller Scale - 15 CM for Engineering Drawing',
 '15 cm parallel rolling ruler for rapid hatching, parallel lines and isometric drawings. Smooth brass rollers and clear metric graduations.',
 19.00, 'Acceptable', 'assets/items/roller_scale_15.png', 'available', 15, 'chat'),
(4, 4, 7, 'Tools for Engineering Graphics (Complete Set)',
 'Comprehensive drafting kit: set squares, protractor, French curves, erasing shield, compass divider, and stencil.',
 235.00, 'New', 'assets/items/eg_kit.png', 'available', 62, 'chat'),
(5, 4, 7, 'Maped Mechanical Rounder for Engineering Graphics',
 'High precision Maped bow compass/rounder with universal pencil attachment and micro-adjustment screw.',
 70.00, 'Good', 'assets/items/rounder.png', 'available', 19, 'chat'),
(6, 4, 7, 'Roller Scale - 15 CM (Omega Brand)',
 'Clear acrylic roller ruler with rubberized roller rings. Good condition, used for only one assignment session.',
 30.00, 'Like New', 'assets/items/roller_scale_box.png', 'available', 11, 'chat'),
(7, 2, 1, 'Higher Engineering Mathematics - B.S. Grewal (44th Ed)',
 'Complete reference textbook for Sem 1 to 4 mathematics. Clean pages, no missing sheets, formulas highlighted in pencil.',
 420.00, 'Good', 'assets/items/bs_grewal.png', 'available', 55, 'chat'),
(8, 3, 5, 'Hero Sprint 26T Single Speed Campus Bicycle',
 'Sturdy campus cycle with front basket, bell, and chain lock included. Tires in good condition, recently serviced.',
 2500.00, 'Good', 'assets/items/campus_bike.png', 'available', 89, 'chat'),
(9, 2, 4, 'Digital Multimeter with Probe Needles',
 'Pocket digital multimeter measuring voltage, current, and resistance. Continuity buzzer working. Extra battery included.',
 350.00, 'Like New', 'assets/items/multimeter.png', 'available', 34, 'chat'),
(10, 3, 1, 'Engineering Mechanics: Statics & Dynamics - Beer & Johnston',
 'Recommended textbook for 1st year mechanics. Free solved assignment sheets included inside the book.',
 380.00, 'Good', 'assets/items/beer_johnston.png', 'sold', 72, 'chat')
ON CONFLICT (id) DO NOTHING;

INSERT INTO wishlist (id, user_id, listing_id) VALUES
(1, 2, 8), (2, 3, 1), (3, 4, 1), (4, 4, 7)
ON CONFLICT (id) DO NOTHING;

INSERT INTO reports (id, reporter_id, listing_id, reason, details, status, admin_notes) VALUES
(1, 3, 6, 'Duplicate Listing', 'The seller appears to have posted the same roller scale twice under different prices.', 'pending', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO admin_audit_logs (id, admin_id, action, target_type, target_id, details, ip_address) VALUES
(1, 1, 'APPROVE_STUDENT', 'user', 2, 'Verified student registration for Atharva Gokhale (2023CS0104)', '127.0.0.1'),
(2, 1, 'APPROVE_STUDENT', 'user', 3, 'Verified student registration for Janhavi Patil (2023ME0215)', '127.0.0.1'),
(3, 1, 'APPROVE_STUDENT', 'user', 4, 'Verified student registration for Yash Deshmukh (2024EC0318)', '127.0.0.1'),
(4, 1, 'REJECT_STUDENT', 'user', 6, 'Rejected verification for Ananya Roy due to invalid ID card image', '127.0.0.1')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE((SELECT MAX(id) FROM categories), 1));
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('listings', 'id'), COALESCE((SELECT MAX(id) FROM listings), 1));
SELECT setval(pg_get_serial_sequence('wishlist', 'id'), COALESCE((SELECT MAX(id) FROM wishlist), 1));
SELECT setval(pg_get_serial_sequence('reports', 'id'), COALESCE((SELECT MAX(id) FROM reports), 1));
SELECT setval(pg_get_serial_sequence('admin_audit_logs', 'id'), COALESCE((SELECT MAX(id) FROM admin_audit_logs), 1));
