
-- ============================================================
-- MES 仓储模块（wm）
--
-- 设计要点：
--   1) 14 类出入库单据合并为 wm_doc / wm_doc_line / wm_doc_detail，
--      用 doc_type 区分业务、io_flag 决定库存加减方向
--   2) 3 类通知单合并为 wm_notice / wm_notice_line，用 notice_type 区分
--   3) 库区/库位合并为 wm_location（自关联树），用 location_type 区分
--   4) 数量统一 decimal(18,6)，禁用 double
--   5) 全表 del_flag 逻辑删除：0-存在 1-删除
--
-- 【单据类型字典 wm_doc.doc_type】  io_flag：I=入库(库存+)  O=出库(库存-)  T=调拨(双向)
--   ITEM_RECPT        采购入库单    I     原 wm_item_recpt
--   PRODUCT_RECPT     产品入库单    I     原 wm_product_recpt
--   PRODUCT_PRODUCE   生产入库单    I     原 wm_product_produce
--   RT_ISSUE          生产退料单    I     原 wm_rt_issue
--   RT_SALES          销售退货单    I     原 wm_rt_sales
--   OUTSOURCE_RECPT   外协入库单    I     原 wm_outsource_recpt
--   MISC_RECPT        杂项入库单    I     原 wm_misc_recpt
--   RT_VENDOR         供应商退货单  O     原 wm_rt_vendor
--   ISSUE             生产领料单    O     原 wm_issue_header
--   ITEM_CONSUME      物料消耗单    O     原 wm_item_consume
--   PRODUCT_SALES     销售出库单    O     原 wm_product_sales
--   OUTSOURCE_ISSUE   外协出库单    O     原 wm_outsource_issue
--   MISC_ISSUE        杂项出库单    O     原 wm_misc_issue
--   TRANSFER          调拨单        T     原 wm_transfer
--
-- 【通知类型字典 wm_notice.notice_type】
--   ARRIVAL           到货通知单   原 wm_arrival_notice
--   SALES             发货通知单   原 wm_sales_notice
--   MATERIAL_REQUEST  备料申请单   原 wm_materialrequest_notice
--
-- 【库区库位字典 wm_location.location_type】
--   AREA              库区（父级，parent_id=0）
--   LOCATION          库位（子级，parent_id 指向库区）
--   注意：旧版 wm_storage_location 是库区、wm_storage_area 是库位，
--         命名与 MES 惯例相反，本版已纠正为 AREA(大) / LOCATION(小)
--
-- 【来源单据软引用口径】（跨模块关联，无数据库外键）
--   source_doc_table  来源单据所在表：wm_doc / wm_notice / pro_workorder / ...
--   source_doc_type   来源单据类型：存上表枚举值，不要存表名
--   source_doc_id     来源单据ID
--   source_line_id    来源单据行ID
--   例：采购入库单 → wm_doc + ITEM_RECPT；到货通知单 → wm_notice + ARRIVAL
-- ============================================================

-- ----------------------------
-- 1、仓库表
-- ----------------------------
drop table if exists wm_warehouse;
create table wm_warehouse (
  warehouse_id        bigint(20)      not null auto_increment     comment '仓库ID',
  warehouse_code      varchar(64)     not null                    comment '仓库编码',
  warehouse_name      varchar(255)    not null                    comment '仓库名称',
  location            varchar(500)                                comment '位置',
  area                decimal(12,2)                               comment '面积',
  user_id             bigint(20)                                  comment '用户ID',
  user_name           varchar(64)                                 comment '用户名',
  charge              varchar(64)                                 comment '仓管名称',
  manager_id          bigint(20)                                  comment '主管ID',
  manager_name        varchar(64)                                 comment '主管用户名',
  manager_nick        varchar(64)                                 comment '主管名称',
  enable_flag         char(1)         default 'N'                 comment '是否启用 Y-是 N-否',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (warehouse_id),
  unique key uk_warehouse_code (warehouse_code)
) engine=innodb auto_increment=200 comment = '仓库表';


-- ----------------------------
-- 2、库区库位表（原库区表 wm_storage_location + 库位表 wm_storage_area 合并）
--    location_type：AREA-库区（父级） LOCATION-库位（挂在库区下，parent_id 指向库区）
-- ----------------------------
drop table if exists wm_location;
create table wm_location (
  location_id         bigint(20)      not null auto_increment     comment '库区/库位ID',
  parent_id           bigint(20)      default 0                   comment '父级ID（库位挂库区下，顶级为0）',
  location_type       varchar(32)     not null                    comment '类型 AREA-库区 LOCATION-库位',
  location_code       varchar(64)     not null                    comment '库区/库位编码',
  location_name       varchar(255)    not null                    comment '库区/库位名称',
  warehouse_id        bigint(20)                                  comment '所属仓库ID',
  warehouse_code      varchar(64)                                 comment '所属仓库编码',
  warehouse_name      varchar(255)                                comment '所属仓库名称',
  area                decimal(12,2)                               comment '面积',
  max_loa             decimal(12,2)                               comment '最大载重量',
  position_x          int(11)                                     comment '位置X坐标',
  position_y          int(11)                                     comment '位置Y坐标',
  position_z          int(11)                                     comment '位置Z坐标',
  area_flag           char(1)         default 'N'                 comment '是否开启库位管理（库区可用）',
  enable_flag         char(1)         default 'Y'                 comment '是否启用',
  frozen_flag         char(1)         default 'N'                 comment '是否冻结',
  product_mixing      char(1)         default 'Y'                 comment '是否允许产品混放',
  batch_mixing        char(1)         default 'Y'                 comment '是否允许批次混放',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (location_id),
  unique key uk_loc_code (location_type, location_code),
  key idx_loc_parent (parent_id),
  key idx_loc_warehouse (warehouse_id)
) engine=innodb auto_increment=200 comment = '库区库位表（自关联树，location_type 区分库区/库位）';


-- ----------------------------
-- 3、库存记录表（现存量）
--    唯一约束 uk_stock：同一物料 + 同批次 + 同仓库 + 同库区 + 同库位 + 同容器 只允许一条记录。
--    为让约束真正生效，批次/库区/库位/容器用 0 与空串表示"无"，不再允许 NULL
--    （MySQL 唯一索引中 NULL 不参与比较，允许重复，是新手最容易踩的坑）。
--    出库/调拨时只允许 UPDATE 数量，不允许 DELETE 后 INSERT。
-- ----------------------------
drop table if exists wm_material_stock;
create table wm_material_stock (
  material_stock_id   bigint(20)      not null auto_increment     comment '库存记录ID',
  item_type_id        bigint(20)                                  comment '物料类型ID',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  unit_name           varchar(128)                                comment '单位名称',
  batch_id            bigint(20)      not null default 0          comment '批次ID（0-无批次）',
  batch_code          varchar(255)     not null default ''         comment '批次号',
  workorder_id        bigint(20)                                  comment '生产工单ID',
  workorder_code      varchar(64)                                 comment '生产工单编号',
  vendor_id           bigint(20)                                  comment '供应商ID',
  vendor_code         varchar(64)                                 comment '供应商编号',
  vendor_name         varchar(255)                                comment '供应商名称',
  vendor_nick         varchar(64)                                 comment '供应商简称',
  client_id           bigint(20)                                  comment '客户ID',
  client_code         varchar(64)                                 comment '客户编码',
  client_name         varchar(255)                                comment '客户名称',
  client_nick         varchar(255)                                comment '客户简称',
  warehouse_id        bigint(20)      not null                    comment '仓库ID',
  warehouse_code      varchar(64)                                 comment '仓库编码',
  warehouse_name      varchar(255)                                comment '仓库名称',
  location_id         bigint(20)      not null default 0          comment '库位ID（0-未指定）',
  location_code       varchar(64)     not null default ''         comment '库位编码',
  location_name       varchar(255)    not null default ''         comment '库位名称',
  area_id             bigint(20)      not null default 0          comment '库区ID（0-未指定）',
  area_code           varchar(64)     not null default ''         comment '库区编码',
  area_name           varchar(255)    not null default ''         comment '库区名称',
  package_id          bigint(20)      not null default 0          comment '容器ID（0-未装箱）',
  package_code        varchar(64)     not null default ''         comment '容器编号',
  quantity_onhand     decimal(18,6)   not null default 0          comment '在库数量',
  quantity_reserved   decimal(18,6)   not null default 0          comment '保留数量（已分配未出库）',
  production_date     datetime                                    comment '生产日期',
  recpt_date          datetime                                    comment '入库时间',
  expire_date         datetime                                    comment '库存有效期',
  frozen_flag         char(1)         not null default 'N'        comment '是否冻结',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (material_stock_id),
  unique key uk_stock (item_id, batch_id, warehouse_id, location_id, area_id, package_id),
  key idx_stock_item (item_id),
  key idx_stock_warehouse (warehouse_id),
  key idx_stock_batch (batch_id)
) engine=innodb auto_increment=200 comment = '库存记录表';


-- ----------------------------
-- 4、库存事务表（库存流水，只追加不修改）
-- ----------------------------
drop table if exists wm_transaction;
create table wm_transaction (
  transaction_id        bigint(20)      not null auto_increment   comment '事务ID',
  transaction_type      varchar(64)     not null                  comment '事务类型（对应 wm_doc.doc_type）',
  item_id               bigint(20)      not null                  comment '产品物料ID',
  item_code             varchar(64)                               comment '产品物料编码',
  item_name             varchar(255)                              comment '产品物料名称',
  specification         varchar(500)                              comment '规格型号',
  unit_of_measure       varchar(64)                               comment '单位',
  unit_name             varchar(128)                              comment '单位名称',
  batch_id              bigint(20)                                comment '批次ID',
  batch_code            varchar(255)                              comment '批次号',
  warehouse_id          bigint(20)      not null                  comment '仓库ID',
  warehouse_code        varchar(64)                               comment '仓库编码',
  warehouse_name        varchar(255)                              comment '仓库名称',
  location_id           bigint(20)                                comment '库位ID',
  location_code         varchar(64)                               comment '库位编码',
  location_name         varchar(255)                              comment '库位名称',
  area_id               bigint(20)                                comment '库区ID',
  area_code             varchar(64)                               comment '库区编码',
  area_name             varchar(255)                              comment '库区名称',
  package_id            bigint(20)                                comment '容器ID',
  package_code          varchar(64)                               comment '容器编号',
  source_doc_type       varchar(64)                               comment '来源单据类型',
  source_doc_id         bigint(20)                                comment '来源单据ID',
  source_doc_code       varchar(64)                               comment '来源单据编号',
  source_doc_line_id    bigint(20)                                comment '来源单据行ID',
  material_stock_id     bigint(20)      not null                  comment '库存记录ID',
  transaction_flag      int(1)          default 1                 comment '库存方向 1-入库(+) -1-出库(-)',
  transaction_quantity  decimal(18,6)   not null                  comment '事务数量（正数，方向看 transaction_flag）',
  transaction_date      datetime                                  comment '事务日期',
  related_transaction_id bigint(20)                               comment '关联的事务ID（调拨的配对行）',
  erp_date              datetime                                  comment 'ERP账期',
  recpt_date            datetime                                  comment '接收日期',
  del_flag              char(1)         default '0'               comment '删除标志 0-存在 1-删除',
  create_by             varchar(64)     default ''                comment '创建者',
  create_time           datetime                                  comment '创建时间',
  update_by             varchar(64)     default ''                comment '更新者',
  update_time           datetime                                  comment '更新时间',
  primary key (transaction_id),
  key idx_trx_item (item_id, batch_id),
  key idx_trx_doc (source_doc_type, source_doc_id),
  key idx_trx_date (transaction_date),
  key idx_trx_stock (material_stock_id)
) engine=innodb auto_increment=200 comment = '库存事务表';


-- ----------------------------
-- 5、批次记录表
-- ----------------------------
drop table if exists wm_batch;
create table wm_batch (
  batch_id            bigint(20)      not null auto_increment     comment '批次ID',
  batch_code          varchar(64)     not null                    comment '批次编号',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  produce_date        datetime                                    comment '生产日期',
  expire_date         datetime                                    comment '有效期',
  recpt_date          datetime                                    comment '入库日期',
  vendor_id           bigint(20)                                  comment '供应商ID',
  vendor_code         varchar(64)                                 comment '供应商编码',
  vendor_name         varchar(255)                                comment '供应商名称',
  vendor_nick         varchar(255)                                comment '供应商简称',
  client_id           bigint(20)                                  comment '客户ID',
  client_code         varchar(64)                                 comment '客户编码',
  client_name         varchar(255)                                comment '客户名称',
  client_nick         varchar(255)                                comment '客户简称',
  so_code             varchar(64)                                 comment '销售订单编号（原 co_code）',
  po_code             varchar(64)                                 comment '采购订单编号',
  workorder_id        bigint(20)                                  comment '生产工单ID',
  workorder_code      varchar(64)                                 comment '生产工单编码',
  task_id             bigint(20)                                  comment '生产任务ID',
  task_code           varchar(64)                                 comment '生产任务编号',
  workstation_id      bigint(20)                                  comment '工作站ID',
  workstation_code    varchar(64)                                 comment '工作站编码',
  tool_id             bigint(20)                                  comment '工具ID',
  tool_code           varchar(64)                                 comment '工具编号',
  mold_id             bigint(20)                                  comment '模具ID',
  mold_code           varchar(64)                                 comment '模具编号',
  lot_number          varchar(128)                                comment '生产批号',
  quality_status      varchar(64)                                 comment '质量状态',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (batch_id),
  unique key uk_batch_code (batch_code),
  key idx_batch_item (item_id)
) engine=innodb auto_increment=200 comment = '批次记录表';


-- ----------------------------
-- 6、出入库单据头表
--    原 14 张单据头表合并：item_recpt / rt_vendor / issue_header / rt_issue / item_consume /
--    product_produce / product_recpt / product_sales / rt_sales / transfer /
--    outsource_issue / outsource_recpt / misc_recpt / misc_issue
-- ----------------------------
drop table if exists wm_doc;
create table wm_doc (
  doc_id              bigint(20)      not null auto_increment     comment '单据ID',
  doc_type            varchar(32)     not null                    comment '单据类型 见文件头【单据类型字典】',
  doc_code            varchar(64)     not null                    comment '单据编号',
  doc_name            varchar(255)                                comment '单据名称',
  io_flag             char(1)         not null                    comment '出入库标志 I-入库(+) O-出库(-) T-调拨(双向)',
  biz_date            datetime                                    comment '业务日期',
  required_date       datetime                                    comment '需求日期（领料单的需求时间）',
  status              varchar(64)     default 'PREPARE'           comment '单据状态 PREPARE/CONFIRMED/FINISHED/CANCELED',
  -- 上游单据
  source_doc_type     varchar(64)                                 comment '来源单据类型',
  source_doc_id       bigint(20)                                  comment '来源单据ID',
  source_doc_code     varchar(64)                                 comment '来源单据编号',
  -- 往来对象（供应商/客户/外协厂商 三选一，由 partner_type 指定）
  partner_type        varchar(32)                                 comment '往来对象类型 VENDOR/CLIENT/OUTSOURCE',
  partner_id          bigint(20)                                  comment '往来对象ID',
  partner_code        varchar(64)                                 comment '往来对象编码',
  partner_name        varchar(255)                                comment '往来对象名称',
  partner_nick        varchar(255)                                comment '往来对象简称',
  -- 外部订单号
  po_code             varchar(64)                                 comment '采购订单编号',
  so_code             varchar(64)                                 comment '销售订单编号',
  -- 生产相关
  workorder_id        bigint(20)                                  comment '生产工单ID',
  workorder_code      varchar(64)                                 comment '生产工单编码',
  workorder_name      varchar(255)                                comment '生产工单名称',
  task_id             bigint(20)                                  comment '生产任务ID',
  task_code           varchar(64)                                 comment '生产任务编号',
  task_name           varchar(255)                                comment '生产任务名称',
  workstation_id      bigint(20)                                  comment '工作站ID',
  workstation_code    varchar(64)                                 comment '工作站编码',
  workstation_name    varchar(255)                                comment '工作站名称',
  process_id          bigint(20)                                  comment '工序ID',
  process_code        varchar(64)                                 comment '工序编号',
  process_name        varchar(255)                                comment '工序名称',
  feedback_id         bigint(20)                                  comment '关联的报工单ID',
  -- 整单物料（产品入库单等"一单一产品"场景，多数单据以行为准）
  item_id             bigint(20)                                  comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  unit_name           varchar(128)                                comment '单位名称',
  -- 质量检验
  qc_type             varchar(32)                                 comment '检验类型 IQC/IPQC/OQC/FQC',
  qc_id               bigint(20)                                  comment '检验单ID',
  qc_code             varchar(64)                                 comment '检验单编号',
  -- 物流
  carrier             varchar(128)                                comment '承运商',
  shipping_number     varchar(128)                                comment '运输单号',
  recipient           varchar(128)                                comment '收货人',
  tel                 varchar(128)                                comment '联系方式',
  address             varchar(256)                                comment '收货地址/目的地',
  -- 业务分类与原因（原 misc_type / transfer_type / rt_type / rt_reason 统一）
  reason_type         varchar(64)                                 comment '业务类型 杂项类型/调拨类型/退料类型',
  reason              varchar(500)                                comment '原因说明（退货原因等）',
  -- 源仓库（整单默认库位，实际落位以 wm_doc_detail 为准）
  warehouse_id        bigint(20)                                  comment '仓库ID',
  warehouse_code      varchar(64)                                 comment '仓库编码',
  warehouse_name      varchar(255)                                comment '仓库名称',
  location_id         bigint(20)                                  comment '库位ID',
  location_code       varchar(64)                                 comment '库位编码',
  location_name       varchar(255)                                comment '库位名称',
  area_id             bigint(20)                                  comment '库区ID',
  area_code           varchar(64)                                 comment '库区编码',
  area_name           varchar(255)                                comment '库区名称',
  -- 目标仓库（仅调拨 TRANSFER 使用）
  to_warehouse_id     bigint(20)                                  comment '目标仓库ID',
  to_warehouse_code   varchar(64)                                 comment '目标仓库编码',
  to_warehouse_name   varchar(255)                                comment '目标仓库名称',
  to_location_id      bigint(20)                                  comment '目标库位ID',
  to_location_code    varchar(64)                                 comment '目标库位编码',
  to_location_name    varchar(255)                                comment '目标库位名称',
  to_area_id          bigint(20)                                  comment '目标库区ID',
  to_area_code        varchar(64)                                 comment '目标库区编码',
  to_area_name        varchar(255)                                comment '目标库区名称',
  -- 标记位
  delivery_flag       char(1)         default 'N'                 comment '是否配送（调拨单）',
  confirm_flag        char(1)         default 'N'                 comment '是否已确认（调拨单）',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (doc_id),
  unique key uk_doc_code (doc_type, doc_code),
  key idx_doc_type_status (doc_type, status),
  key idx_doc_bizdate (biz_date),
  key idx_doc_source (source_doc_type, source_doc_id),
  key idx_doc_partner (partner_type, partner_id),
  key idx_doc_workorder (workorder_id)
) engine=innodb auto_increment=200 comment = '出入库单据头表（14类单据合一，doc_type 区分）';


-- ----------------------------
-- 7、出入库单据行表
--    原 14 张单据行表合并，数量字段统一为 quantity（不再有 quantity_recived/issued/rted...）
-- ----------------------------
drop table if exists wm_doc_line;
create table wm_doc_line (
  line_id             bigint(20)      not null auto_increment     comment '行ID',
  doc_id              bigint(20)      not null                    comment '单据ID',
  doc_type            varchar(32)     not null                    comment '单据类型（冗余，便于按类型统计与防串数据）',
  line_no             int(11)                                     comment '行号',
  source_line_id      bigint(20)                                  comment '来源单据行ID（通知单行、来源单据行）',
  material_stock_id   bigint(20)                                  comment '库存记录ID（出库/调拨类必填）',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  unit_name           varchar(128)                                comment '单位名称',
  quantity            decimal(18,6)   not null                    comment '单据数量',
  quantity_actual     decimal(18,6)                               comment '实际数量（收货差异、退货实退等场景）',
  batch_id            bigint(20)                                  comment '批次ID',
  batch_code          varchar(255)                                comment '批次号',
  produce_date        datetime                                    comment '生产日期',
  expire_date         datetime                                    comment '有效期',
  lot_number          varchar(128)                                comment '生产批号',
  workorder_id        bigint(20)                                  comment '生产工单ID',
  workorder_code      varchar(64)                                 comment '生产工单编码',
  workorder_name      varchar(255)                                comment '生产工单名称',
  qc_flag             char(1)         default 'N'                 comment '是否检验',
  qc_id               bigint(20)                                  comment '检验单ID',
  qc_code             varchar(64)                                 comment '检验单编号',
  quality_status      varchar(64)                                 comment '质量状态',
  -- 以下为"建议库位"，实际落位以 wm_doc_detail 为准
  warehouse_id        bigint(20)                                  comment '建议仓库ID',
  warehouse_code      varchar(64)                                 comment '建议仓库编码',
  warehouse_name      varchar(255)                                comment '建议仓库名称',
  location_id         bigint(20)                                  comment '建议库位ID',
  location_code       varchar(64)                                 comment '建议库位编码',
  location_name       varchar(255)                                comment '建议库位名称',
  area_id             bigint(20)                                  comment '建议库区ID',
  area_code           varchar(64)                                 comment '建议库区编码',
  area_name           varchar(255)                                comment '建议库区名称',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (line_id),
  key idx_line_doc (doc_id),
  key idx_line_type (doc_type),
  key idx_line_item (item_id, batch_id),
  key idx_line_source (source_line_id)
) engine=innodb auto_increment=200 comment = '出入库单据行表';


-- ----------------------------
-- 8、出入库单据明细表（库位落位记录，过账时按此表更新库存）
--    原 14 张 detail 表字段 100% 同构，直接合一
-- ----------------------------
drop table if exists wm_doc_detail;
create table wm_doc_detail (
  detail_id           bigint(20)      not null auto_increment     comment '明细ID',
  doc_id              bigint(20)                                  comment '单据ID',
  doc_type            varchar(32)                                 comment '单据类型',
  line_id             bigint(20)                                  comment '所属行ID',
  material_stock_id   bigint(20)                                  comment '库存记录ID（出库类指向被扣减的库存）',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  unit_name           varchar(128)                                comment '单位名称',
  quantity            decimal(18,6)   not null                    comment '数量（正数，方向由单据 io_flag 决定）',
  batch_id            bigint(20)                                  comment '批次ID',
  batch_code          varchar(255)                                comment '批次号',
  warehouse_id        bigint(20)                                  comment '仓库ID',
  warehouse_code      varchar(64)                                 comment '仓库编码',
  warehouse_name      varchar(255)                                comment '仓库名称',
  location_id         bigint(20)                                  comment '库位ID',
  location_code       varchar(64)                                 comment '库位编码',
  location_name       varchar(255)                                comment '库位名称',
  area_id             bigint(20)                                  comment '库区ID',
  area_code           varchar(64)                                 comment '库区编码',
  area_name           varchar(255)                                comment '库区名称',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (detail_id),
  key idx_detail_doc (doc_id),
  key idx_detail_line (line_id),
  key idx_detail_stock (material_stock_id),
  key idx_detail_location (warehouse_id, location_id, area_id)
) engine=innodb auto_increment=200 comment = '出入库单据明细表（库位落位记录）';


-- ----------------------------
-- 9、仓储通知单表
--    原到货通知 wm_arrival_notice + 发货通知 wm_sales_notice + 备料申请 wm_materialrequest_notice 合并
-- ----------------------------
drop table if exists wm_notice;
create table wm_notice (
  notice_id           bigint(20)      not null auto_increment     comment '通知单ID',
  notice_type         varchar(32)     not null                    comment '通知类型 ARRIVAL-到货 SALES-发货 MATERIAL_REQUEST-备料申请',
  notice_code         varchar(64)     not null                    comment '通知单编号（原备料申请缺此字段，已补）',
  notice_name         varchar(255)                                comment '通知单名称',
  po_code             varchar(64)                                 comment '采购订单编号',
  so_code             varchar(64)                                 comment '销售订单编号',
  partner_type        varchar(32)                                 comment '往来对象类型 VENDOR/CLIENT',
  partner_id          bigint(20)                                  comment '往来对象ID',
  partner_code        varchar(64)                                 comment '往来对象编码',
  partner_name        varchar(255)                                comment '往来对象名称',
  partner_nick        varchar(255)                                comment '往来对象简称',
  workorder_id        bigint(20)                                  comment '生产工单ID',
  workorder_code      varchar(64)                                 comment '生产工单编号',
  workstation_id      bigint(20)                                  comment '工作站ID',
  workstation_code    varchar(64)                                 comment '工作站编号',
  workstation_name    varchar(255)                                comment '工作站名称',
  applicant_id        bigint(20)                                  comment '申请人ID',
  applicant_name      varchar(64)                                 comment '申请人用户名',
  applicant_nick      varchar(64)                                 comment '申请人姓名',
  request_time        datetime                                    comment '申请/需求时间',
  start_time          datetime                                    comment '要求开始时间',
  end_time            datetime                                    comment '要求完成时间',
  notice_date         datetime                                    comment '通知日期（到货日期/发货日期）',
  contact             varchar(64)                                 comment '联系人',
  tel                 varchar(128)                                comment '联系方式',
  address             varchar(255)                                comment '收货地址',
  status              varchar(64)     default 'PREPARE'           comment '单据状态 PREPARE/CONFIRMED/FINISHED/CANCELED',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (notice_id),
  unique key uk_notice_code (notice_type, notice_code),
  key idx_notice_type_status (notice_type, status)
) engine=innodb auto_increment=200 comment = '仓储通知单表（到货/发货/备料申请 合一）';


-- ----------------------------
-- 10、仓储通知单行表
-- ----------------------------
drop table if exists wm_notice_line;
create table wm_notice_line (
  line_id             bigint(20)      not null auto_increment     comment '行ID',
  notice_id           bigint(20)      not null                    comment '通知单ID',
  notice_type         varchar(32)                                 comment '通知类型（冗余）',
  line_no             int(11)                                     comment '行号',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  unit_name           varchar(128)                                comment '单位名称',
  quantity            decimal(18,6)   not null                    comment '通知数量',
  quantity_qualified  decimal(18,6)                               comment '合格数量',
  batch_id            bigint(20)                                  comment '批次ID',
  batch_code          varchar(255)                                comment '批次号',
  qc_flag             char(1)         default 'N'                 comment '是否检验',
  qc_id               bigint(20)                                  comment '检验单ID',
  qc_code             varchar(64)                                 comment '检验单编号',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (line_id),
  key idx_nline_notice (notice_id),
  key idx_nline_item (item_id)
) engine=innodb auto_increment=200 comment = '仓储通知单行表';


-- ----------------------------
-- 11、装箱单表（结构特殊：有父子箱层级与体积重量，保持独立）
-- ----------------------------
drop table if exists wm_package;
create table wm_package (
  package_id          bigint(20)      not null auto_increment     comment '装箱单ID',
  parent_id           bigint(20)      not null default 0          comment '父箱ID',
  ancestors           varchar(255)    not null default '0'        comment '所有父节点ID',
  package_code        varchar(64)                                 comment '装箱单编号',
  barcode_id          bigint(20)                                  comment '条码ID',
  barcode_content     varchar(255)                                comment '条码内容',
  barcode_url         varchar(255)                                comment '条码地址',
  package_date        datetime        not null                    comment '装箱日期',
  so_code             varchar(64)                                 comment '销售订单编号',
  invoice_code        varchar(255)                                comment '发票编号',
  client_id           bigint(20)                                  comment '客户ID',
  client_code         varchar(64)                                 comment '客户编码',
  client_name         varchar(255)                                comment '客户名称',
  client_nick         varchar(255)                                comment '客户简称',
  package_length      decimal(12,4)                               comment '箱长度',
  package_width       decimal(12,4)                               comment '箱宽度',
  package_height      decimal(12,4)                               comment '箱高度',
  size_unit           varchar(64)                                 comment '尺寸单位',
  net_weight          decimal(12,4)                               comment '净重',
  gross_weight        decimal(12,4)                               comment '毛重（原 cross_weight 已修正）',
  weight_unit         varchar(64)                                 comment '重量单位',
  inspector           varchar(64)                                 comment '检查员用户名',
  inspector_name      varchar(64)                                 comment '检查员名称',
  status              varchar(64)     default 'PREPARE'           comment '状态',
  enable_flag         char(1)         default 'Y'                 comment '是否生效',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (package_id),
  unique key uk_package_code (package_code),
  key idx_package_parent (parent_id),
  key idx_package_so (so_code)
) engine=innodb auto_increment=200 comment = '装箱单表';


-- ----------------------------
-- 12、装箱明细表
-- ----------------------------
drop table if exists wm_package_line;
create table wm_package_line (
  line_id             bigint(20)      not null auto_increment     comment '明细行ID',
  package_id          bigint(20)      not null                    comment '装箱单ID',
  material_stock_id   bigint(20)                                  comment '库存记录ID',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  quantity            decimal(18,6)   not null                    comment '装箱数量（原 quantity_package）',
  workorder_id        bigint(20)                                  comment '生产工单ID',
  workorder_code      varchar(64)                                 comment '生产工单编号',
  batch_code          varchar(255)                                comment '批次号',
  warehouse_id        bigint(20)                                  comment '仓库ID',
  warehouse_code      varchar(64)                                 comment '仓库编码',
  warehouse_name      varchar(255)                                comment '仓库名称',
  location_id         bigint(20)                                  comment '库位ID',
  location_code       varchar(64)                                 comment '库位编码',
  location_name       varchar(255)                                comment '库位名称',
  area_id             bigint(20)                                  comment '库区ID',
  area_code           varchar(64)                                 comment '库区编码',
  area_name           varchar(255)                                comment '库区名称',
  expire_date         datetime                                    comment '有效期',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (line_id),
  key idx_pline_package (package_id),
  key idx_pline_item (item_id)
) engine=innodb auto_increment=200 comment = '装箱明细表';


-- ----------------------------
-- 13、库存盘点方案表（删除原 data_sql 字段，过滤条件改由 wm_stock_taking_scope 表达）
-- ----------------------------
drop table if exists wm_stock_taking_plan;
create table wm_stock_taking_plan (
  plan_id             bigint(20)      not null auto_increment     comment '盘点方案ID',
  plan_code           varchar(64)     not null                    comment '盘点方案编号',
  plan_name           varchar(255)                                comment '盘点方案名称',
  taking_type         varchar(64)     not null                    comment '盘点类型',
  start_time          datetime                                    comment '开始时间',
  end_time            datetime                                    comment '结束时间',
  blind_flag          char(1)         default 'N'                 comment '是否盲盘',
  frozen_flag         char(1)         default 'Y'                 comment '是否库存冻结',
  enable_flag         char(1)         default 'Y'                 comment '是否启用',
  status              varchar(64)     default 'PREPARE'           comment '单据状态',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (plan_id),
  unique key uk_plan_code (plan_code)
) engine=innodb auto_increment=200 comment = '库存盘点方案表';


-- ----------------------------
-- 14、库存盘点方案范围表（原 wm_stock_taking_param，替代 data_sql）
--     一个方案可配多行条件：WAREHOUSE / LOCATION / AREA / ITEM_TYPE / ITEM / VENDOR ...
-- ----------------------------
drop table if exists wm_stock_taking_scope;
create table wm_stock_taking_scope (
  scope_id            bigint(20)      not null auto_increment     comment '范围ID',
  plan_id             bigint(20)      not null                    comment '方案ID',
  scope_type          varchar(64)     not null                    comment '条件类型 WAREHOUSE/LOCATION/AREA/ITEM_TYPE/ITEM/VENDOR',
  scope_value_id      bigint(20)                                  comment '条件值ID',
  scope_value_code    varchar(64)                                 comment '条件值编码',
  scope_value_name    varchar(128)                                comment '条件值名称',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (scope_id),
  key idx_scope_plan (plan_id),
  key idx_scope_type (scope_type, scope_value_id)
) engine=innodb auto_increment=200 comment = '库存盘点方案范围表';


-- ----------------------------
-- 15、库存盘点任务表
-- ----------------------------
drop table if exists wm_stock_taking;
create table wm_stock_taking (
  taking_id           bigint(20)      not null auto_increment     comment '盘点单ID',
  taking_code         varchar(64)     not null                    comment '盘点单编号',
  taking_name         varchar(255)                                comment '盘点单名称',
  taking_date         datetime        not null                    comment '盘点日期',
  taking_type         varchar(64)     not null                    comment '盘点类型',
  user_id             bigint(20)                                  comment '盘点人ID',
  user_name           varchar(64)                                 comment '盘点人用户名',
  nick_name           varchar(64)                                 comment '盘点人',
  blind_flag          char(1)         default 'N'                 comment '是否盲盘',
  frozen_flag         char(1)         default 'Y'                 comment '是否库存冻结',
  plan_id             bigint(20)                                  comment '方案ID',
  plan_code           varchar(64)                                 comment '方案编号',
  plan_name           varchar(128)                                comment '方案名称',
  start_time          datetime                                    comment '开始时间',
  end_time            datetime                                    comment '结束时间',
  status              varchar(64)     default 'PREPARE'           comment '单据状态',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (taking_id),
  unique key uk_taking_code (taking_code),
  key idx_taking_plan (plan_id),
  key idx_taking_date (taking_date)
) engine=innodb auto_increment=200 comment = '库存盘点任务表';


-- ----------------------------
-- 16、库存盘点明细表（原 wm_stock_taking_line + wm_stock_taking_result 合并）
--     一行同时记录：quantity 账面数量、taking_quantity 实盘数量、diff_quantity 差异数量
-- ----------------------------
drop table if exists wm_stock_taking_line;
create table wm_stock_taking_line (
  line_id             bigint(20)      not null auto_increment     comment '行ID',
  taking_id           bigint(20)      not null                    comment '盘点单ID',
  material_stock_id   bigint(20)                                  comment '库存记录ID',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  unit_name           varchar(128)                                comment '单位名称',
  batch_id            bigint(20)                                  comment '批次ID',
  batch_code          varchar(128)                                comment '批次编号',
  quantity            decimal(18,6)   not null default 0          comment '账面数量',
  taking_quantity     decimal(18,6)                               comment '实盘数量',
  diff_quantity       decimal(18,6)                               comment '差异数量（实盘-账面，过账时回填）',
  warehouse_id        bigint(20)                                  comment '仓库ID',
  warehouse_code      varchar(64)                                 comment '仓库编码',
  warehouse_name      varchar(255)                                comment '仓库名称',
  location_id         bigint(20)                                  comment '库位ID',
  location_code       varchar(64)                                 comment '库位编码',
  location_name       varchar(255)                                comment '库位名称',
  area_id             bigint(20)                                  comment '库区ID',
  area_code           varchar(64)                                 comment '库区编码',
  area_name           varchar(255)                                comment '库区名称',
  taking_status       varchar(64)     default 'UNCHECKED'         comment '盘点状态 UNCHECKED-未盘 NORMAL-无差异 PROFIT-盘盈 LOSS-盘亏',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (line_id),
  key idx_tline_taking (taking_id),
  key idx_tline_stock (material_stock_id),
  key idx_tline_item (item_id)
) engine=innodb auto_increment=200 comment = '库存盘点明细表（账面数与实盘数同行存放）';


-- ----------------------------
-- 17、条码清单表
-- ----------------------------
drop table if exists wm_barcode;
create table wm_barcode (
  barcode_id          bigint(20)      not null auto_increment     comment '条码ID',
  barcode_format      varchar(64)     not null                    comment '条码格式',
  barcode_type        varchar(64)     not null                    comment '条码类型',
  barcode_content     varchar(255)    not null                    comment '条码内容',
  biz_id              bigint(20)      not null                    comment '业务ID',
  biz_code            varchar(64)                                 comment '业务编码',
  biz_name            varchar(255)                                comment '业务名称',
  barcode_url         varchar(255)                                comment '条码地址',
  enable_flag         char(1)         default 'Y'                 comment '是否生效',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (barcode_id),
  key idx_barcode_biz (barcode_type, biz_id)
) engine=innodb auto_increment=200 comment = '条码清单表';


-- ----------------------------
-- 18、条码配置表
-- ----------------------------
drop table if exists wm_barcode_config;
create table wm_barcode_config (
  config_id           bigint(20)      not null auto_increment     comment '配置ID',
  barcode_format      varchar(64)     not null                    comment '条码格式',
  barcode_type        varchar(64)     not null                    comment '条码类型',
  content_format      varchar(255)    not null                    comment '内容格式',
  content_example     varchar(255)                                comment '内容样例',
  auto_gen_flag       char(1)         default 'Y'                 comment '是否自动生成',
  default_template    varchar(255)                                comment '默认的打印模板',
  enable_flag         char(1)         default 'Y'                 comment '是否生效',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (config_id),
  unique key uk_bcconfig (barcode_type, barcode_format)
) engine=innodb auto_increment=200 comment = '条码配置表';


-- ----------------------------
-- 19、SN码表（序列号）
-- ----------------------------
drop table if exists wm_sn;
create table wm_sn (
  sn_id               bigint(20)      not null auto_increment     comment 'SN码ID',
  sn_code             varchar(64)     not null                    comment 'SN码',
  item_id             bigint(20)      not null                    comment '产品物料ID',
  item_code           varchar(64)                                 comment '产品物料编码',
  item_name           varchar(255)                                comment '产品物料名称',
  specification       varchar(500)                                comment '规格型号',
  unit_of_measure     varchar(64)                                 comment '单位',
  batch_code          varchar(255)                                comment '批次号',
  gen_date            datetime                                    comment '生成时间',
  workorder_id        bigint(20)                                  comment '生产工单ID',
  status              varchar(32)     default 'UNUSED'            comment '状态 UNUSED-未使用 USED-已使用 SCRAPPED-已报废',
  del_flag            char(1)         default '0'                 comment '删除标志 0-存在 1-删除',
  remark              varchar(500)    default ''                  comment '备注',
  create_by           varchar(64)     default ''                  comment '创建者',
  create_time         datetime                                    comment '创建时间',
  update_by           varchar(64)     default ''                  comment '更新者',
  update_time         datetime                                    comment '更新时间',
  primary key (sn_id),
  unique key uk_sn_code (sn_code),
  key idx_sn_item (item_id),
  key idx_sn_workorder (workorder_id)
) engine=innodb auto_increment=200 comment = 'SN码表';


