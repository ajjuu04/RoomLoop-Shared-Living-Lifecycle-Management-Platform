INSERT INTO `user` (mobile_number, name, email, password) VALUES
('9876543210', 'Ajinkya Jadhav', 'ajinkya@gmail.com', 'password123'),
('9876543211', 'Rahul Patil', 'rahul@gmail.com', 'password123'),
('9876543212', 'Amit Sharma', 'amit@gmail.com', 'password123'),
('9876543213', 'Rohit Kulkarni', 'rohit@gmail.com', 'password123'),
('9876543214', 'Sanket Deshmukh', 'sanket@gmail.com', 'password123');


INSERT INTO room
(address, rent, deposit, total_occupancy, creted_by_id) VALUES
('Baner, Pune', 18000.00, 36000.00, 3, 1),
('Wakad, Pune', 15000.00, 30000.00, 2, 2),
('Kothrud, Pune', 22000.00, 44000.00, 4, 3),
('Viman Nagar, Pune', 20000.00, 40000.00, 3, 4),
('Hinjewadi, Pune', 16000.00, 32000.00, 2, 5);

INSERT INTO listing
(room_id, posted_by_id, open_spots, preferences, listing_status) VALUES
(1, 2, 2, 'Non-smoker, working professional', 'OPEN'),
(2, 3, 1, 'Vegetarian preferred, clean and quiet', 'OPEN'),
(3, 1, 3, 'Students or working professionals', 'OPEN'),
(4, 4, 2, 'No pets, preferably non-smoker', 'OPEN'),
(5, 5, 1, 'Working professional, flexible timings', 'OPEN');

INSERT INTO membership
(user_id, room_id, is_admin, membership_status) VALUES
(1, 1, TRUE, 'ACTIVE'),
(2, 1, FALSE, 'ACTIVE'),
(3, 2, TRUE, 'ACTIVE'),
(4, 4, TRUE, 'ACTIVE'),
(5, 5, TRUE, 'ACTIVE');

INSERT INTO interested
(user_id, listing_id, created_at) VALUES
(1, 1, CURRENT_TIMESTAMP),
(3, 1, CURRENT_TIMESTAMP),
(4, 2, CURRENT_TIMESTAMP),
(2, 3, CURRENT_TIMESTAMP),
(5, 4, CURRENT_TIMESTAMP);