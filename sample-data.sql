-- ============================================
-- SAMPLE DATA FOR ALL MICROSERVICES DATABASES
-- ============================================

-- ============================================
-- 1. USER_DB (User Service)
-- ============================================
USE user_db;

-- Users (passwords: admin123, manager123, employee123)
INSERT INTO users (employee_id, email, first_name, last_name, password, phone_number, department_id, designation_id, manager_id, role, active, date_of_joining) VALUES
('ADMIN001', 'admin@revature.com', 'Admin', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1234567890', 1, 1, NULL, 'ADMIN', 1, '2024-01-01'),
('MGR001', 'manager@revature.com', 'John', 'Manager', '$2a$10$8K1p/H0MIWe69vN6YX2wO.D8p8W5/qjU5xvWkCYmQqg5xvWkCYmQq', '9876543210', 1, 2, 1, 'MANAGER', 1, '2024-01-15'),
('EMP001', 'employee@revature.com', 'Jane', 'Employee', '$2a$10$dXJ3SW6G7P4YBjCv6tEsF.QxvAbohEXJh7xK5cyuf1YizwSqb4eri', '9988776655', 1, 3, 2, 'EMPLOYEE', 1, '2024-02-01'),
('EMP002', 'bob.smith@revature.com', 'Bob', 'Smith', '$2a$10$dXJ3SW6G7P4YBjCv6tEsF.QxvAbohEXJh7xK5cyuf1YizwSqb4eri', '8877665544', 2, 4, 2, 'EMPLOYEE', 1, '2024-02-15');

-- ============================================
-- 2. EMPLOYEE_MGMT_DB (Employee Management Service)
-- ============================================
USE employee_mgmt_db;

-- Departments
INSERT INTO departments (name, description) VALUES
('Engineering', 'Software Development and Engineering'),
('Human Resources', 'HR and Recruitment'),
('Finance', 'Finance and Accounting'),
('Marketing', 'Marketing and Sales');

-- Designations
INSERT INTO designations (title, description) VALUES
('Administrator', 'System Administrator'),
('Engineering Manager', 'Manages Engineering Team'),
('Software Engineer', 'Develops Software'),
('HR Specialist', 'Handles HR Operations'),
('Accountant', 'Manages Finances');

-- Announcements
INSERT INTO announcements (title, content, created_by, created_at) VALUES
('Welcome to RevWorkforce', 'Welcome to our new workforce management system!', 1, NOW()),
('Holiday Notice', 'Office will be closed on March 15th for company event.', 1, NOW()),
('New Policy Update', 'Please review the updated leave policy in the employee handbook.', 2, NOW());

-- ============================================
-- 3. LEAVE_DB (Leave Service)
-- ============================================
USE leave_db;

-- Leave Types
INSERT INTO leave_types (name, description, max_days_per_year) VALUES
('Sick Leave', 'Medical leave for illness', 10),
('Casual Leave', 'Personal casual leave', 12),
('Vacation Leave', 'Annual vacation leave', 15),
('Maternity Leave', 'Maternity leave for mothers', 90),
('Paternity Leave', 'Paternity leave for fathers', 7);

-- Leave Balances
INSERT INTO leave_balances (user_id, leave_type_id, total_days, used_days, remaining_days) VALUES
(1, 1, 10, 0, 10),
(1, 2, 12, 0, 12),
(1, 3, 15, 0, 15),
(2, 1, 10, 2, 8),
(2, 2, 12, 3, 9),
(2, 3, 15, 5, 10),
(3, 1, 10, 1, 9),
(3, 2, 12, 2, 10),
(3, 3, 15, 0, 15),
(4, 1, 10, 0, 10),
(4, 2, 12, 1, 11),
(4, 3, 15, 3, 12);

-- Leave Applications
INSERT INTO leave_applications (user_id, leave_type_id, start_date, end_date, reason, status, applied_date) VALUES
(3, 1, '2024-03-20', '2024-03-22', 'Medical checkup', 'APPROVED', '2024-03-10'),
(3, 2, '2024-04-10', '2024-04-12', 'Personal work', 'PENDING', '2024-03-15'),
(4, 3, '2024-05-01', '2024-05-10', 'Family vacation', 'PENDING', '2024-03-18');

-- ============================================
-- 4. NOTIFICATION_DB (Notification Service)
-- ============================================
USE notification_db;

-- Notifications
INSERT INTO notifications (user_id, title, message, type, is_read, created_at) VALUES
(1, 'Welcome Admin', 'Welcome to RevWorkforce Management System', 'INFO', 0, NOW()),
(2, 'Leave Approved', 'Your leave application has been approved', 'SUCCESS', 0, NOW()),
(3, 'Leave Pending', 'Your leave application is pending approval', 'WARNING', 0, NOW()),
(3, 'Performance Review', 'Your quarterly performance review is scheduled', 'INFO', 0, NOW()),
(4, 'New Announcement', 'Check the new company announcement', 'INFO', 0, NOW());

-- ============================================
-- 5. PERFORMANCE_DB (Performance Service)
-- ============================================
USE performance_db;

-- Performance Reviews
INSERT INTO performance_reviews (employee_id, reviewer_id, review_period, rating, comments, review_date, status) VALUES
(3, 2, 'Q1 2024', 4.5, 'Excellent performance, meets all expectations', '2024-03-01', 'COMPLETED'),
(4, 2, 'Q1 2024', 4.0, 'Good performance, room for improvement in communication', '2024-03-05', 'COMPLETED'),
(3, 2, 'Q2 2024', 0.0, 'Review in progress', '2024-06-01', 'PENDING');

-- Goals
INSERT INTO goals (user_id, title, description, deadline, status, priority) VALUES
(3, 'Complete Spring Boot Certification', 'Obtain Spring Professional certification', '2024-06-30', 'IN_PROGRESS', 'HIGH'),
(3, 'Lead Project Migration', 'Lead the microservices migration project', '2024-12-31', 'IN_PROGRESS', 'MEDIUM'),
(4, 'Improve Code Quality', 'Reduce code review comments by 50%', '2024-09-30', 'NOT_STARTED', 'MEDIUM'),
(2, 'Team Building', 'Organize quarterly team building activities', '2024-12-31', 'IN_PROGRESS', 'LOW');

-- ============================================
-- 6. REPORTING_DB (Reporting Service)
-- ============================================
USE reporting_db;

-- Activity Logs
INSERT INTO activity_logs (user_id, action, description, timestamp) VALUES
(1, 'LOGIN', 'Admin logged into the system', NOW()),
(1, 'CREATE_USER', 'Created new user: manager@revature.com', NOW()),
(2, 'LOGIN', 'Manager logged into the system', NOW()),
(2, 'APPROVE_LEAVE', 'Approved leave for employee Jane Employee', NOW()),
(3, 'LOGIN', 'Employee logged into the system', NOW()),
(3, 'APPLY_LEAVE', 'Applied for sick leave from 2024-03-20 to 2024-03-22', NOW()),
(3, 'UPDATE_PROFILE', 'Updated phone number', NOW()),
(4, 'LOGIN', 'Employee logged into the system', NOW()),
(4, 'APPLY_LEAVE', 'Applied for vacation leave', NOW());

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify data insertion
SELECT 'user_db' as database_name, COUNT(*) as user_count FROM user_db.users
UNION ALL
SELECT 'employee_mgmt_db', COUNT(*) FROM employee_mgmt_db.departments
UNION ALL
SELECT 'leave_db', COUNT(*) FROM leave_db.leave_types
UNION ALL
SELECT 'notification_db', COUNT(*) FROM notification_db.notifications
UNION ALL
SELECT 'performance_db', COUNT(*) FROM performance_db.performance_reviews
UNION ALL
SELECT 'reporting_db', COUNT(*) FROM reporting_db.activity_logs;
