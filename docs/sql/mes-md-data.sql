-- ============================================================
-- MES【主数据 md 模块】演示数据
-- 由 docs/scripts/gen_md_data.py 自动生成（2026-09-21 15:44:28）
--
-- 执行顺序：先 00-init-database.sql 建库，再 mes-*.sql 建表，最后执行本文件
--   mysql -uroot -p mes_elevn < docs/sql/mes-md-data.sql
--
-- 约定：
--   1) 所有演示数据主键固定在 1001~1999 段，与表 auto_increment=200 的初始段错开；
--   2) 每张表先 delete「本表演示数据实际占用的 ID 区间」再 insert，脚本可重复执行；
--      区间是按数据算出来的（不是写死 1001~1999），所以你在页面上新增的记录不会被误删。
--      注意：表的 auto_increment 会被演示数据顶到演示 ID 之后，你新增的数据会继续往后排，
--      建议把各表 auto_increment 调到 2000 起，让演示数据和业务数据彻底分开。
--   3) Part 0 是 md_workstation 依赖的外模块数据（工序/线边库/库区库位/设备/岗位/工装类型），
--      若这些表你已有真实数据，跳过 Part 0 并把 Part 1 里 md_workstation 的对应 ID 改掉即可。
-- ============================================================

set names utf8mb4;
use mes_elevn;

-- ############################################################
-- Part 0  外模块依赖数据（可按需跳过）
-- ############################################################

-- ----------------------------
-- 生产岗位 sys_post
-- 补充生产一线岗位（默认只有 CEO/经理/员工）
-- ----------------------------
delete from sys_post where post_id between 1001 and 1005;
insert into sys_post (
  post_id,
  post_code,
  post_name,
  post_sort,
  status,
  del_flag,
  create_by,
  create_time,
  update_by,
  update_time,
  remark
) values
(1001, 'ASSEMBLER', '装配工', 10, '0', '0', 'admin', '2026-09-01 09:00:00', '', null, '演示数据-生产岗位'),
(1002, 'WELDER', '焊工', 11, '0', '0', 'admin', '2026-09-01 09:00:00', '', null, '演示数据-生产岗位'),
(1003, 'QC', '质检员', 12, '0', '0', 'admin', '2026-09-01 09:00:00', '', null, '演示数据-生产岗位'),
(1004, 'OPERATOR', '设备操作员', 13, '0', '0', 'admin', '2026-09-01 09:00:00', '', null, '演示数据-生产岗位'),
(1005, 'PACKER', '包装工', 14, '0', '0', 'admin', '2026-09-01 09:00:00', '', null, '演示数据-生产岗位');

-- ----------------------------
-- 生产工序 pro_process
-- 下料→焊接→装配→接线→测试→老化→包装
-- ----------------------------
delete from pro_process where process_id between 1001 and 1007;
insert into pro_process (
  process_id,
  process_code,
  process_name,
  attention,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'PR-10', '下料', '按图纸尺寸下料，切口去毛刺，材料需有材质证明书', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'PR-20', '焊接', '氩弧焊满焊，焊后打磨平整，焊缝不得有气孔夹渣', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'PR-30', '装配', '按装配图纸作业，紧固件扭矩 8N·m，不得漏装', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null),
(1004, 'PR-40', '接线', '线号与图纸一致，压接牢固，走线束扎整齐', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null),
(1005, 'PR-50', '测试', '耐压 1500V/1min 不击穿，功能测试全项通过', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null),
(1006, 'PR-60', '老化', '常温老化 48 小时，每 8 小时记录一次运行电流', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null),
(1007, 'PR-70', '包装', '内衬珍珠棉，封箱后贴铭牌与合格证标签', 'Y', '0', '演示数据', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 线边库 wm_warehouse
-- 3 个线边库，供工作站物料暂存
-- ----------------------------
delete from wm_warehouse where warehouse_id between 1001 and 1003;
insert into wm_warehouse (
  warehouse_id,
  warehouse_code,
  warehouse_name,
  location,
  area,
  user_id,
  user_name,
  charge,
  manager_id,
  manager_name,
  manager_nick,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'WH-LB01', '一线边库（钣金）', '钣金车间西侧', 320.0, null, '', '周国强', null, '', '', 'Y', '0', '演示数据-线边库', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'WH-LB02', '二线边库（装配）', '总装车间东侧', 260.0, null, '', '孙丽', null, '', '', 'Y', '0', '演示数据-线边库', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'WH-LB03', '成品线边库', '包装车间北侧', 400.0, null, '', '吴涛', null, '', '', 'Y', '0', '演示数据-线边库', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 库区/库位 wm_location
-- 3 个库区 + 8 个库位，库位挂在库区下
-- ----------------------------
delete from wm_location where location_id between 1001 and 1018;
insert into wm_location (
  location_id,
  parent_id,
  location_type,
  location_code,
  location_name,
  warehouse_id,
  warehouse_code,
  warehouse_name,
  area,
  max_loa,
  position_x,
  position_y,
  position_z,
  area_flag,
  enable_flag,
  frozen_flag,
  product_mixing,
  batch_mixing,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 0, 'AREA', 'AREA-A01', '钣金线边区', 1001, 'WH-LB01', '一线边库（钣金）', 120.0, 5000.0, null, null, null, 'Y', 'Y', 'N', 'Y', 'Y', '0', '演示数据-库区', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 0, 'AREA', 'AREA-B01', '装配线边区', 1002, 'WH-LB02', '二线边库（装配）', 100.0, 5000.0, null, null, null, 'Y', 'Y', 'N', 'Y', 'Y', '0', '演示数据-库区', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 0, 'AREA', 'AREA-C01', '成品暂存区', 1003, 'WH-LB03', '成品线边库', 150.0, 5000.0, null, null, null, 'Y', 'Y', 'N', 'Y', 'Y', '0', '演示数据-库区', 'admin', '2026-09-01 09:00:00', '', null),
(1011, 1001, 'LOCATION', 'LOC-A01-01', '钣金料架A-01', 1001, 'WH-LB01', '一线边库（钣金）', 12.0, 800.0, 10, 1, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1012, 1001, 'LOCATION', 'LOC-A01-02', '钣金料架A-02', 1001, 'WH-LB01', '一线边库（钣金）', 12.0, 800.0, 20, 2, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1013, 1002, 'LOCATION', 'LOC-B01-01', '装配料架B-01', 1002, 'WH-LB02', '二线边库（装配）', 12.0, 800.0, 10, 1, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1014, 1002, 'LOCATION', 'LOC-B01-02', '装配料架B-02', 1002, 'WH-LB02', '二线边库（装配）', 12.0, 800.0, 20, 2, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1015, 1002, 'LOCATION', 'LOC-B01-03', '装配料架B-03', 1002, 'WH-LB02', '二线边库（装配）', 12.0, 800.0, 30, 3, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1016, 1002, 'LOCATION', 'LOC-B01-04', '装配料架B-04', 1002, 'WH-LB02', '二线边库（装配）', 12.0, 800.0, 40, 4, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1017, 1003, 'LOCATION', 'LOC-C01-01', '成品暂存位C-01', 1003, 'WH-LB03', '成品线边库', 12.0, 800.0, 10, 1, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null),
(1018, 1003, 'LOCATION', 'LOC-C01-02', '成品暂存位C-02', 1003, 'WH-LB03', '成品线边库', 12.0, 800.0, 20, 2, 1, 'N', 'Y', 'N', 'N', 'N', '0', '演示数据-库位', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 设备类型 dv_machinery_type
-- 加工/焊接/检测/电子/包装
-- ----------------------------
delete from dv_machinery_type where machinery_type_id between 1001 and 1005;
insert into dv_machinery_type (
  machinery_type_id,
  machinery_type_code,
  machinery_type_name,
  parent_type_id,
  ancestors,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'MT-01', '加工设备', 0, '0', 'Y', '0', '演示数据-设备类型', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'MT-02', '焊接设备', 0, '0', 'Y', '0', '演示数据-设备类型', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'MT-03', '检测设备', 0, '0', 'Y', '0', '演示数据-设备类型', 'admin', '2026-09-01 09:00:00', '', null),
(1004, 'MT-04', '电子设备', 0, '0', 'Y', '0', '演示数据-设备类型', 'admin', '2026-09-01 09:00:00', '', null),
(1005, 'MT-05', '包装设备', 0, '0', 'Y', '0', '演示数据-设备类型', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 设备 dv_machinery
-- 9 台设备，按车间归属
-- ----------------------------
delete from dv_machinery where machinery_id between 1001 and 1009;
insert into dv_machinery (
  machinery_id,
  machinery_code,
  machinery_name,
  machinery_brand,
  machinery_spec,
  machinery_type_id,
  machinery_type_code,
  machinery_type_name,
  workshop_id,
  workshop_code,
  workshop_name,
  last_mainten_time,
  last_check_time,
  status,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'CNC-001', '数控冲床', '亚威', 'HPH-3048', 1001, 'MT-01', '加工设备', 1001, 'WS-01', '钣金车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1002, 'CUT-001', '数控剪板机', '亚威', 'QC12Y-6x3200', 1001, 'MT-01', '加工设备', 1001, 'WS-01', '钣金车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1003, 'WELD-001', '氩弧焊机', '松下', 'YC-315TX', 1002, 'MT-02', '焊接设备', 1001, 'WS-01', '钣金车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1004, 'WELD-002', '点焊机', '松下', 'YR-350', 1002, 'MT-02', '焊接设备', 1001, 'WS-01', '钣金车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'MAINTAIN', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1005, 'SMT-001', '自动贴片机', '雅马哈', 'YS24', 1004, 'MT-04', '电子设备', 1002, 'WS-02', '电子装配车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1006, 'REFLOW-001', '回流焊炉', '劲拓', 'JT-800', 1004, 'MT-04', '电子设备', 1002, 'WS-02', '电子装配车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1007, 'TEST-001', '耐压测试仪', '长盛', 'CS2670C', 1003, 'MT-03', '检测设备', 1003, 'WS-03', '总装车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1008, 'TEST-002', '功能测试台', '自制', 'FCT-A100', 1003, 'MT-03', '检测设备', 1003, 'WS-03', '总装车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00'),
(1009, 'PACK-001', '自动打包机', '永创', 'YC-101', 1005, 'MT-05', '包装设备', 1004, 'WS-04', '包装车间', '2026-08-15 09:00:00', '2026-08-28 09:00:00', 'RUNNING', '0', '演示数据-设备', 'admin', '2026-09-01 09:00:00', 'admin', '2026-09-02 14:20:00');

-- ----------------------------
-- 工装夹具类型 tm_tool_type
-- 夹具/模具/量具/电动工具
-- ----------------------------
delete from tm_tool_type where tool_type_id between 1001 and 1004;
insert into tm_tool_type (
  tool_type_id,
  tool_type_code,
  tool_type_name,
  code_flag,
  mainten_type,
  mainten_period,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'TT-01', '夹具', 'Y', 'PERIOD', 90, '0', '演示数据-工装夹具类型', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'TT-02', '模具', 'Y', 'PERIOD', 180, '0', '演示数据-工装夹具类型', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'TT-03', '量具', 'Y', 'PERIOD', 365, '0', '演示数据-工装夹具类型', 'admin', '2026-09-01 09:00:00', '', null),
(1004, 'TT-04', '电动工具', 'N', 'PERIOD', 180, '0', '演示数据-工装夹具类型', 'admin', '2026-09-01 09:00:00', '', null);


-- ############################################################
-- Part 1  md 模块主数据
-- ############################################################

-- ----------------------------
-- 计量单位 md_unit_measure
-- 主单位 + 辅助单位换算（如 1 箱 = 100 个）
-- ----------------------------
delete from md_unit_measure where measure_id between 1001 and 1010;
insert into md_unit_measure (
  measure_id,
  measure_code,
  measure_name,
  primary_flag,
  primary_id,
  change_rate,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'PCS', '个', 'Y', null, 1, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'BOX', '箱', 'N', 1001, 100, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'PAL', '托盘', 'N', 1001, 500, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1004, 'KG', '千克', 'Y', null, 1, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1005, 'G', '克', 'N', 1004, 0.001, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1006, 'T', '吨', 'N', 1004, 1000, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1007, 'M', '米', 'Y', null, 1, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1008, 'M2', '平方米', 'Y', null, 1, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1009, 'ROLL', '卷', 'N', 1007, 100, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null),
(1010, 'SET', '套', 'Y', null, 1, 'Y', '0', '演示数据-计量单位', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 物料分类 md_item_type
-- 原材料 / 半成品 / 成品 三层树
-- ----------------------------
delete from md_item_type where item_type_id between 1001 and 1011;
insert into md_item_type (
  item_type_id,
  item_type_code,
  item_type_name,
  parent_type_id,
  ancestors,
  item_or_product,
  order_num,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'RAW', '原材料', 0, '0', 'ITEM', 1, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'RAW-METAL', '金属材料', 1001, '0,1001', 'ITEM', 1, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'RAW-ELEC', '电子元件', 1001, '0,1001', 'ITEM', 2, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1004, 'RAW-PLASTIC', '塑胶件', 1001, '0,1001', 'ITEM', 3, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1005, 'RAW-PACK', '包装材料', 1001, '0,1001', 'ITEM', 4, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1006, 'WIP', '半成品', 0, '0', 'ITEM', 2, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1007, 'WIP-MODULE', '功能模块', 1006, '0,1006', 'ITEM', 1, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1008, 'WIP-ASSY', '组件', 1006, '0,1006', 'ITEM', 2, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1009, 'FG', '成品', 0, '0', 'PRODUCT', 3, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1010, 'FG-MACHINE', '整机', 1009, '0,1009', 'PRODUCT', 1, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null),
(1011, 'FG-SPARE', '配件', 1009, '0,1009', 'PRODUCT', 2, 'Y', '0', '演示数据-物料分类', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 物料产品 md_item
-- 4 成品 + 3 半成品 + 19 原材料（含 4 条为演示替代关系补的同类物料）
-- ----------------------------
delete from md_item where item_id between 1001 and 1053;
insert into md_item (
  item_id,
  item_code,
  item_name,
  specification,
  unit_of_measure,
  unit_name,
  item_or_product,
  item_type_id,
  item_type_code,
  item_type_name,
  enable_flag,
  safe_stock_flag,
  min_stock,
  max_stock,
  high_value,
  batch_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'FG-CTRL-A100', '智能控制柜A100', 'A100/380V/50Hz', 'SET', '套', 'PRODUCT', 1010, 'FG-MACHINE', '整机', 'Y', 'Y', 20, 200, 'Y', 'Y', '0', '主推机型，出口版需加贴CE标签', 'admin', '2026-09-01 10:30:00', '', null),
(1002, 'FG-CTRL-B200', '智能控制柜B200', 'B200/380V/50Hz', 'SET', '套', 'PRODUCT', 1010, 'FG-MACHINE', '整机', 'Y', 'Y', 10, 120, 'Y', 'Y', '0', '双电源冗余机型', 'admin', '2026-09-01 10:30:00', '', null),
(1003, 'FG-SENS-300', '温度传感器TS-300', 'TS-300/-40~150℃', 'PCS', '个', 'PRODUCT', 1010, 'FG-MACHINE', '整机', 'Y', 'Y', 100, 1000, 'N', 'Y', '0', '常规备货机型', 'admin', '2026-09-01 10:30:00', '', null),
(1004, 'SP-BRK-01', '断路器配件BRK-01', 'BRK-01/63A', 'PCS', '个', 'PRODUCT', 1011, 'FG-SPARE', '配件', 'Y', 'N', 0, 0, 'N', 'N', '0', '售后配件，按需生产', 'admin', '2026-09-01 10:30:00', '', null),
(1011, 'SF-MB-01', '主控板组件MB-01', 'MB-01/Rev2.1', 'PCS', '个', 'ITEM', 1007, 'WIP-MODULE', '功能模块', 'Y', 'Y', 50, 500, 'Y', 'Y', '0', '含程序烧录', 'admin', '2026-09-01 10:30:00', '', null),
(1012, 'SF-PS-01', '电源模块组件PS-01', 'PS-01/24V-10A', 'PCS', '个', 'ITEM', 1007, 'WIP-MODULE', '功能模块', 'Y', 'Y', 50, 400, 'Y', 'Y', '0', '需耐压测试', 'admin', '2026-09-01 10:30:00', '', null),
(1013, 'SF-DOOR-01', '柜门组件DR-01', 'DR-01/1000x800', 'PCS', '个', 'ITEM', 1008, 'WIP-ASSY', '组件', 'Y', 'N', 0, 0, 'N', 'Y', '0', '含钣金焊接与喷涂', 'admin', '2026-09-01 10:30:00', '', null),
(1021, 'RM-STEEL-01', '冷轧钢板', '1.5mm/Q235', 'KG', '千克', 'ITEM', 1002, 'RAW-METAL', '金属材料', 'Y', 'Y', 500, 5000, 'N', 'Y', '0', '需材质证明书', 'admin', '2026-09-01 10:30:00', '', null),
(1022, 'RM-STEEL-02', '不锈钢板', '2.0mm/304', 'KG', '千克', 'ITEM', 1002, 'RAW-METAL', '金属材料', 'Y', 'Y', 300, 3000, 'N', 'Y', '0', '防潮存放', 'admin', '2026-09-01 10:30:00', '', null),
(1023, 'RM-ALU-01', '铝型材', '4040/长度6m', 'M', '米', 'ITEM', 1002, 'RAW-METAL', '金属材料', 'Y', 'N', 0, 0, 'N', 'N', '0', '定尺采购', 'admin', '2026-09-01 10:30:00', '', null),
(1024, 'RM-SCREW-01', '内六角螺丝', 'M6x20/不锈钢', 'PCS', '个', 'ITEM', 1002, 'RAW-METAL', '金属材料', 'Y', 'Y', 2000, 20000, 'N', 'Y', '0', '通用件', 'admin', '2026-09-01 10:30:00', '', null),
(1031, 'RM-CHIP-01', 'MCU主控芯片', 'STM32F407VGT6', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 200, 2000, 'Y', 'Y', '0', '高价值，静电防护', 'admin', '2026-09-01 10:30:00', '', null),
(1032, 'RM-RELAY-01', '继电器', '24V/10A', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 300, 3000, 'N', 'Y', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1033, 'RM-CAP-01', '电解电容', '470uF/35V', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 1000, 10000, 'N', 'Y', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1034, 'RM-PCB-01', 'PCB空板', '双层/FR-4/150x100', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 500, 5000, 'N', 'Y', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1035, 'RM-CABLE-01', '屏蔽线缆', 'RVVP 4x0.75', 'M', '米', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 1000, 8000, 'N', 'Y', '0', '按米发料', 'admin', '2026-09-01 10:30:00', '', null),
(1036, 'RM-LCD-01', '液晶显示屏', '3.5寸/TFT', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'N', 0, 0, 'Y', 'Y', '0', '高价值，单独包装', 'admin', '2026-09-01 10:30:00', '', null),
(1041, 'RM-PLASTIC-01', 'ABS外壳注塑件', 'ABS/白色/120x80', 'PCS', '个', 'ITEM', 1004, 'RAW-PLASTIC', '塑胶件', 'Y', 'N', 0, 0, 'N', 'N', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1042, 'RM-PLASTIC-02', '尼龙扎带', '100mm/白色', 'PCS', '个', 'ITEM', 1004, 'RAW-PLASTIC', '塑胶件', 'Y', 'N', 0, 0, 'N', 'N', '0', '低值易耗', 'admin', '2026-09-01 10:30:00', '', null),
(1051, 'RM-CARTON-01', '包装纸箱', '5层瓦楞/600x500x400', 'PCS', '个', 'ITEM', 1005, 'RAW-PACK', '包装材料', 'Y', 'Y', 200, 2000, 'N', 'N', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1052, 'RM-FOAM-01', 'EPE珍珠棉缓冲垫', '10mm/500x400', 'PCS', '个', 'ITEM', 1005, 'RAW-PACK', '包装材料', 'Y', 'N', 0, 0, 'N', 'N', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1053, 'RM-LABEL-01', '不干胶标签', '80x50/铜版纸', 'PCS', '个', 'ITEM', 1005, 'RAW-PACK', '包装材料', 'Y', 'N', 0, 0, 'N', 'N', '0', '', 'admin', '2026-09-01 10:30:00', '', null),
(1025, 'RM-SCREW-02', '十字盘头螺丝', 'M6x20/不锈钢', 'PCS', '个', 'ITEM', 1002, 'RAW-METAL', '金属材料', 'Y', 'Y', 2000, 20000, 'N', 'Y', '0', '与 RM-SCREW-01 通用件，可双向互换', 'admin', '2026-09-01 10:30:00', '', null),
(1037, 'RM-CHIP-02', '国产MCU芯片', 'CH32F407VCT6', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 100, 1000, 'Y', 'Y', '0', 'RM-CHIP-01 的国产替代方案，需固件适配', 'admin', '2026-09-01 10:30:00', '', null),
(1038, 'RM-LCD-02', '国产液晶显示屏', '3.5寸/TFT', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'N', 0, 0, 'N', 'Y', '0', 'RM-LCD-01 的同尺寸替代屏，需换驱动板', 'admin', '2026-09-01 10:30:00', '', null),
(1039, 'RM-CAP-02', '固态电容', '470uF/35V', 'PCS', '个', 'ITEM', 1003, 'RAW-ELEC', '电子元件', 'Y', 'Y', 500, 5000, 'N', 'Y', '0', 'RM-CAP-01 的同规格固态替代', 'admin', '2026-09-01 10:30:00', '', null);

-- ----------------------------
-- 产品BOM md_product_bom
-- 成品→半成品→原材料 两级展开
-- ----------------------------
delete from md_product_bom where bom_id between 1001 and 1039;
insert into md_product_bom (
  bom_id,
  item_id,
  bom_item_id,
  bom_item_code,
  bom_item_name,
  bom_item_spec,
  unit_of_measure,
  item_or_product,
  quantity,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 1011, 'SF-MB-01', '主控板组件MB-01', 'MB-01/Rev2.1', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1002, 1001, 1012, 'SF-PS-01', '电源模块组件PS-01', 'PS-01/24V-10A', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1003, 1001, 1013, 'SF-DOOR-01', '柜门组件DR-01', 'DR-01/1000x800', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1004, 1001, 1036, 'RM-LCD-01', '液晶显示屏', '3.5寸/TFT', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1005, 1001, 1024, 'RM-SCREW-01', '内六角螺丝', 'M6x20/不锈钢', 'PCS', 'ITEM', 24, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1006, 1001, 1035, 'RM-CABLE-01', '屏蔽线缆', 'RVVP 4x0.75', 'M', 'ITEM', 5, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1007, 1001, 1042, 'RM-PLASTIC-02', '尼龙扎带', '100mm/白色', 'PCS', 'ITEM', 10, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1008, 1001, 1051, 'RM-CARTON-01', '包装纸箱', '5层瓦楞/600x500x400', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1009, 1001, 1052, 'RM-FOAM-01', 'EPE珍珠棉缓冲垫', '10mm/500x400', 'PCS', 'ITEM', 4, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1010, 1001, 1053, 'RM-LABEL-01', '不干胶标签', '80x50/铜版纸', 'PCS', 'ITEM', 2, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1011, 1002, 1011, 'SF-MB-01', '主控板组件MB-01', 'MB-01/Rev2.1', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1012, 1002, 1012, 'SF-PS-01', '电源模块组件PS-01', 'PS-01/24V-10A', 'PCS', 'ITEM', 2, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1013, 1002, 1013, 'SF-DOOR-01', '柜门组件DR-01', 'DR-01/1000x800', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1014, 1002, 1036, 'RM-LCD-01', '液晶显示屏', '3.5寸/TFT', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1015, 1002, 1024, 'RM-SCREW-01', '内六角螺丝', 'M6x20/不锈钢', 'PCS', 'ITEM', 32, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1016, 1002, 1035, 'RM-CABLE-01', '屏蔽线缆', 'RVVP 4x0.75', 'M', 'ITEM', 8, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1017, 1002, 1042, 'RM-PLASTIC-02', '尼龙扎带', '100mm/白色', 'PCS', 'ITEM', 14, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1018, 1002, 1051, 'RM-CARTON-01', '包装纸箱', '5层瓦楞/600x500x400', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1019, 1002, 1052, 'RM-FOAM-01', 'EPE珍珠棉缓冲垫', '10mm/500x400', 'PCS', 'ITEM', 6, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1020, 1002, 1053, 'RM-LABEL-01', '不干胶标签', '80x50/铜版纸', 'PCS', 'ITEM', 3, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1021, 1003, 1031, 'RM-CHIP-01', 'MCU主控芯片', 'STM32F407VGT6', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1022, 1003, 1034, 'RM-PCB-01', 'PCB空板', '双层/FR-4/150x100', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1023, 1003, 1033, 'RM-CAP-01', '电解电容', '470uF/35V', 'PCS', 'ITEM', 3, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1024, 1003, 1041, 'RM-PLASTIC-01', 'ABS外壳注塑件', 'ABS/白色/120x80', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1025, 1003, 1035, 'RM-CABLE-01', '屏蔽线缆', 'RVVP 4x0.75', 'M', 'ITEM', 1.5, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1026, 1003, 1053, 'RM-LABEL-01', '不干胶标签', '80x50/铜版纸', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1027, 1004, 1032, 'RM-RELAY-01', '继电器', '24V/10A', 'PCS', 'ITEM', 2, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1028, 1004, 1024, 'RM-SCREW-01', '内六角螺丝', 'M6x20/不锈钢', 'PCS', 'ITEM', 4, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1029, 1004, 1041, 'RM-PLASTIC-01', 'ABS外壳注塑件', 'ABS/白色/120x80', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1030, 1011, 1031, 'RM-CHIP-01', 'MCU主控芯片', 'STM32F407VGT6', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1031, 1011, 1034, 'RM-PCB-01', 'PCB空板', '双层/FR-4/150x100', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1032, 1011, 1033, 'RM-CAP-01', '电解电容', '470uF/35V', 'PCS', 'ITEM', 5, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1033, 1011, 1032, 'RM-RELAY-01', '继电器', '24V/10A', 'PCS', 'ITEM', 2, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1034, 1012, 1034, 'RM-PCB-01', 'PCB空板', '双层/FR-4/150x100', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1035, 1012, 1033, 'RM-CAP-01', '电解电容', '470uF/35V', 'PCS', 'ITEM', 4, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1036, 1012, 1032, 'RM-RELAY-01', '继电器', '24V/10A', 'PCS', 'ITEM', 1, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1037, 1013, 1021, 'RM-STEEL-01', '冷轧钢板', '1.5mm/Q235', 'KG', 'ITEM', 8, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1038, 1013, 1023, 'RM-ALU-01', '铝型材', '4040/长度6m', 'M', 'ITEM', 2, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null),
(1039, 1013, 1024, 'RM-SCREW-01', '内六角螺丝', 'M6x20/不锈钢', 'PCS', 'ITEM', 12, 'Y', '0', '演示数据-BOM', 'admin', '2026-09-01 10:30:00', '', null);

-- ----------------------------
-- 批次属性配置 md_item_batch_config
-- 按物料类型配置需要记录的批次属性
-- ----------------------------
delete from md_item_batch_config where config_id between 1001 and 1009;
insert into md_item_batch_config (
  config_id,
  item_id,
  produce_date_flag,
  expire_date_flag,
  recpt_date_flag,
  vendor_flag,
  client_flag,
  co_code_flag,
  po_code_flag,
  workorder_flag,
  task_flag,
  workstation_flag,
  tool_flag,
  mold_flag,
  lot_number_flag,
  quality_status_flag,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 'Y', 'N', 'N', 'N', 'Y', 'Y', 'N', 'Y', 'Y', 'Y', 'Y', 'N', 'Y', 'Y', 'Y', '0', '成品按生产工单与销售订单追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1002, 1003, 'Y', 'N', 'N', 'N', 'Y', 'Y', 'N', 'Y', 'Y', 'Y', 'Y', 'N', 'Y', 'Y', 'Y', '0', '成品按生产工单与销售订单追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1003, 1011, 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', 'N', 'N', 'Y', 'Y', 'Y', '0', '半成品按工单与任务追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1004, 1013, 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', 'N', 'N', 'Y', 'Y', 'Y', '0', '半成品按工单与任务追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1005, 1021, 'Y', 'N', 'Y', 'Y', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', '0', '钢板按炉批号与供应商追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1006, 1024, 'Y', 'N', 'Y', 'Y', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', '0', '通用件按采购订单追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1007, 1031, 'Y', 'N', 'Y', 'Y', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', '0', '芯片按原厂批号追溯，有效期管理', 'admin', '2026-09-02 14:20:00', '', null),
(1008, 1035, 'Y', 'N', 'Y', 'Y', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', '0', '线缆按供应商批次追溯', 'admin', '2026-09-02 14:20:00', '', null),
(1009, 1036, 'Y', 'N', 'Y', 'Y', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y', 'Y', '0', '显示屏按供应商批次追溯', 'admin', '2026-09-02 14:20:00', '', null);

-- ----------------------------
-- 供应商 md_vendor
-- 8 家供应商
-- ----------------------------
delete from md_vendor where vendor_id between 1001 and 1008;
insert into md_vendor (
  vendor_id,
  vendor_code,
  vendor_name,
  vendor_nick,
  vendor_en,
  vendor_des,
  vendor_logo,
  vendor_level,
  vendor_score,
  address,
  website,
  email,
  tel,
  contact1,
  contact1_tel,
  contact1_email,
  contact2,
  contact2_tel,
  contact2_email,
  credit_code,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'SUP-0001', '苏州精工金属材料有限公司', '精工金属', 'Suzhou Jinggong Metal Co., Ltd.', '冷轧板、不锈钢板、铝型材供应商，合作 6 年', '', 'A', 92, '江苏省苏州市吴中区胥口镇工业大道 88 号', 'www.jinggong-metal.com', 'sales@jinggong-metal.com', '0512-6688-1020', '李伟', '138-1266-3301', 'liwei@jinggong-metal.com', '赵敏', '138-1266-3302', 'zhaomin@jinggong-metal.com', '91320500MA1MWXYZ8K', 'Y', '0', '金属原材料', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1002, 'SUP-0002', '深圳市华芯电子科技有限公司', '华芯电子', 'Shenzhen Huaxin Electronics Co., Ltd.', 'MCU、电容、继电器等电子元器件代理商', '', 'A', 95, '广东省深圳市宝安区西乡街道兴业路 2008 号', 'www.huaxin-elec.com', 'sales@huaxin-elec.com', '0755-2999-7788', '陈锋', '135-0987-6543', 'chenfeng@huaxin-elec.com', '林静', '135-0987-6544', 'linjing@huaxin-elec.com', '91440300MA5EABCD7N', 'Y', '0', '电子元件', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1003, 'SUP-0003', '无锡华通塑胶制品有限公司', '华通塑胶', 'Wuxi Huatong Plastics Co., Ltd.', '注塑件开模与批量生产', '', 'B', 85, '江苏省无锡市新吴区旺庄工业园 66 号', 'www.huatong-plastic.com', 'sales@huatong-plastic.com', '0510-8522-6611', '周涛', '139-2155-8802', 'zhoutao@huatong-plastic.com', '', '', '', '91320214MA1QWERT3F', 'Y', '0', '塑胶件', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1004, 'SUP-0004', '上海恒力紧固件有限公司', '恒力紧固', 'Shanghai Hengli Fastener Co., Ltd.', '标准紧固件，支持小批量快发', '', 'B', 82, '上海市嘉定区马陆镇希望路 388 号', 'www.hengli-fastener.com', 'sales@hengli-fastener.com', '021-5951-2277', '钱进', '137-0175-4433', 'qianjin@hengli-fastener.com', '', '', '', '91310114MA1HABCD9M', 'Y', '0', '紧固件', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1005, 'SUP-0005', '常州市远大包装材料有限公司', '远大包装', 'Changzhou Yuanda Packaging Co., Ltd.', '纸箱、珍珠棉、标签定制', '', 'C', 75, '江苏省常州市武进区湖塘镇纺织工业园 12 号', 'www.yuanda-pack.com', 'sales@yuanda-pack.com', '0519-8655-3090', '孙丽华', '138-0612-7765', 'sunlh@yuanda-pack.com', '', '', '', '91320412MA1PZXCV5T', 'Y', '0', '包装材料', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1006, 'SUP-0006', '杭州智联显示技术有限公司', '智联显示', 'Hangzhou Zhilian Display Tech Co., Ltd.', '工业液晶屏模组原厂', '', 'A', 90, '浙江省杭州市滨江区江陵路 88 号智慧产业园 A 座', 'www.zhilian-display.com', 'sales@zhilian-display.com', '0571-8666-5210', '吴敏', '138-5712-3344', 'wumin@zhilian-display.com', '郑凯', '138-5712-3345', 'zhengkai@zhilian-display.com', '91330108MA2GHJKL6R', 'Y', '0', '显示模组', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1007, 'SUP-0007', '广东线缆集团股份有限公司', '广东线缆', 'Guangdong Cable Group Co., Ltd.', '屏蔽线缆、动力电缆国标厂家', '', 'A', 93, '广东省东莞市虎门镇线缆大道 1 号', 'www.gd-cable.com', 'sales@gd-cable.com', '0769-8555-1000', '何强', '139-2299-6677', 'heqiang@gd-cable.com', '梁芳', '139-2299-6678', 'liangfang@gd-cable.com', '91441900MA4BNMLK2Q', 'Y', '0', '线缆', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1008, 'SUP-0008', '宁波市精诚模具制造有限公司', '精诚模具', 'Ningbo Jingcheng Mould Co., Ltd.', '钣金模具与工装夹具制造', '', 'B', 88, '浙江省宁波市北仑区模具园区 26 号', 'www.jingcheng-mould.com', 'sales@jingcheng-mould.com', '0574-8677-2244', '徐刚', '137-0574-1122', 'xugang@jingcheng-mould.com', '', '', '', '91330206MA2CLPOI8W', 'Y', '0', '模具/夹具', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00');

-- ----------------------------
-- 客户 md_client
-- 8 家客户
-- ----------------------------
delete from md_client where client_id between 1001 and 1008;
insert into md_client (
  client_id,
  client_code,
  client_name,
  client_nick,
  client_en,
  client_des,
  client_logo,
  client_type,
  address,
  website,
  email,
  tel,
  contact1,
  contact1_tel,
  contact1_email,
  contact2,
  contact2_tel,
  contact2_email,
  credit_code,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'CUS-0001', '华东智能装备股份有限公司', '华东智装', 'Huadong Intelligent Equipment Co., Ltd.', '大型成套设备集成商，年采购额 3000 万', '', 'ENTERPRISE', '江苏省南京市江宁区将军大道 66 号', 'www.hd-ie.com', 'purchase@hd-ie.com', '025-5218-9000', '陆建国', '139-0518-2266', 'lujg@hd-ie.com', '韩雪', '139-0518-2267', 'hanxue@hd-ie.com', '91320115MA1ABCDE0X', 'Y', '0', '战略客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1002, 'CUS-0002', '北方电力自动化有限公司', '北方电力', 'North Power Automation Co., Ltd.', '电力系统自动化改造项目客户', '', 'ENTERPRISE', '天津市西青区华苑产业区海泰大道 18 号', 'www.np-auto.com', 'buy@np-auto.com', '022-5866-3300', '马涛', '138-2022-7788', 'matao@np-auto.com', '', '', '', '91120116MA0FGHIJ5B', 'Y', '0', '行业客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1003, 'CUS-0003', '中远重工集团有限责任公司', '中远重工', 'Zhongyuan Heavy Industry Group', '央企下属装备制造公司，账期 90 天', '', 'ENTERPRISE', '辽宁省大连市甘井子区海中街 1 号', 'www.zyhi.com', 'procurement@zyhi.com', '0411-8677-5500', '宋宏伟', '137-0411-9090', 'songhw@zyhi.com', '刘洋', '137-0411-9091', 'liuyang@zyhi.com', '91210200MA3JKLMN7C', 'Y', '0', '大客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1004, 'CUS-0004', '深圳科创电子有限公司', '深圳科创', 'Shenzhen Kechuang Electronics Co., Ltd.', '中小型电子制造企业，按需下单', '', 'ENTERPRISE', '广东省深圳市龙岗区坂田街道科技园 5 栋', 'www.kc-elec.com', 'order@kc-elec.com', '0755-8333-2200', '黄志文', '135-1088-4455', 'huangzw@kc-elec.com', '', '', '', '91440300MA6OPQRS1D', 'Y', '0', '常规客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1005, 'CUS-0005', '江苏新能源科技有限公司', '江苏新能', 'Jiangsu New Energy Tech Co., Ltd.', '储能柜配套采购，增长型客户', '', 'ENTERPRISE', '江苏省常州市金坛区华城路 168 号', 'www.jsne-tech.com', 'buy@jsne-tech.com', '0519-8288-6600', '严磊', '138-6118-2233', 'yanlei@jsne-tech.com', '', '', '', '91320413MA7TUVWX9E', 'Y', '0', '增长客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1006, 'CUS-0006', '成都轨道交通设备有限公司', '成都轨交', 'Chengdu Rail Transit Equipment Co., Ltd.', '轨道交通配套，需提供第三方检测报告', '', 'ENTERPRISE', '四川省成都市郫都区现代工业港北区 88 号', 'www.cdrt-equip.com', 'purchase@cdrt-equip.com', '028-8788-1122', '谭小刚', '136-8822-5566', 'tanxg@cdrt-equip.com', '杨梅', '136-8822-5567', 'yangmei@cdrt-equip.com', '91510124MA8YZABC3K', 'Y', '0', '行业客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1007, 'CUS-0007', '青岛蓝海智控有限公司', '蓝海智控', 'Qingdao Lanhai Intelligent Control Co., Ltd.', '智慧楼宇解决方案商', '', 'ENTERPRISE', '山东省青岛市黄岛区江山南路 66 号', 'www.lanhai-ic.com', 'sales@lanhai-ic.com', '0532-8688-9900', '段永平', '137-0532-6688', 'duanyp@lanhai-ic.com', '', '', '', '91370211MA9BCDFG4P', 'Y', '0', '常规客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00'),
(1008, 'CUS-0008', '武汉光谷自动化有限公司', '光谷自动化', 'Wuhan Optics Valley Automation Co., Ltd.', '高校合作单位，小批量试样为主', '', 'ENTERPRISE', '湖北省武汉市东湖新技术开发区高新大道 999 号', 'www.ov-auto.com', 'contact@ov-auto.com', '027-8777-3300', '潘峰', '139-0718-4455', 'panfeng@ov-auto.com', '', '', '', '91420100MA0HIJKL8N', 'Y', '0', '试样客户', 'admin', '2026-09-01 10:30:00', 'admin', '2026-09-03 16:45:00');

-- ----------------------------
-- 车间 md_workshop
-- 钣金 / 电子装配 / 总装 / 包装
-- ----------------------------
delete from md_workshop where workshop_id between 1001 and 1004;
insert into md_workshop (
  workshop_id,
  workshop_code,
  workshop_name,
  area,
  charge,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'WS-01', '钣金车间', 1200.0, '张建国', 'Y', '0', '负责下料、焊接、打磨；两班制', 'admin', '2026-09-01 09:00:00', '', null),
(1002, 'WS-02', '电子装配车间', 800.0, '李慧敏', 'Y', '0', '恒温恒湿，静电防护区', 'admin', '2026-09-01 09:00:00', '', null),
(1003, 'WS-03', '总装车间', 1500.0, '王志强', 'Y', '0', '整机装配、接线、测试、老化', 'admin', '2026-09-01 09:00:00', '', null),
(1004, 'WS-04', '包装车间', 600.0, '陈晓峰', 'Y', '0', '成品包装与入库交接', 'admin', '2026-09-01 09:00:00', '', null);

-- ----------------------------
-- 工作站 md_workstation
-- 车间 + 工序 + 线边库 + 库区 + 库位
-- ----------------------------
delete from md_workstation where workstation_id between 1001 and 1009;
insert into md_workstation (
  workstation_id,
  workstation_code,
  workstation_name,
  workstation_address,
  workshop_id,
  workshop_code,
  workshop_name,
  process_id,
  process_code,
  process_name,
  warehouse_id,
  warehouse_code,
  warehouse_name,
  area_id,
  area_code,
  area_name,
  location_id,
  location_code,
  location_name,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 'ST-01', '下料工作站', '钣金车间A区1号', 1001, 'WS-01', '钣金车间', 1001, 'PR-10', '下料', 1001, 'WH-LB01', '一线边库（钣金）', 1001, 'AREA-A01', '钣金线边区', 1011, 'LOC-A01-01', '钣金料架A-01', 'Y', '0', '配置数控冲床与剪板机', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1002, 'ST-02', '焊接工作站', '钣金车间A区2号', 1001, 'WS-01', '钣金车间', 1002, 'PR-20', '焊接', 1001, 'WH-LB01', '一线边库（钣金）', 1001, 'AREA-A01', '钣金线边区', 1012, 'LOC-A01-02', '钣金料架A-02', 'Y', '0', '配置氩弧焊机与点焊机', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1003, 'ST-03', '贴片工作站', '电子车间B区1号', 1002, 'WS-02', '电子装配车间', 1003, 'PR-30', '装配', 1002, 'WH-LB02', '二线边库（装配）', 1002, 'AREA-B01', '装配线边区', 1013, 'LOC-B01-01', '装配料架B-01', 'Y', '0', 'SMT 产线首站', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1004, 'ST-04', '波峰焊接线', '电子车间B区2号', 1002, 'WS-02', '电子装配车间', 1002, 'PR-20', '焊接', 1002, 'WH-LB02', '二线边库（装配）', 1002, 'AREA-B01', '装配线边区', 1014, 'LOC-B01-02', '装配料架B-02', 'Y', '0', '回流焊与手工补焊', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1005, 'ST-05', '总装工作站', '总装车间C区1号', 1003, 'WS-03', '总装车间', 1003, 'PR-30', '装配', 1002, 'WH-LB02', '二线边库（装配）', 1002, 'AREA-B01', '装配线边区', 1015, 'LOC-B01-03', '装配料架B-03', 'Y', '0', '整机装配主线', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1006, 'ST-06', '接线工作站', '总装车间C区2号', 1003, 'WS-03', '总装车间', 1004, 'PR-40', '接线', 1002, 'WH-LB02', '二线边库（装配）', 1002, 'AREA-B01', '装配线边区', 1015, 'LOC-B01-03', '装配料架B-03', 'Y', '0', '二次接线与线束整理', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1007, 'ST-07', '功能测试站', '总装车间C区3号', 1003, 'WS-03', '总装车间', 1005, 'PR-50', '测试', 1002, 'WH-LB02', '二线边库（装配）', 1002, 'AREA-B01', '装配线边区', 1016, 'LOC-B01-04', '装配料架B-04', 'Y', '0', '耐压与功能测试', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1008, 'ST-08', '老化测试站', '总装车间C区4号', 1003, 'WS-03', '总装车间', 1006, 'PR-60', '老化', 1002, 'WH-LB02', '二线边库（装配）', 1002, 'AREA-B01', '装配线边区', 1016, 'LOC-B01-04', '装配料架B-04', 'Y', '0', '常温老化 48 小时', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00'),
(1009, 'ST-09', '包装工作站', '包装车间D区1号', 1004, 'WS-04', '包装车间', 1007, 'PR-70', '包装', 1003, 'WH-LB03', '成品线边库', 1003, 'AREA-C01', '成品暂存区', 1017, 'LOC-C01-01', '成品暂存位C-01', 'Y', '0', '成品包装与贴标', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-05 08:50:00');

-- ----------------------------
-- 工作站设备资源 md_workstation_machine
-- 工作站挂设备
-- ----------------------------
delete from md_workstation_machine where record_id between 1001 and 1010;
insert into md_workstation_machine (
  record_id,
  workstation_id,
  machinery_id,
  machinery_code,
  machinery_name,
  quantity,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 1001, 'CNC-001', '数控冲床', 1, '0', '主力设备', 'admin', '2026-09-02 14:20:00', '', null),
(1002, 1001, 1002, 'CUT-001', '数控剪板机', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1003, 1002, 1003, 'WELD-001', '氩弧焊机', 2, '0', '两台轮替作业', 'admin', '2026-09-02 14:20:00', '', null),
(1004, 1002, 1004, 'WELD-002', '点焊机', 1, '0', '保养中，暂不可用', 'admin', '2026-09-02 14:20:00', '', null),
(1005, 1003, 1005, 'SMT-001', '自动贴片机', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1006, 1004, 1006, 'REFLOW-001', '回流焊炉', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1007, 1007, 1007, 'TEST-001', '耐压测试仪', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1008, 1007, 1008, 'TEST-002', '功能测试台', 2, '0', '双工位并行测试', 'admin', '2026-09-02 14:20:00', '', null),
(1009, 1008, 1008, 'TEST-002', '功能测试台', 2, '0', '老化过程监测', 'admin', '2026-09-02 14:20:00', '', null),
(1010, 1009, 1009, 'PACK-001', '自动打包机', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null);

-- ----------------------------
-- 工作站人力资源 md_workstation_worker
-- 工作站挂岗位与定员
-- ----------------------------
delete from md_workstation_worker where record_id between 1001 and 1009;
insert into md_workstation_worker (
  record_id,
  workstation_id,
  post_id,
  post_code,
  post_name,
  quantity,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 1004, 'OPERATOR', '设备操作员', 2, '0', '设备操作员', 'admin', '2026-09-02 14:20:00', '', null),
(1002, 1002, 1002, 'WELDER', '焊工', 2, '0', '持焊工证上岗', 'admin', '2026-09-02 14:20:00', '', null),
(1003, 1003, 1004, 'OPERATOR', '设备操作员', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1004, 1004, 1001, 'ASSEMBLER', '装配工', 2, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1005, 1005, 1001, 'ASSEMBLER', '装配工', 4, '0', '主线装配', 'admin', '2026-09-02 14:20:00', '', null),
(1006, 1006, 1001, 'ASSEMBLER', '装配工', 2, '0', '二次接线', 'admin', '2026-09-02 14:20:00', '', null),
(1007, 1007, 1003, 'QC', '质检员', 1, '0', '', 'admin', '2026-09-02 14:20:00', '', null),
(1008, 1008, 1003, 'QC', '质检员', 1, '0', '老化巡检', 'admin', '2026-09-02 14:20:00', '', null),
(1009, 1009, 1005, 'PACKER', '包装工', 3, '0', '打包与贴标', 'admin', '2026-09-02 14:20:00', '', null);

-- ----------------------------
-- 工作站工装夹具 md_workstation_tool
-- 工作站挂工装夹具类型
-- ----------------------------
delete from md_workstation_tool where record_id between 1001 and 1009;
insert into md_workstation_tool (
  record_id,
  workstation_id,
  tool_type_id,
  tool_type_code,
  tool_type_name,
  quantity,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 1003, 'TT-03', '量具', 2, '0', '游标卡尺、卷尺', 'admin', '2026-09-02 14:20:00', '', null),
(1002, 1002, 1001, 'TT-01', '夹具', 3, '0', '焊接夹具', 'admin', '2026-09-02 14:20:00', '', null),
(1003, 1002, 1002, 'TT-02', '模具', 1, '0', '柜门成型模', 'admin', '2026-09-02 14:20:00', '', null),
(1004, 1003, 1004, 'TT-04', '电动工具', 2, '0', '电动螺丝刀', 'admin', '2026-09-02 14:20:00', '', null),
(1005, 1004, 1004, 'TT-04', '电动工具', 2, '0', '电动螺丝刀', 'admin', '2026-09-02 14:20:00', '', null),
(1006, 1005, 1001, 'TT-01', '夹具', 2, '0', '装配夹具', 'admin', '2026-09-02 14:20:00', '', null),
(1007, 1006, 1004, 'TT-04', '电动工具', 3, '0', '压线钳、电动螺丝刀', 'admin', '2026-09-02 14:20:00', '', null),
(1008, 1007, 1003, 'TT-03', '量具', 1, '0', '万用表', 'admin', '2026-09-02 14:20:00', '', null),
(1009, 1009, 1004, 'TT-04', '电动工具', 1, '0', '打包辅助工具', 'admin', '2026-09-02 14:20:00', '', null);

-- ----------------------------
-- 产品SOP md_product_sop
-- 按工序编写作业指导
-- ----------------------------
delete from md_product_sop where sop_id between 1001 and 1009;
insert into md_product_sop (
  sop_id,
  item_id,
  order_num,
  process_id,
  process_code,
  process_name,
  sop_title,
  sop_description,
  sop_url,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 1, 1001, 'PR-10', '下料', '钢板下料作业指导', '按图纸核对材质与厚度，设定冲床模具，首件检验合格后方可批量下料，每 50 件抽检一次尺寸。', '/files/sop/A100-01.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1002, 1001, 2, 1002, 'PR-20', '焊接', '柜体焊接作业指导', '焊接前清理油污，按焊接顺序对称施焊控制变形，焊后打磨并做外观自检。', '/files/sop/A100-02.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1003, 1001, 3, 1003, 'PR-30', '装配', '整机装配作业指导', '先装电源模块再装主控板，紧固扭矩 8N·m，装配完成后核对铭牌信息。', '/files/sop/A100-03.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1004, 1001, 4, 1004, 'PR-40', '接线', '二次接线作业指导', '按接线图布线，线号管齐全，压接后做拉力自检，走线束扎间距不大于 150mm。', '/files/sop/A100-04.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1005, 1001, 5, 1005, 'PR-50', '测试', '功能测试作业指导', '先做耐压测试再做功能测试，测试数据录入 MES，异常贴红标隔离。', '/files/sop/A100-05.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1006, 1001, 6, 1007, 'PR-70', '包装', '包装入库作业指导', '内衬珍珠棉四角到位，封箱后贴铭牌与合格证，扫码入库。', '/files/sop/A100-06.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1007, 1003, 1, 1003, 'PR-30', '装配', '传感器装配作业指导', '芯片防静电操作，焊接温度 350℃，装配后检查外壳密封圈。', '/files/sop/TS300-01.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1008, 1003, 2, 1005, 'PR-50', '测试', '传感器标定作业指导', '在恒温槽中做三点标定，误差超过 ±0.5℃ 需返工。', '/files/sop/TS300-02.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null),
(1009, 1003, 3, 1007, 'PR-70', '包装', '传感器包装作业指导', '单只独立包装，附校准证书，10 只一盒。', '/files/sop/TS300-03.pdf', 'Y', '0', '演示数据-SOP', 'admin', '2026-09-03 16:45:00', '', null);

-- ----------------------------
-- 产品SIP md_product_sip
-- 来料/过程/出厂检验标准
-- ----------------------------
delete from md_product_sip where sip_id between 1001 and 1007;
insert into md_product_sip (
  sip_id,
  item_id,
  order_num,
  process_id,
  process_code,
  process_name,
  sip_title,
  sip_description,
  sip_url,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1001, 1, null, '', '', '钢板来料检验标准', '核对材质证明书，测厚抽检 5 张，表面不得有严重划伤与锈斑。', '/files/sip/A100-IQC.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null),
(1002, 1001, 2, 1002, 'PR-20', '焊接', '焊接过程检验标准', '焊缝外观 100% 目视，关键焊缝按 10% 抽检做渗透检测。', '/files/sip/A100-IPQC-01.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null),
(1003, 1001, 3, 1004, 'PR-40', '接线', '接线过程检验标准', '线号与图纸一致性 100% 核对，压接拉力测试每批抽检 3 根。', '/files/sip/A100-IPQC-02.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null),
(1004, 1001, 4, 1005, 'PR-50', '测试', '成品出厂检验标准', '耐压、功能、外观、铭牌四项全检，任一不合格判定不合格。', '/files/sip/A100-OQC.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null),
(1005, 1001, 5, 1007, 'PR-70', '包装', '包装检验标准', '外箱无破损，标签与实物一致，随机附件齐全。', '/files/sip/A100-PACK.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null),
(1006, 1003, 1, null, '', '', '芯片来料检验标准', '核对原厂批号与防伪标识，抽测 3 只做功能验证。', '/files/sip/TS300-IQC.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null),
(1007, 1003, 2, 1005, 'PR-50', '测试', '传感器成品检验标准', '精度、线性度、绝缘电阻三项全检，附校准证书。', '/files/sip/TS300-OQC.pdf', 'Y', '0', '演示数据-SIP', 'admin', '2026-09-03 16:45:00', '', null);

-- ----------------------------
-- 物料替代品 md_item_substitute
-- 单向国产替代 / 双向互换 / 用量比 1.05 / 已停用记录各一条
-- ----------------------------
delete from md_item_substitute where substitute_id between 1001 and 1006;
insert into md_item_substitute (
  substitute_id,
  item_id,
  item_code,
  item_name,
  sub_item_id,
  sub_item_code,
  sub_item_name,
  sub_item_spec,
  unit_of_measure,
  substitute_type,
  substitute_ratio,
  priority,
  effective_date,
  expire_date,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1031, 'RM-CHIP-01', 'MCU主控芯片', 1037, 'RM-CHIP-02', '国产MCU芯片', 'CH32F407VCT6', 'PCS', 'ONE_WAY', 1.0, 1, '2026-01-01', null, 'Y', '0', '进口 MCU → 国产 MCU，单向替代，需固件适配', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1002, 1036, 'RM-LCD-01', '液晶显示屏', 1038, 'RM-LCD-02', '国产液晶显示屏', '3.5寸/TFT', 'PCS', 'ONE_WAY', 1.0, 1, '2026-03-01', null, 'Y', '0', '同尺寸国产屏替代，需更换驱动板', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1003, 1024, 'RM-SCREW-01', '内六角螺丝', 1025, 'RM-SCREW-02', '十字盘头螺丝', 'M6x20/不锈钢', 'PCS', 'TWO_WAY', 1.0, 1, '2026-01-01', null, 'Y', '0', '同为 M6x20 不锈钢件，双向互换', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1004, 1033, 'RM-CAP-01', '电解电容', 1039, 'RM-CAP-02', '固态电容', '470uF/35V', 'PCS', 'TWO_WAY', 1.0, 1, '2026-01-01', null, 'Y', '0', '同为 470uF/35V，固态与电解电容双向互换', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1005, 1021, 'RM-STEEL-01', '冷轧钢板', 1022, 'RM-STEEL-02', '不锈钢板', '2.0mm/304', 'KG', 'ONE_WAY', 1.05, 2, '2026-01-01', null, 'Y', '0', '防腐场景用不锈钢板替代冷轧板，用量比 1.05 计入加工损耗', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1006, 1023, 'RM-ALU-01', '铝型材', 1021, 'RM-STEEL-01', '冷轧钢板', '1.5mm/Q235', 'KG', 'ONE_WAY', 1.15, 3, '2025-06-01', '2026-06-01', 'N', '0', '曾用钢板替代铝型材做横梁，2026 年评估结果不通过已停用，保留记录', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00');

-- ----------------------------
-- 物料供应商 md_item_vendor
-- 同一物料挂多家供应商，演示不同价格与交期
-- ----------------------------
delete from md_item_vendor where item_vendor_id between 1001 and 1030;
insert into md_item_vendor (
  item_vendor_id,
  item_id,
  item_code,
  item_name,
  vendor_id,
  vendor_code,
  vendor_name,
  vendor_item_code,
  vendor_item_name,
  purchase_price,
  currency,
  min_order_qty,
  lead_time,
  primary_flag,
  priority,
  effective_date,
  expire_date,
  enable_flag,
  del_flag,
  remark,
  create_by,
  create_time,
  update_by,
  update_time
) values
(1001, 1031, 'RM-CHIP-01', 'MCU主控芯片', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-STM32F407', 'MCU主控芯片', 46.5, 'CNY', 100, 15, 'Y', 1, '2026-01-01', null, 'Y', '0', '主供，原厂代理', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1002, 1031, 'RM-CHIP-01', 'MCU主控芯片', 1006, 'SUP-0006', '杭州智联显示技术有限公司', 'ZL-MCU-STM32', 'MCU主控芯片', 48.2, 'CNY', 100, 20, 'N', 2, '2026-01-01', null, 'Y', '0', '备选，价格高但现货', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1003, 1037, 'RM-CHIP-02', '国产MCU芯片', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-CH32F407', '国产MCU芯片', 23.8, 'CNY', 200, 12, 'Y', 1, '2026-01-01', null, 'Y', '0', '国产替代料主供', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1004, 1032, 'RM-RELAY-01', '继电器', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-RLY-2410', '继电器', 12.6, 'CNY', 300, 10, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1005, 1032, 'RM-RELAY-01', '继电器', 1006, 'SUP-0006', '杭州智联显示技术有限公司', 'ZL-RY-2410', '继电器', 13.1, 'CNY', 200, 18, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1006, 1033, 'RM-CAP-01', '电解电容', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-EC-47035', '电解电容', 1.85, 'CNY', 1000, 7, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1007, 1033, 'RM-CAP-01', '电解电容', 1006, 'SUP-0006', '杭州智联显示技术有限公司', 'ZL-CAP-47035', '电解电容', 1.92, 'CNY', 1000, 12, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1008, 1039, 'RM-CAP-02', '固态电容', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-SC-47035', '固态电容', 3.4, 'CNY', 500, 10, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1009, 1034, 'RM-PCB-01', 'PCB空板', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-PCB-150100', 'PCB空板', 8.9, 'CNY', 500, 12, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1010, 1034, 'RM-PCB-01', 'PCB空板', 1008, 'SUP-0008', '宁波市精诚模具制造有限公司', 'JC-PCB-FR4-2L', 'PCB空板', 7.6, 'CNY', 1000, 20, 'N', 2, '2026-01-01', null, 'Y', '0', '价格低但交期长', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1011, 1035, 'RM-CABLE-01', '屏蔽线缆', 1007, 'SUP-0007', '广东线缆集团股份有限公司', 'GD-RVVP-4C075', '屏蔽线缆', 6.2, 'CNY', 500, 8, 'Y', 1, '2026-01-01', null, 'Y', '0', '按米计价', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1012, 1035, 'RM-CABLE-01', '屏蔽线缆', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-RVVP-4075', '屏蔽线缆', 6.55, 'CNY', 300, 10, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1013, 1036, 'RM-LCD-01', '液晶显示屏', 1006, 'SUP-0006', '杭州智联显示技术有限公司', 'ZL-TFT-35', '液晶显示屏', 62.0, 'CNY', 50, 15, 'Y', 1, '2026-01-01', null, 'Y', '0', '含驱动板', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1014, 1036, 'RM-LCD-01', '液晶显示屏', 1002, 'SUP-0002', '深圳市华芯电子科技有限公司', 'HX-LCD-35TFT', '液晶显示屏', 58.5, 'CNY', 100, 25, 'N', 2, '2026-01-01', null, 'Y', '0', '价格低但交期长', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1015, 1038, 'RM-LCD-02', '国产液晶显示屏', 1006, 'SUP-0006', '杭州智联显示技术有限公司', 'ZL-TFT-35B', '国产液晶显示屏', 38.0, 'CNY', 100, 12, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1016, 1021, 'RM-STEEL-01', '冷轧钢板', 1001, 'SUP-0001', '苏州精工金属材料有限公司', 'JG-Q235-15', '冷轧钢板', 5.2, 'CNY', 1000, 5, 'Y', 1, '2026-01-01', null, 'Y', '0', '按公斤计价', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1017, 1021, 'RM-STEEL-01', '冷轧钢板', 1008, 'SUP-0008', '宁波市精诚模具制造有限公司', 'JC-STEEL-Q235', '冷轧钢板', 5.05, 'CNY', 2000, 12, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1018, 1022, 'RM-STEEL-02', '不锈钢板', 1001, 'SUP-0001', '苏州精工金属材料有限公司', 'JG-SUS304-20', '不锈钢板', 18.6, 'CNY', 500, 7, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1019, 1023, 'RM-ALU-01', '铝型材', 1001, 'SUP-0001', '苏州精工金属材料有限公司', 'JG-ALU-4040', '铝型材', 32.0, 'CNY', 100, 10, 'Y', 1, '2026-01-01', null, 'Y', '0', '定尺 6 米', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1020, 1023, 'RM-ALU-01', '铝型材', 1008, 'SUP-0008', '宁波市精诚模具制造有限公司', 'JC-ALU-4040-6M', '铝型材', 30.5, 'CNY', 200, 18, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1021, 1024, 'RM-SCREW-01', '内六角螺丝', 1004, 'SUP-0004', '上海恒力紧固件有限公司', 'HL-SC-M6X20', '内六角螺丝', 0.45, 'CNY', 5000, 3, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1022, 1024, 'RM-SCREW-01', '内六角螺丝', 1001, 'SUP-0001', '苏州精工金属材料有限公司', 'JG-SCREW-M620', '内六角螺丝', 0.48, 'CNY', 3000, 5, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1023, 1025, 'RM-SCREW-02', '十字盘头螺丝', 1004, 'SUP-0004', '上海恒力紧固件有限公司', 'HL-PT-M6X20', '十字盘头螺丝', 0.38, 'CNY', 5000, 3, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1024, 1041, 'RM-PLASTIC-01', 'ABS外壳注塑件', 1003, 'SUP-0003', '无锡华通塑胶制品有限公司', 'HT-ABS-12080', 'ABS外壳注塑件', 4.2, 'CNY', 500, 12, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1025, 1041, 'RM-PLASTIC-01', 'ABS外壳注塑件', 1008, 'SUP-0008', '宁波市精诚模具制造有限公司', 'JC-IM-ABS-01', 'ABS外壳注塑件', 3.9, 'CNY', 1000, 20, 'N', 2, '2026-01-01', null, 'Y', '0', '模具厂可顺带做注塑', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1026, 1042, 'RM-PLASTIC-02', '尼龙扎带', 1003, 'SUP-0003', '无锡华通塑胶制品有限公司', 'HT-NC-100', '尼龙扎带', 0.08, 'CNY', 10000, 5, 'Y', 1, '2026-01-01', null, 'Y', '0', '低值易耗', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1027, 1051, 'RM-CARTON-01', '包装纸箱', 1005, 'SUP-0005', '常州市远大包装材料有限公司', 'YD-CT-600500', '包装纸箱', 6.8, 'CNY', 200, 6, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1028, 1052, 'RM-FOAM-01', 'EPE珍珠棉缓冲垫', 1005, 'SUP-0005', '常州市远大包装材料有限公司', 'YD-EPE-10', 'EPE珍珠棉缓冲垫', 1.2, 'CNY', 500, 6, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1029, 1052, 'RM-FOAM-01', 'EPE珍珠棉缓冲垫', 1003, 'SUP-0003', '无锡华通塑胶制品有限公司', 'HT-FOAM-10', 'EPE珍珠棉', 1.35, 'CNY', 500, 10, 'N', 2, '2026-01-01', null, 'Y', '0', '备选', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00'),
(1030, 1053, 'RM-LABEL-01', '不干胶标签', 1005, 'SUP-0005', '常州市远大包装材料有限公司', 'YD-LB-8050', '不干胶标签', 0.15, 'CNY', 2000, 4, 'Y', 1, '2026-01-01', null, 'Y', '0', '', 'admin', '2026-09-02 14:20:00', 'admin', '2026-09-03 16:45:00');

-- ----------------------------
-- 数据核对（可选）
-- ----------------------------
select 'md_unit_measure' t, count(1) c from md_unit_measure where del_flag='0'
union all select 'md_item_type',    count(1) from md_item_type where del_flag='0'
union all select 'md_item',         count(1) from md_item where del_flag='0'
union all select 'md_product_bom',  count(1) from md_product_bom where del_flag='0'
union all select 'md_item_batch_config', count(1) from md_item_batch_config where del_flag='0'
union all select 'md_vendor',       count(1) from md_vendor where del_flag='0'
union all select 'md_client',       count(1) from md_client where del_flag='0'
union all select 'md_workshop',     count(1) from md_workshop where del_flag='0'
union all select 'md_workstation',  count(1) from md_workstation where del_flag='0'
union all select 'md_workstation_machine', count(1) from md_workstation_machine where del_flag='0'
union all select 'md_workstation_worker',  count(1) from md_workstation_worker where del_flag='0'
union all select 'md_workstation_tool',    count(1) from md_workstation_tool where del_flag='0'
union all select 'md_product_sop',  count(1) from md_product_sop where del_flag='0'
union all select 'md_product_sip',  count(1) from md_product_sip where del_flag='0'
union all select 'md_item_substitute', count(1) from md_item_substitute where del_flag='0'
union all select 'md_item_vendor',  count(1) from md_item_vendor where del_flag='0';
