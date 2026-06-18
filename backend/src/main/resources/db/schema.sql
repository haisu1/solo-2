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
DROP TABLE IF EXISTS biz_approval_record;
DROP TABLE IF EXISTS biz_approval_node;
DROP TABLE IF EXISTS biz_approval_flow;
DROP TABLE IF EXISTS biz_stock_log;
DROP TABLE IF EXISTS biz_warning_message;
DROP TABLE IF EXISTS biz_sequence;

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

CREATE TABLE biz_approval_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_name VARCHAR(200) NOT NULL,
    biz_type VARCHAR(50) NOT NULL,
    department_id BIGINT,
    category_id BIGINT,
    min_amount DECIMAL(12,2) DEFAULT 0,
    max_amount DECIMAL(12,2) DEFAULT 99999999.99,
    priority INT DEFAULT 0,
    status INT DEFAULT 1,
    node_config TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_approval_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_id BIGINT NOT NULL,
    node_level INT NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    approver_type VARCHAR(20) NOT NULL,
    approver_ids VARCHAR(500),
    role_code VARCHAR(50),
    timeout_hours INT DEFAULT 24,
    can_transfer INT DEFAULT 1,
    can_withdraw INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(50) NOT NULL,
    biz_id BIGINT NOT NULL,
    biz_no VARCHAR(50),
    flow_id BIGINT,
    current_level INT DEFAULT 1,
    total_levels INT DEFAULT 1,
    node_name VARCHAR(100),
    approver_id BIGINT,
    approver_name VARCHAR(50),
    action VARCHAR(20),
    remark VARCHAR(500),
    start_time TIMESTAMP,
    approve_time TIMESTAMP,
    transfer_from_id BIGINT,
    transfer_from_name VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_warning_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supply_id BIGINT NOT NULL,
    supply_name VARCHAR(200),
    supply_code VARCHAR(50),
    warning_level VARCHAR(20) NOT NULL,
    warning_content VARCHAR(1000),
    current_stock INT DEFAULT 0,
    safe_stock_quantity INT DEFAULT 0,
    stock_days INT DEFAULT 0,
    user_id BIGINT,
    read_flag INT DEFAULT 0,
    read_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_sequence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seq_name VARCHAR(50) NOT NULL UNIQUE,
    seq_date VARCHAR(8) NOT NULL,
    current_value INT DEFAULT 1,
    max_value INT DEFAULT 9999,
    version INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
