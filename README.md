# MES 生产工单系统

一个基于 **Spring Boot 3 + MyBatis + Spring AI + Vue 3** 的制造执行系统（MES）实训项目，覆盖生产工单、报工、物料消耗、质检、仓储、设备点检等制造核心业务，并集成大模型对话能力辅助生产管理。

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.3.5、MyBatis、PageHelper |
| AI | Spring AI（DeepSeek / 阿里百炼 DashScope）、JDBC 对话记忆 |
| 前端 | Vue 3、Vite、Element Plus |
| 数据库 | MySQL 8.x |
| 构建 | Maven |

## 项目结构

```
mes-elevn/
├── src/main/java/com/elevn/mes/     # 后端源码（controller / service / mapper / entity / config）
├── src/main/resources/
│   ├── application-8081.yaml        # 多环境配置（8081 / 8082 / 8083）
│   └── mapper/                       # MyBatis Mapper XML
├── mes-front/                        # 前端 Vue3 工程
├── docs/
│   ├── sql/                          # 数据库脚本（拆分版，104 张表）
│   └── mes_260917.sql                # 整库导出一体版（参考）
└── pom.xml
```

## 快速开始

### 1. 环境要求
- JDK 17
- Maven 3.6+
- MySQL 8.x
- Node.js 16+（前端，如需运行）

### 2. 初始化数据库

创建并导入数据库（默认库名 `mes_elevn`，账号 root/root，可按实际修改脚本与 yaml）：

```bash
# 建库（自动建 mes_elevn 并切换）
mysql -uroot -proot < docs/sql/00-init-database.sql

# 建表 + 导入基础数据（按顺序执行，脚本已内置 use mes_elevn）
mysql -uroot -proot < docs/sql/mes-sys.sql
mysql -uroot -proot < docs/sql/mes-md.sql
mysql -uroot -proot < docs/sql/mes-pro.sql
mysql -uroot -proot < docs/sql/mes-qc.sql
mysql -uroot -proot < docs/sql/mes-wm.sql
mysql -uroot -proot < docs/sql/mes-dv.sql
mysql -uroot -proot < docs/sql/mes-cal.sql
mysql -uroot -proot < docs/sql/mes-codingrule.sql
mysql -uroot -proot < docs/sql/mes-tm.sql

# 导入基础数据
mysql -uroot -proot < docs/sql/mes-pro-dict.sql
mysql -uroot -proot < docs/sql/mes-wm-dict.sql
mysql -uroot -proot < docs/sql/mes-md-data.sql
mysql -uroot -proot < docs/sql/mes-pro-data.sql
mysql -uroot -proot < docs/sql/mes-workorder-data.sql

# 报工冲销改造迁移（幂等，可重复执行）
mysql -uroot -proot < docs/sql/_alter_reverse_20260923.sql
```

> 也可直接导入 `docs/mes_260917.sql`（整库导出一体版），但该版本为 101 张表、数据较拆分版少，推荐以 `docs/sql/` 为准。

### 3. 启动后端

```bash
# 单实例（默认 8081）
mvn spring-boot:run -Dspring-boot.run.profiles=8081
```

如需多实例，分别指定 `8081 / 8082 / 8083` 三个 profile 启动即可（连接同一数据库）。

### 4. 配置 AI（可选）

使用对话辅助功能前，需配置环境变量：

```bash
# Windows
set DEEPSEEK_API_KEY=your_key
set BAILIAN_API_KEY=your_key
```

三份 `application-*.yaml` 的差异仅为端口号；AI 与数据源配置一致。详见各 yaml 内注释。

## 主要功能模块

- **生产管理**：生产工单、生产任务、流转卡、报工、冲销、物料消耗
- **物料主数据**：物料档案、BOM、工艺路线、车间/工位/设备/人员
- **质量管理**：IQC / IPQC / OQC / RQC 检验、缺陷记录
- **仓储管理**：出入库单据、库存、批次、SN 追溯、盘点
- **设备管理**：设备台账、点检计划、点检记录、维修保养
- **系统管理**：用户、角色、菜单、字典、部门、岗位

## 说明

本项目为制造执行系统实训项目，用于生产工单全流程的练习与演示。数据库中已预置示例物料、工艺路线、工单等演示数据，导入后即可体验核心流程。
