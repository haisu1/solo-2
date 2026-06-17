DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_department;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS biz_category;
DROP TABLE IF EXISTS biz_supply;
DROP TABLE IF EXISTS biz_requisition;
DROP TABLE IF EXISTS biz_requisition_item;
DROP TABLE IF EXISTS biz_purchase;
DROP TABLE IF EXISTS biz_purchase_item;
DROP TABLE IF EXISTS biz_inventory_check;
DROP TABLE IF EXISTS biz_inventory_check_item;
DROP TABLE IF EXISTS biz_stock_log;

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL,
    dept_code VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    leader VARCHAR(50),
    phone VARCHAR(20),
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    department_id BIGINT,
    role_id BIGINT,
    status INT DEFAULT 1,
    avatar VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    description VARCHAR(500),
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_supply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supply_name VARCHAR(200) NOT NULL,
    supply_code VARCHAR(50) NOT NULL UNIQUE,
    category_id BIGINT,
    unit VARCHAR(20),
    specification VARCHAR(200),
    price DECIMAL(10,2) DEFAULT 0,
    stock INT DEFAULT 0,
    min_stock INT DEFAULT 0,
    max_stock INT DEFAULT 0,
    image_url VARCHAR(500),
    description VARCHAR(500),
    status INT DEFAULT 1,
    version INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_requisition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requisition_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    department_id BIGINT,
    purpose VARCHAR(500),
    remark VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by BIGINT,
    approve_time TIMESTAMP,
    approve_remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_requisition_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requisition_id BIGINT NOT NULL,
    supply_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) DEFAULT 0,
    total_price DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE biz_purchase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT,
    supplier_name VARCHAR(200),
    total_amount DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_by BIGINT,
    approved_by BIGINT,
    approve_time TIMESTAMP,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_purchase_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_id BIGINT NOT NULL,
    supply_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) DEFAULT 0,
    total_price DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE biz_inventory_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_no VARCHAR(50) NOT NULL UNIQUE,
    check_type VARCHAR(20) DEFAULT 'FULL',
    status VARCHAR(20) DEFAULT 'DRAFT',
    checked_by BIGINT,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_inventory_check_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_id BIGINT NOT NULL,
    supply_id BIGINT NOT NULL,
    system_stock INT DEFAULT 0,
    actual_stock INT DEFAULT 0,
    diff_quantity INT DEFAULT 0,
    diff_reason VARCHAR(500)
);

CREATE TABLE biz_stock_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supply_id BIGINT NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    before_stock INT DEFAULT 0,
    after_stock INT DEFAULT 0,
    related_no VARCHAR(50),
    operator_id BIGINT,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
