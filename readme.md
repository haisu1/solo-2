# 企业办公用品申领系统

## 项目简介

企业办公用品申领系统面向企业管理员、部门员工、行政人员，提供物资分类、库存管理、物品申领、领用审批、物资采购、库存盘点等管理能力。系统支持管理员维护物资、部门、人员基础数据，员工在线提交领用申请，行政人员审核申请、发放物资，实现企业物资规范化管控，降低资源损耗。

## 技术栈

- **后端**: SpringBoot 2.7 + MyBatis-Plus + H2 Database
- **前端**: Vue 2 + Element UI + Vue Router + Vuex + Axios
- **认证**: JWT Token

## 目录结构

```
office-supplies-system/
├── backend/                 # SpringBoot 后端服务
│   ├── src/main/java/       # Java 源码
│   │   └── com/office/supplies
│   │       ├── common/      # 通用类（响应、工具、上下文）
│   │       ├── config/      # 配置类
│   │       ├── controller/  # REST 接口层
│   │       ├── entity/      # 实体类
│   │       ├── mapper/      # 数据访问层
│   │       └── service/     # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   └── db/              # 数据库 SQL 脚本
│   └── pom.xml              # Maven 配置
├── frontend/                # Vue 前端项目
│   ├── src/
│   │   ├── api/             # 接口封装
│   │   ├── assets/          # 静态资源
│   │   ├── router/          # 路由配置
│   │   ├── store/           # Vuex 状态管理
│   │   ├── utils/           # 工具类
│   │   └── views/           # 页面组件
│   ├── public/              # 公共资源
│   ├── package.json         # 前端依赖
│   └── vue.config.js        # Vue 配置
└── readme.md
```

## 角色与权限

| 角色 | 角色编码 | 权限 |
|------|----------|------|
| 系统管理员 | ADMIN | 所有权限 |
| 行政人员 | ADMIN_STAFF | 物资管理、申领审批、库存盘点、数据统计 |
| 普通员工 | EMPLOYEE | 查看物资、提交申领、查看自己的申领 |

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| adminstaff | 123456 | 行政人员 |
| employee1 | 123456 | 普通员工 |
| employee2 | 123456 | 普通员工 |
| employee3 | 123456 | 普通员工 |
| employee4 | 123456 | 普通员工 |

## 核心功能

### 1. 基础数据管理
- 用户、角色、部门管理
- 物资分类、物资信息维护
- 库存上下限设置

### 2. 物资申领
- 员工浏览物资目录
- 提交多物资申领单
- 申领单状态跟踪（待审批/已通过/已驳回/已取消）

### 3. 审批管理
- 行政人员/管理员审批申领单
- 审批通过自动扣减库存
- 记录库存变动日志

### 4. 采购管理
- 发起低库存物资采购
- 采购单审批
- 采购入库自动增加库存

### 5. 库存管理
- 库存预警（低库存高亮显示）
- 定期全盘盘点
- 库存流水查询
- 手动入库操作

### 6. 数据统计
- 首页数据看板
- 申领状态统计
- 库存总览

## 启动说明

### 后端启动
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
后端服务启动在 `http://localhost:8080`

H2 控制台访问: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:officesupplies`
- 用户名: `sa`
- 密码: 空

### 前端启动
```bash
cd frontend
npm install
npm run serve
```
前端服务启动在 `http://localhost:8081`

## 核心流程

1. 管理员维护物资分类、办公用品、部门、用户和角色等基础数据
2. 员工登录后查看物资列表、库存状态，提交物品领用申请
3. 行政人员审核领用申请，审核通过后登记物资发放记录
4. 管理员监控库存，针对低库存物资发起采购申请，完成入库操作
5. 管理员定期进行库存盘点，生成库存报表与领用数据统计
