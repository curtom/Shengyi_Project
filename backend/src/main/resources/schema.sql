-- 报销单主表
CREATE TABLE IF NOT EXISTS fk_reim_main (
    id VARCHAR(32) PRIMARY KEY,
    creation_time VARCHAR(32),
    reimbursement_no VARCHAR(32),
    reimbursement_title VARCHAR(500),
    reimburser_id VARCHAR(32),
    reimburser_no VARCHAR(20),
    reimburser_name VARCHAR(20),
    reim_department_id VARCHAR(20),
    reim_department_no VARCHAR(20),
    reim_department_name VARCHAR(100),
    reim_company_id VARCHAR(20),
    reim_company_no VARCHAR(20),
    reim_company_name VARCHAR(100),
    business_type_id VARCHAR(20),
    business_type_no VARCHAR(20),
    business_type_name VARCHAR(100),
    business_trip_reason VARCHAR(500),
    subsidy_total DECIMAL(18,2) DEFAULT 0,
    meal_allowance DECIMAL(18,2) DEFAULT 0,
    transportation_allowance DECIMAL(18,2) DEFAULT 0,
    phone_allowance DECIMAL(18,2) DEFAULT 0,
    remarks VARCHAR(1000),
    doc_status VARCHAR(2) DEFAULT '0',
    doc_date VARCHAR(20)
);

-- 补录行程表
CREATE TABLE IF NOT EXISTS fk_reim_itinerary (
    id VARCHAR(32) PRIMARY KEY,
    main_id VARCHAR(32) NOT NULL,
    traveler_id VARCHAR(20),
    traveler_no VARCHAR(32),
    traveler_name VARCHAR(50),
    departure_date VARCHAR(20),
    arrival_date VARCHAR(20),
    departure_city VARCHAR(50),
    departure_city_no VARCHAR(20),
    arriving_city VARCHAR(50),
    arriving_city_no VARCHAR(20),
    itinerary_instructions VARCHAR(500)
);

-- 补助信息表
CREATE TABLE IF NOT EXISTS fk_reim_subsidy (
    id VARCHAR(32) PRIMARY KEY,
    main_id VARCHAR(32) NOT NULL,
    itinerary_id VARCHAR(32),
    traveler_id VARCHAR(20),
    traveler_no VARCHAR(20),
    traveler_name VARCHAR(50),
    departure_date VARCHAR(20),
    arrival_date VARCHAR(20),
    subsidy_days INT,
    departure_city VARCHAR(50),
    departure_city_no VARCHAR(20),
    arriving_city VARCHAR(50),
    arriving_city_no VARCHAR(20),
    application_amount DECIMAL(18,2) DEFAULT 0,
    subsidy_amount DECIMAL(18,2) DEFAULT 0,
    meal_allowance DECIMAL(18,2) DEFAULT 0,
    transportation_allowance DECIMAL(18,2) DEFAULT 0,
    phone_allowance DECIMAL(18,2) DEFAULT 0,
    business_type_id VARCHAR(20),
    business_type_no VARCHAR(20),
    business_type_name VARCHAR(100)
);

-- 补助日历表
CREATE TABLE IF NOT EXISTS fk_subsidy_calendar (
    id VARCHAR(32) PRIMARY KEY,
    main_id VARCHAR(32) NOT NULL,
    subsidy_id VARCHAR(32) NOT NULL,
    travel_date VARCHAR(20) NOT NULL,
    travel_date_week VARCHAR(10),
    subsidized_cities VARCHAR(50),
    subsidized_city_number VARCHAR(20),
    remark VARCHAR(200),
    standard_meal_expenses_amount DECIMAL(18,2) DEFAULT 0,
    standard_traffic_amount DECIMAL(18,2) DEFAULT 40,
    standard_communication_amount DECIMAL(18,2) DEFAULT 40,
    meal_expenses_amount DECIMAL(18,2) DEFAULT 0,
    traffic_amount DECIMAL(18,2) DEFAULT 0,
    communication_amount DECIMAL(18,2) DEFAULT 0,
    meal_selected VARCHAR(1) DEFAULT '1',
    traffic_selected VARCHAR(1) DEFAULT '1',
    communication_selected VARCHAR(1) DEFAULT '1',
    is_reimbursed VARCHAR(1) DEFAULT '1'
);

-- 费用分摊表
CREATE TABLE IF NOT EXISTS fk_reim_allocation (
    id VARCHAR(32) PRIMARY KEY,
    main_id VARCHAR(32) NOT NULL,
    sort_no INT DEFAULT 0,
    alloc_company_id VARCHAR(20),
    alloc_company_no VARCHAR(20),
    alloc_company_name VARCHAR(100),
    project_id VARCHAR(20),
    project_no VARCHAR(20),
    project_name VARCHAR(100),
    allocation_ratio DECIMAL(8,4) DEFAULT 0,
    allocation_amount DECIMAL(18,2) DEFAULT 0
);

-- 城市等级（餐补标准）
CREATE TABLE IF NOT EXISTS fk_city_tier (
    city_no VARCHAR(20) PRIMARY KEY,
    city_name VARCHAR(50) NOT NULL,
    tier VARCHAR(1) NOT NULL
);
