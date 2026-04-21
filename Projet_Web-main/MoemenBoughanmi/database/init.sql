-- =============================================================
-- DXC Portal - Database Initialization Script
-- Databases: dxc_form (auth + sla), profiles_db (profiles)
-- =============================================================

-- =============================================================
-- 1. AUTH MICROSERVICE DATABASE (dxc_form)
-- =============================================================

CREATE DATABASE IF NOT EXISTS dxc_form CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dxc_form;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    first_name            VARCHAR(255) NOT NULL,
    last_name             VARCHAR(255) NOT NULL,
    role                  ENUM('EMPLOYEE','RH','ADMIN','MANAGER') NOT NULL DEFAULT 'EMPLOYEE',
    is_verified           TINYINT(1) NOT NULL DEFAULT 0,
    is_active             TINYINT(1) NOT NULL DEFAULT 1,
    is_locked             TINYINT(1) NOT NULL DEFAULT 0,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at         DATETIME,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- OTP codes table
CREATE TABLE IF NOT EXISTS otp_codes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    code       VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    used       TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Password reset table
CREATE TABLE IF NOT EXISTS password_reset (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    user_email     VARCHAR(255) NOT NULL,
    reset_token    VARCHAR(255) NOT NULL UNIQUE,
    reset_selector VARCHAR(255) NOT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at     DATETIME NOT NULL,
    used           TINYINT(1) NOT NULL DEFAULT 0
);

-- Verification tokens table
CREATE TABLE IF NOT EXISTS verification_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    used       TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vtoken_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================================
-- 2. SLA MICROSERVICE DATABASE (dxc_form - shared)
-- =============================================================

-- Employees table
CREATE TABLE IF NOT EXISTS employees (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id          VARCHAR(255) NOT NULL UNIQUE,
    first_name           VARCHAR(255) NOT NULL,
    last_name            VARCHAR(255) NOT NULL,
    email                VARCHAR(255) NOT NULL UNIQUE,
    position             VARCHAR(255) NOT NULL,
    department           VARCHAR(255) NOT NULL,
    hire_date            DATE NOT NULL,
    salary               DOUBLE NOT NULL,
    phone                VARCHAR(255),
    address              VARCHAR(255),
    total_leave_days     INT NOT NULL DEFAULT 30,
    used_leave_days      INT NOT NULL DEFAULT 0,
    remaining_leave_days INT NOT NULL DEFAULT 30,
    user_id              BIGINT,
    is_active            TINYINT(1) NOT NULL DEFAULT 1,
    created_at           DATETIME,
    updated_at           DATETIME
);

-- Requests table
CREATE TABLE IF NOT EXISTS requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number      VARCHAR(255) NOT NULL UNIQUE,
    employee_id         BIGINT NOT NULL,
    type                ENUM(
                          'ATTESTATION_TRAVAIL',
                          'ATTESTATION_SALAIRE',
                          'ATTESTATION_STAGE',
                          'ATTESTATION_EMPLOI',
                          'CONGE_ANNUEL',
                          'CONGE_MALADIE',
                          'CONGE_EXCEPTIONNEL',
                          'ASSURANCE',
                          'ABONNEMENT_TRANSPORT'
                        ) NOT NULL,
    status              ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    motif               VARCHAR(1000) NOT NULL,
    request_date        DATETIME NOT NULL,
    start_date          DATE,
    end_date            DATE,
    number_of_days      INT,
    approved_by         VARCHAR(255),
    approved_by_user_id BIGINT,
    approved_date       DATETIME,
    rejected_by         VARCHAR(255),
    rejected_by_user_id BIGINT,
    rejected_date       DATETIME,
    rejection_reason    VARCHAR(255),
    transport_type      VARCHAR(50),
    pickup_address      VARCHAR(255),
    document_path       VARCHAR(255),
    document_generated  TINYINT(1) DEFAULT 0,
    created_at          DATETIME,
    updated_at          DATETIME,
    CONSTRAINT fk_request_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- SLA configurations table
CREATE TABLE IF NOT EXISTS sla_configurations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name  VARCHAR(255) NOT NULL,
    company_id    VARCHAR(255) NOT NULL UNIQUE,
    frame_time    INT NOT NULL,
    sla_target    DOUBLE NOT NULL,
    formula_type  VARCHAR(255) NOT NULL,
    contact_name  VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(255),
    contact_dept  VARCHAR(255),
    created_at    DATETIME NOT NULL,
    updated_at    DATETIME
);

-- Account configurations table
CREATE TABLE IF NOT EXISTS configurations (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    account             VARCHAR(100) NOT NULL UNIQUE,
    desk_account        VARCHAR(100) NOT NULL,
    time_frame          INT NOT NULL,
    time_frame_ooh      INT,
    time_frame_other    VARCHAR(50),
    answer_sla          VARCHAR(50) NOT NULL,
    abandon_sla         VARCHAR(50) NOT NULL,
    other_sla           VARCHAR(50),
    target_answer_rate  VARCHAR(50) NOT NULL,
    target_abandon_rate VARCHAR(50) NOT NULL,
    target_other        VARCHAR(50),
    created_at          DATETIME,
    updated_at          DATETIME
);

-- Activity logs table
CREATE TABLE IF NOT EXISTS activity_logs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT,
    user_full_name VARCHAR(255),
    action         VARCHAR(255),
    description    VARCHAR(500),
    entity_type    VARCHAR(255),
    entity_id      BIGINT,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================
-- 3. PROFILES MICROSERVICE DATABASE (profiles_db)
-- =============================================================

CREATE DATABASE IF NOT EXISTS profiles_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE profiles_db;

-- Profiles table
CREATE TABLE IF NOT EXISTS profiles (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,
    email               VARCHAR(150) NOT NULL UNIQUE,
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    national_id         VARCHAR(50),
    residential_address VARCHAR(255),
    contact_phone       VARCHAR(20),
    family_status       VARCHAR(100),
    date_of_birth       DATE,
    nationality         VARCHAR(50),
    professional_title  VARCHAR(100),
    current_shift       VARCHAR(100),
    corporate_email     VARCHAR(150),
    department          VARCHAR(100),
    assigned_project    VARCHAR(200),
    hire_date           DATE,
    employee_number     VARCHAR(100),
    technical_skills    VARCHAR(1000),
    role                VARCHAR(50),
    manager_email       VARCHAR(150),
    hr_manager_email    VARCHAR(150),
    profile_picture_url VARCHAR(500),
    is_active           TINYINT(1) NOT NULL DEFAULT 1,
    created_at          DATETIME NOT NULL,
    updated_at          DATETIME
);

-- =============================================================
-- 4. DEFAULT DATA
-- =============================================================

USE dxc_form;

-- Default accounts (password = Admin@123 for all, bcrypt hashed)
INSERT IGNORE INTO users (email, password_hash, first_name, last_name, role, is_verified, is_active, last_login_at) VALUES
('admin@dxc.com',    '$2a$10$DVe086UUQQ7lEvFjXZpc0utJoTedlaSkXCK4efAWCZ9y2jn4UqK1e', 'Admin',   'DXC', 'ADMIN',    1, 1, NOW()),
('rh@dxc.com',       '$2a$10$DVe086UUQQ7lEvFjXZpc0utJoTedlaSkXCK4efAWCZ9y2jn4UqK1e', 'RH',      'DXC', 'RH',       1, 1, NOW()),
('manager@dxc.com',  '$2a$10$DVe086UUQQ7lEvFjXZpc0utJoTedlaSkXCK4efAWCZ9y2jn4UqK1e', 'Manager', 'DXC', 'MANAGER',  1, 1, NOW()),
('employee@dxc.com', '$2a$10$DVe086UUQQ7lEvFjXZpc0utJoTedlaSkXCK4efAWCZ9y2jn4UqK1e', 'Employee','DXC', 'EMPLOYEE', 1, 1, NOW());
