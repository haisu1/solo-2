INSERT INTO sys_role (role_code, role_name, description) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限'),
('ADMIN_STAFF', '行政人员', '负责审核领用申请、发放物资'),
('DEPT_MANAGER', '部门主管', '部门负责人，负责审批本部门申请'),
('EMPLOYEE', '普通员工', '可提交领用申请');

INSERT INTO sys_department (dept_name, dept_code, parent_id, leader, phone, description) VALUES
('总公司', 'HQ', 0, '张总', '13800000001', '公司总部'),
('行政部', 'ADMIN', 1, '李经理', '13800000002', '行政管理部门'),
('技术部', 'TECH', 1, '王经理', '13800000003', '技术研发部门'),
('财务部', 'FINANCE', 1, '赵经理', '13800000004', '财务管理部门'),
('市场部', 'MARKETING', 1, '钱经理', '13800000005', '市场营销部门');

INSERT INTO sys_user (username, password, real_name, phone, email, department_id, role_id, status) VALUES
('admin', '123456', '系统管理员', '13800000001', 'admin@example.com', 2, 1, 1),
('adminstaff', '123456', '行政人员', '13800000002', 'adminstaff@example.com', 2, 2, 1),
('techmgr', '123456', '王经理', '13800000003', 'techmgr@example.com', 3, 3, 1),
('financemgr', '123456', '赵经理', '13800000004', 'financemgr@example.com', 4, 3, 1),
('employee1', '123456', '张三', '13800000010', 'zhangsan@example.com', 3, 4, 1),
('employee2', '123456', '李四', '13800000011', 'lisi@example.com', 3, 4, 1),
('employee3', '123456', '王五', '13800000012', 'wangwu@example.com', 4, 4, 1),
('employee4', '123456', '赵六', '13800000013', 'zhaoliu@example.com', 5, 4, 1);

INSERT INTO biz_category (category_name, category_code, parent_id, description, sort_order) VALUES
('办公文具', 'STATIONERY', 0, '日常办公文具用品', 1),
('办公设备', 'EQUIPMENT', 0, '办公电子设备', 2),
('生活用品', 'DAILY', 0, '办公生活日用品', 3),
('书写工具', 'WRITING', 1, '各类笔、笔记本等', 1),
('文件管理', 'FILE', 1, '文件夹、档案盒等', 2),
('电脑外设', 'PERIPHERAL', 2, '键盘、鼠标、U盘等', 1),
('清洁用品', 'CLEANING', 3, '办公清洁用品', 1);

INSERT INTO biz_supply (supply_name, supply_code, category_id, unit, specification, price, stock, min_stock, max_stock, description, status) VALUES
('签字笔（黑色）', 'WZ001', 4, '支', '0.5mm 黑色', 2.50, 200, 50, 500, '黑色签字笔，书写流畅', 1),
('签字笔（蓝色）', 'WZ002', 4, '支', '0.5mm 蓝色', 2.50, 150, 50, 500, '蓝色签字笔，书写流畅', 1),
('A4打印纸', 'WZ003', 5, '包', '70g 500张/包', 25.00, 80, 20, 200, '优质A4打印纸', 1),
('文件夹', 'WZ004', 5, '个', 'A4 二孔夹', 5.00, 100, 30, 300, '塑料文件夹', 1),
('笔记本', 'WZ005', 4, '本', 'A5 100页', 12.00, 60, 20, 200, '硬壳笔记本', 1),
('无线鼠标', 'SB001', 6, '个', '2.4G无线', 45.00, 30, 10, 100, '人体工学无线鼠标', 1),
('有线键盘', 'SB002', 6, '个', 'USB接口', 65.00, 25, 10, 80, '标准有线键盘', 1),
('U盘 32G', 'SB003', 6, '个', 'USB3.0', 35.00, 15, 5, 50, '高速U盘', 1),
('抽纸', 'RY001', 7, '包', '3层 100抽', 5.00, 200, 50, 500, '办公抽纸', 1),
('洗手液', 'RY002', 7, '瓶', '500ml', 15.00, 40, 10, 100, '办公洗手液', 1),
('订书机', 'WZ006', 5, '个', '标准型', 18.00, 5, 5, 50, '办公用品订书机', 1);

INSERT INTO biz_approval_flow (flow_name, biz_type, department_id, category_id, min_amount, max_amount, priority, status, node_config) VALUES
('申领-小额审批流程', 'REQUISITION', NULL, NULL, 0, 499.99, 5, 1, NULL),
('申领-大额审批流程', 'REQUISITION', NULL, NULL, 500, 99999999.99, 10, 1, NULL),
('采购审批流程', 'PURCHASE', NULL, NULL, 0, 99999999.99, 1, 1, NULL);

INSERT INTO biz_approval_node (flow_id, node_level, node_name, approver_type, approver_ids, role_code, timeout_hours, can_transfer, can_withdraw) VALUES
(1, 1, '部门主管审批', 'DEPARTMENT_LEADER', NULL, NULL, 24, 1, 1),
(2, 1, '部门主管审批', 'DEPARTMENT_LEADER', NULL, NULL, 24, 1, 1),
(2, 2, '行政经理审批', 'ROLE', NULL, 'ADMIN_STAFF', 24, 1, 1),
(2, 3, '财务审批', 'USER', '1,4', NULL, 24, 1, 0),
(3, 1, '部门主管审批', 'DEPARTMENT_LEADER', NULL, NULL, 24, 1, 1),
(3, 2, '财务审批', 'USER', '1,4', NULL, 48, 1, 1),
(3, 3, '总经理审批', 'ROLE', NULL, 'ADMIN', 48, 0, 0);
