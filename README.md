# 🏥 医疗设备运维管理系统

> Medical Equipment Operation & Maintenance Management System

基于 **Spring Boot 3 + JWT + MyBatis-Plus + H2** 后端 + **Android App** 的医疗设备全生命周期管理系统，覆盖"设备台账 → 扫码报修 → 工单流转 → 巡检打卡 → 统计报表"全流程。

---

## 📁 项目结构

```
medical-equipment/
├── medical-equipment-api/     # Spring Boot 后端（生产版本）
│   ├── src/main/java/com/hospital/equipment/
│   │   ├── EquipmentApplication.java    # 启动类
│   │   ├── common/                      # 通用层（Result/异常/常量/工具）
│   │   ├── config/                      # 配置类（Security/MyBatis/数据源）
│   │   ├── security/                    # JWT 过滤器
│   │   ├── modules/
│   │   │   ├── user/                    # 用户模块
│   │   │   ├── device/                  # 设备台账模块
│   │   │   ├── repairorder/             # 报修工单模块
│   │   │   ├── inspect/                 # 巡检任务模块
│   │   │   ├── notification/            # 通知公告模块
│   │   │   ├── stats/                   # 统计报表模块
│   │   │   └── file/                    # 文件上传模块
│   │   └── task/                        # 定时任务
│   ├── src/main/resources/               # 配置文件 & SQL
│   └── pom.xml
│
├── preview/                   # Node.js 预览版（Express + SQLite + Web前端）
│   ├── server.js              # Express API 服务
│   ├── package.json
│   └── public/index.html      # Web 管理界面
│
└── docs/                      # 需求文档 & API 文档
```

---

## 🚀 快速开始

### 方式一：Node.js 预览版（推荐快速体验）

```bash
cd preview
npm install
node server.js
# 浏览器访问 http://localhost:3000
```

### 方式二：Spring Boot 生产版

```bash
# 需要 JDK 17+ 和 Maven
cd medical-equipment-api
mvn spring-boot:run
# 或打包后运行
mvn package -DskipTests
java -jar target/medical-equipment-api-1.0.0.jar
# 浏览器访问 http://localhost:8080
# Swagger/Knife4j: http://localhost:8080/doc.html
# H2 控制台: http://localhost:8080/h2-console
```

---

## 🔑 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | `admin` | `123456` | 全部权限：设备管理、工单管理、用户管理、统计报表、导出 |
| 工程师 | `engineer` | `123456` | 工单接单/完工、巡检任务处理 |
| 护士 | `nurse` | `123456` | 扫码提交报修、查看我的工单、评价完工工单 |

---

## ✨ 功能模块

### 📱 用户模块
- JWT 认证登录，Token 自动续期
- 三级角色权限控制（admin / engineer / nurse）
- 修改密码、获取当前用户信息

### 🏥 设备台账
- 设备分页查询（支持关键词、科室、状态筛选）
- 设备详情（关联最近 5 条报修工单）
- 新增/编辑/删除设备（仅 admin）
- 自动生成设备二维码（ZXing）

### 📋 报修工单
- 工单状态机：待受理 → 维修中 → 待评价 → 已完成
- 护士提交报修（图片上传）
- 工程师接单、完工
- 护士星级评价
- 管理员转派/关闭工单
- 完整操作日志记录

### ✅ 巡检任务
- 每日自动生成巡检任务（@Scheduled 定时任务）
- 工程师巡检打卡
- 异常自动转报修工单

### 🔔 通知公告
- 工单受理/完工自动通知
- 计量检定到期提醒
- 未读数统计

### 📊 统计报表
- 设备总数/完好率
- 本月报修数/完成率
- 支持 EasyExcel 导出

---

## 🛠️ 技术栈

### 后端（Spring Boot）
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.x | 应用框架 |
| Spring Security | 6.x | 鉴权框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| JWT (jjwt) | 0.12.x | Token 认证 |
| H2 | 2.2.x | 内嵌数据库（开发） |
| ZXing | 3.5.x | 二维码生成 |
| Knife4j | 4.x | API 文档 |
| EasyExcel | 4.x | Excel 导出 |
| Lombok | 1.18.x | 简化代码 |

### 预览版（Node.js）
| 技术 | 用途 |
|------|------|
| Express | Web 框架 |
| better-sqlite3 | SQLite 数据库 |
| jsonwebtoken | JWT 认证 |
| bcryptjs | 密码加密 |
| Multer | 文件上传 |

### Android（规划中）
- Kotlin + MVVM
- Retrofit + OkHttp
- ZXing 扫码
- Room 离线缓存
- Glide + Luban 图片处理

---

## 📊 数据库设计

6 张核心表：
- `sys_user` - 系统用户表
- `medical_device` - 医疗设备表
- `repair_order` - 报修工单表
- `repair_order_log` - 工单操作日志表
- `inspect_task` - 巡检任务表
- `notification` - 通知公告表

详细建表 SQL 见 `medical-equipment-api/src/main/resources/schema.sql`

---

## 📡 API 接口列表

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 用户登录 |
| 用户 | GET | `/api/user/info` | 当前用户信息 |
| 用户 | PUT | `/api/user/password` | 修改密码 |
| 设备 | GET | `/api/device/page` | 设备分页列表 |
| 设备 | GET | `/api/device/{id}` | 设备详情 |
| 设备 | POST | `/api/device` | 新增设备 |
| 设备 | PUT | `/api/device` | 编辑设备 |
| 设备 | DELETE | `/api/device/{id}` | 删除设备 |
| 设备 | GET | `/api/device/{id}/qrcode` | 生成设备二维码 |
| 工单 | POST | `/api/repair-order/v2` | 提交报修 |
| 工单 | GET | `/api/repair-order/page` | 工单分页列表 |
| 工单 | GET | `/api/repair-order/my` | 我的工单 |
| 工单 | GET | `/api/repair-order/{id}` | 工单详情 |
| 工单 | PUT | `/api/repair-order/{id}/accept` | 接单 |
| 工单 | PUT | `/api/repair-order/{id}/finish` | 完工 |
| 工单 | PUT | `/api/repair-order/{id}/rate` | 评价 |
| 工单 | PUT | `/api/repair-order/{id}/assign` | 转派 |
| 工单 | PUT | `/api/repair-order/{id}/close` | 关闭 |
| 巡检 | GET | `/api/inspect-task/my` | 我的巡检任务 |
| 巡检 | PUT | `/api/inspect-task/{id}/submit` | 提交巡检结果 |
| 通知 | GET | `/api/notification/my` | 我的通知列表 |
| 通知 | PUT | `/api/notification/{id}/read` | 标记已读 |
| 通知 | GET | `/api/notification/unread-count` | 未读数 |
| 统计 | GET | `/api/stats/overview` | 概览统计 |

---

## 🏗️ 架构设计

```
┌─────────────────┐     ┌────────────────────┐     ┌─────────────────┐
│   Android App   │────▶│  Spring Boot API   │────▶│  MySQL / H2 DB  │
│  (Kotlin/MVVM)  │◀────│ (JWT + Security)   │◀────│  MyBatis-Plus   │
└─────────────────┘     └────────────────────┘     └─────────────────┘
           │                       │                        ▲
           │   Web Preview         │  Redis 缓存            │
           ▼                       ▼                        │
    ┌──────────────┐        ┌──────────────┐               │
    │  Web Browser │        │    Redis     │───────────────┘
    └──────────────┘        └──────────────┘
           ▲
           │  Express Preview
           │  (Node.js)
    ┌──────────────┐
    │  Node.js API │
    └──────────────┘
```

---

## 📝 简历项目描述

> **医疗设备运维管理系统** | 独立开发（Android + Spring Boot）| 2026
>
> - 基于 Spring Boot 3 + Spring Security + JWT 实现后端服务，MyBatis-Plus + H2/MySQL 存储设备台账、报修工单与巡检记录，支持管理员/工程师/护士三级角色权限；
> - 工单采用状态机模式管理流转（待受理→维修中→待评价→已完成），完整记录操作日志；
> - 基于 Spring @Scheduled 实现每日巡检任务自动派发与计量检定到期提醒；
> - 使用 ZXing 生成设备二维码，护士扫码即可提交报修；
> - 封装全局异常处理与统一返回结果体，使用 EasyExcel 实现月度报修报表导出；
> - 内嵌 H2 内存数据库，开箱即用，无需外部依赖。

---

## 📄 License

MIT License
