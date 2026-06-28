-- Create database (if it does not exist)
CREATE DATABASE IF NOT EXISTS access_token_refresh_token_db;
USE access_token_refresh_token_db;

-- 1. Create Roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- e.g., 'ROLE_USER', 'ROLE_ADMIN'
    description VARCHAR(255)
);

-- 2. Create Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    -- Automatically retrieves the current time when creating a new file
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Automatically update the time whenever an UPDATE command is received
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Create Users_Roles junction table (Many-to-Many relationship)
CREATE TABLE users_roles (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,
     role_id BIGINT NOT NULL,
     granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     granted_by VARCHAR(255),
     -- Unique constraint to prevent duplicate role assignments for the same user
     UNIQUE KEY uk_user_role (user_id, role_id),
     -- Foreign key referencing the users table
     CONSTRAINT fk_user FOREIGN KEY (user_id)
         REFERENCES users(id) ON DELETE CASCADE,
     -- Foreign key referencing the roles table
     CONSTRAINT fk_role FOREIGN KEY (role_id)
         REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Create Refresh Tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE
);

---------------------------------------------------------
-- MOCK DATA INSERTION
---------------------------------------------------------

-- Insert data for Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Has full access to all resources'),
('ROLE_USER', 'Standard user with limited access'),
('ROLE_INSTRUCTOR', 'Can manage course materials');

-- Insert data for Users
-- Note: Using {noop} prefix for plain text passwords (Spring Security demo purposes)
INSERT INTO users (username, password, full_name, email, phone, address, enabled) VALUES
('admin', '$2a$10$es6vQY9Z9E4oUBFDl8sUKeXWo60NaLuFXfh/m2uNLD09.4n3SOPdC', 'System Administrator', 'admin@skillspark.vn', '0901234567', 'Ho Chi Minh City', 1),
('daonbm', '$2a$10$es6vQY9Z9E4oUBFDl8sUKeXWo60NaLuFXfh/m2uNLD09.4n3SOPdC', 'Nguyen Ba Minh Dao', 'daonbm@example.com', '0911223344', 'Dong Nai', 1),
('student1', '$2a$10$es6vQY9Z9E4oUBFDl8sUKeXWo60NaLuFXfh/m2uNLD09.4n3SOPdC', 'Nguyen Van A', 'student1@example.com', '0988776655', 'Hanoi', 1);

-- Map Users to Roles (Junction table data)
INSERT INTO users_roles (user_id, role_id, granted_by) VALUES
(1, 1, 'SYSTEM'), -- admin assigned ROLE_ADMIN
(1, 2, 'SYSTEM'), -- admin assigned ROLE_USER
(2, 3, 'admin'),  -- hoangnv assigned ROLE_INSTRUCTOR
(3, 2, 'admin');  -- student1 assigned ROLE_USER