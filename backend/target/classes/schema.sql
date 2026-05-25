CREATE TABLE IF NOT EXISTS fk_reim_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reimb_no VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(200),
    applicant VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    expense_company VARCHAR(100),
    business_type VARCHAR(50),
    reason TEXT,
    total_amount DECIMAL(15,2) DEFAULT 0,
    subsidy_total DECIMAL(15,2) DEFAULT 0,
    meal_allowance DECIMAL(15,2) DEFAULT 0,
    transport_allowance DECIMAL(15,2) DEFAULT 0,
    communication_allowance DECIMAL(15,2) DEFAULT 0,
    status INT DEFAULT 0,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fk_reim_itinerary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    main_id BIGINT NOT NULL,
    traveler VARCHAR(50),
    start_date DATE,
    end_date DATE,
    travel_date VARCHAR(50),
    departure_city VARCHAR(100),
    destination_city VARCHAR(100),
    route VARCHAR(500),
    city_level INT DEFAULT 1,
    transport_type VARCHAR(50),
    remark VARCHAR(500),
    description VARCHAR(500),
    CONSTRAINT fk_itinerary_main FOREIGN KEY (main_id) REFERENCES fk_reim_main(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fk_reim_subsidy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    main_id BIGINT NOT NULL,
    subsidy_date DATE NOT NULL,
    city_level INT DEFAULT 1,
    standard_amount DECIMAL(10,2),
    actual_amount DECIMAL(10,2),
    subsidy_type VARCHAR(50),
    CONSTRAINT fk_subsidy_main FOREIGN KEY (main_id) REFERENCES fk_reim_main(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fk_reim_allocation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    main_id BIGINT NOT NULL,
    expense_type VARCHAR(50),
    expense_type_name VARCHAR(100),
    project_code VARCHAR(50),
    project_name VARCHAR(200),
    allocation_ratio DECIMAL(5,2),
    allocation_amount DECIMAL(15,2),
    CONSTRAINT fk_allocation_main FOREIGN KEY (main_id) REFERENCES fk_reim_main(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fk_subsidy_calendar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_level INT DEFAULT 1,
    effective_date DATE,
    subsidy_amount DECIMAL(10,2),
    remark VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS fk_acceptance_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    legal_comp_id VARCHAR(50),
    legal_comp_code VARCHAR(50),
    legal_comp_name VARCHAR(100),
    ticket_no VARCHAR(100),
    issue_date TIMESTAMP,
    expiration_date TIMESTAMP,
    received_date TIMESTAMP,
    draft_type VARCHAR(20),
    draft_type_name VARCHAR(50),
    accepting_bank VARCHAR(100),
    billing_method VARCHAR(20),
    billing_method_name VARCHAR(50),
    accept_comp_id VARCHAR(50),
    accept_comp_code VARCHAR(50),
    accept_comp_name VARCHAR(100),
    ticket_status VARCHAR(20),
    ticket_status_name VARCHAR(50),
    expiration_status VARCHAR(20),
    face_amount DECIMAL(18,2),
    balance DECIMAL(18,2),
    under_acceptance_amount DECIMAL(18,2),
    accepted_amount DECIMAL(18,2),
    create_user_code VARCHAR(50),
    create_user_name VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_user_code VARCHAR(50),
    update_user_name VARCHAR(50),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
