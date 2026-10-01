-- ============================================================
-- MES【仓储管理】字典数据 + 单据编码规则
--
-- 执行：mysql -uroot -p mes_elevn < docs/sql/mes-wm-dict.sql
-- 幂等：先按 dict_type / rule_code 删干净再插，可重复执行
--
-- 说明：
--   1) 七类字典全部新增（sys_dict_type 里原本一条 wm 都没有）：出入库单据类型 /
--      出入库标志 / 出入库单据状态 / 仓储通知单状态 / 仓储通知类型 / 库区库位类型 / 批次质量状态；
--   2) 单据编码规则写进 sys_coding_rules，14 类单据各一条，
--      前缀用拼音缩写（中文环境下比 PR/MI 这种英文缩写更好认）；
--   3) 不写死 dict_id 主键，交给 auto_increment，避免与已有数据冲突；
--   4) dict_sort 从 21 起排（生产模块用到 20）；
--   5) 【重要】wm_doc_type 的 dict_value 就是表里存的 doc_type 原值，
--      dict_label 是页面显示的中文名，两者不要混。
-- ============================================================

set names utf8mb4;
use mes_elevn;

-- ----------------------------
-- 一、字典类型
-- ----------------------------
delete from sys_dict_type where dict_type in
  ('wm_doc_type', 'wm_io_flag', 'wm_doc_status', 'wm_notice_status',
   'wm_notice_type', 'wm_location_type', 'wm_batch_quality_status');

insert into sys_dict_type
  (dict_name, dict_type, dict_sort, status, del_flag, create_by, create_time, remark)
values
  ('出入库单据类型', 'wm_doc_type', 21, '0', '0', 'admin', now(),
   '14 类出入库单据合并为 wm_doc 一张表，用 doc_type 区分'),
  ('出入库标志',   'wm_io_flag',            22, '0', '0', 'admin', now(),
   '决定库存加减方向：I 入库(+) / O 出库(-) / T 调拨(双向)'),
  ('出入库单据状态', 'wm_doc_status',       23, '0', '0', 'admin', now(),
   '单据流转状态：待过账 / 已过账 / 已完成 / 已取消'),
  ('仓储通知类型', 'wm_notice_type',        24, '0', '0', 'admin', now(),
   '3 类通知单合并为 wm_notice，用 notice_type 区分'),
  ('仓储通知单状态', 'wm_notice_status',    27, '0', '0', 'admin', now(),
   '通知单流转状态：待处理 / 已触发检验 / 已完成 / 已取消（取值与单据状态同名但语义不同，故单独建一类）'),
  ('库区库位类型', 'wm_location_type',      25, '0', '0', 'admin', now(),
   'wm_location 自关联树：AREA 库区为父级，LOCATION 库位为子级'),
  ('批次质量状态', 'wm_batch_quality_status', 26, '0', '0', 'admin', now(),
   '批次的质量状态，由 C 线检验结果回写');

-- ----------------------------
-- 二、字典数据
-- ----------------------------
-- 先清掉这几类的旧数据，保证可重复执行
delete from sys_dict_data where dict_type in
  ('wm_doc_type', 'wm_io_flag', 'wm_doc_status', 'wm_notice_status',
   'wm_notice_type', 'wm_location_type', 'wm_batch_quality_status');

-- 出入库标志：I 入库 / O 出库 / T 调拨
-- 【口径】io_flag 决定库存加减方向，是过账时唯一的方向依据：
--   I → wm_material_stock.quantity_onhand 加、wm_transaction.transaction_flag = 1
--   O → 减、transaction_flag = -1
--   T → 源仓库减一条 + 目标仓库加一条（配对）
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_io_flag', '入库', 'I', 1, 'N', 'primary', '', '0', '0', 'admin', now(),
   '库存增加，如采购入库、生产入库'),
  ('wm_io_flag', '出库', 'O', 2, 'N', 'warning', '', '0', '0', 'admin', now(),
   '库存减少，如生产领料、销售出库'),
  ('wm_io_flag', '调拨', 'T', 3, 'N', 'info',    '', '0', '0', 'admin', now(),
   '库间转移，一出一进配对，总数不变');

-- 出入库单据类型：14 类
-- 【口径】doc_type 是单据的业务身份，io_flag 由它推导而来，页面上不给用户手选
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_doc_type', '采购入库单',   'ITEM_RECPT',      1,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：供应商送货到厂后验收入库，来源到货通知单'),
  ('wm_doc_type', '产品入库单',   'PRODUCT_RECPT',   2,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：产品完工后入成品库，来源生产工单'),
  ('wm_doc_type', '生产入库单',   'PRODUCT_PRODUCE', 3,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：生产完工直接入库，来源生产任务'),
  ('wm_doc_type', '生产退料单',   'RT_ISSUE',        4,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：产线没用完的料退回仓库，来源生产领料单'),
  ('wm_doc_type', '销售退货单',   'RT_SALES',        5,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：客户退货回厂，需做退料检验(RQC)后决定能否重新入库'),
  ('wm_doc_type', '外协入库单',   'OUTSOURCE_RECPT', 6,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：外协厂加工完成后收回'),
  ('wm_doc_type', '杂项入库单',   'MISC_RECPT',      7,  'N', 'primary', '', '0', '0', 'admin', now(),
   'I 入库：盘盈、借用归还等无来源单据的入库'),
  ('wm_doc_type', '供应商退货单', 'RT_VENDOR',       8,  'N', 'warning', '', '0', '0', 'admin', now(),
   'O 出库：来料检验不合格退回供应商'),
  ('wm_doc_type', '生产领料单',   'ISSUE',           9,  'N', 'warning', '', '0', '0', 'admin', now(),
   'O 出库：按工单从仓库领料到产线，过账后写 pro_task_issue 投料'),
  ('wm_doc_type', '物料消耗单',   'ITEM_CONSUME',    10, 'N', 'warning', '', '0', '0', 'admin', now(),
   'O 出库：产线实际消耗，通常由报工倒冲自动生成'),
  ('wm_doc_type', '销售出库单',   'PRODUCT_SALES',   11, 'N', 'warning', '', '0', '0', 'admin', now(),
   'O 出库：成品发货给客户，需做出货检验(OQC)后放行'),
  ('wm_doc_type', '外协出库单',   'OUTSOURCE_ISSUE', 12, 'N', 'warning', '', '0', '0', 'admin', now(),
   'O 出库：发料给外协厂加工'),
  ('wm_doc_type', '杂项出库单',   'MISC_ISSUE',      13, 'N', 'warning', '', '0', '0', 'admin', now(),
   'O 出库：盘亏、报废等无来源单据的出库'),
  ('wm_doc_type', '调拨单',       'TRANSFER',        14, 'N', 'info',    '', '0', '0', 'admin', now(),
   'T 调拨：仓库之间转移物料，源仓减、目标仓加，总数不变');

-- 单据状态：PREPARE 为默认值（与 wm_doc.status 的 DEFAULT 'PREPARE' 一致）
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_doc_status', '待过账', 'PREPARE',   1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '草稿状态，可以改表头、加行、删行，也可以直接删除整单'),
  ('wm_doc_status', '已过账', 'CONFIRMED', 2, 'N', 'primary', '', '0', '0', 'admin', now(),
   '库存已经变动，不能再改数量和明细 —— 库存动过就回不了头'),
  ('wm_doc_status', '已完成', 'FINISHED',  3, 'N', 'success', '', '0', '0', 'admin', now(),
   '调拨单送达确认(confirm_flag=Y)后置为此状态'),
  ('wm_doc_status', '已取消', 'CANCELED',  4, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '单据作废，不再参与库存统计');

-- 通知单状态：PREPARE 为默认值（与 wm_notice.status 的 DEFAULT 'PREPARE' 一致）
-- 【为什么单独建一类而不复用 wm_doc_status】
--   取值虽然一样，但语义不同：单据是"待过账/已过账"（库存动没动），
--   通知单是"待处理/已触发检验"（报检了没有）。混用会把页面上的提示写歪。
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_notice_status', '待处理',     'PREPARE',   1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '通知单草稿，可以改表头、加行、删行，也可以直接删除整单'),
  ('wm_notice_status', '已触发检验', 'CONFIRMED', 2, 'N', 'primary', '', '0', '0', 'admin', now(),
   '已经报检（到货→IQC / 发货→OQC），不能再改也不能再删'),
  ('wm_notice_status', '已完成',     'FINISHED',  3, 'N', 'success', '', '0', '0', 'admin', now(),
   '通知对应的业务已经落地（货到齐 / 货发完 / 料备好）'),
  ('wm_notice_status', '已取消',     'CANCELED',  4, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '通知作废，不再参与后续单据的下推');

-- 仓储通知类型：3 类
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_notice_type', '到货通知单',   'ARRIVAL',          1, 'N', 'primary', '', '0', '0', 'admin', now(),
   '供应商送货前的预告，到货后触发 C 线来料检验(IQC)'),
  ('wm_notice_type', '发货通知单',   'SALES',            2, 'N', 'success', '', '0', '0', 'admin', now(),
   '客户要货的预告，发货前触发 C 线出货检验(OQC)'),
  ('wm_notice_type', '备料申请单',   'MATERIAL_REQUEST', 3, 'N', 'warning', '', '0', '0', 'admin', now(),
   '产线按工单申请备料，是生产领料单的上游来源');

-- 库区库位类型：AREA / LOCATION
-- 【易错点】旧版命名里 wm_storage_location 是库区、wm_storage_area 是库位，
--           跟直觉正好相反；本版已纠正为 AREA(大) / LOCATION(小)
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_location_type', '库区', 'AREA',     1, 'N', 'primary', '', '0', '0', 'admin', now(),
   '库区：父级节点，parent_id = 0，挂在仓库下，本身不放货'),
  ('wm_location_type', '库位', 'LOCATION', 2, 'N', 'success', '', '0', '0', 'admin', now(),
   '库位：子级节点，parent_id 指向所属库区，货物实际存放的地方');

-- 批次质量状态
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_batch_quality_status', '待检',   'PENDING',     1, 'Y', 'warning', '', '0', '0', 'admin', now(),
   '刚生成还没检验，默认状态'),
  ('wm_batch_quality_status', '合格',   'QUALIFIED',   2, 'N', 'success', '', '0', '0', 'admin', now(),
   '检验判定合格，可以正常领用与出库'),
  ('wm_batch_quality_status', '不合格', 'UNQUALIFIED', 3, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '检验判定不合格，禁止出库'),
  ('wm_batch_quality_status', '冻结',   'FROZEN',      4, 'N', 'info',    '', '0', '0', 'admin', now(),
   '因质量问题被冻结，需解冻后才能动');

-- ----------------------------
-- 二·五、S3 模块字典（装箱 / 盘点 / 条码 / SN）
-- ----------------------------

-- 装箱状态：wm_package.status 的 DEFAULT 'PREPARE'
-- 【为什么只有两态】装箱不像出入库那样动库存，它的关键节点只有
--   "还在装（可以加行换行）"和"装完了（明细锁定，等着贴码发货）"。
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_package_status', '装箱中', 'PREPARE', 1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '箱子还在装，可以加明细行、编辑箱信息，也可以直接删除'),
  ('wm_package_status', '已完成', 'PACKED',  2, 'N', 'success', '', '0', '0', 'admin', now(),
   '装箱完成，明细行锁定；后续发货/贴条码都以此为准');

-- 盘点单状态：wm_stock_taking.status / wm_stock_taking_plan.status 共用
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_taking_status', '待录入', 'PREPARE',   1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '盘点单已生成，实盘数量还没录或没录完'),
  ('wm_taking_status', '已过账', 'CONFIRMED', 2, 'N', 'primary', '', '0', '0', 'admin', now(),
   '盈亏差异已写入库存与事务流水，不能再改实盘数'),
  ('wm_taking_status', '已取消', 'CANCELED',  3, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '盘点作废，差异不生效');

-- 盘点类型：wm_stock_taking_plan.taking_type
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_taking_type', '全盘',   'FULL', 1, 'N', 'primary', '', '0', '0', 'admin', now(),
   '所有仓库的所有库存都盘'),
  ('wm_taking_type', '部分盘点', 'PART', 2, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '按范围（仓库/库区/物料分类）圈定盘点对象，最常用');

-- 盘点范围类型：wm_stock_taking_scope.scope_type
-- 【展开口径】generate 时把范围翻译成库存行：
--   WAREHOUSE -> 该仓下全部库存行；AREA -> 该库区下所有库位的库存行；
--   ITEM_TYPE -> 该分类下所有物料的库存行。三者可混选，并集去重。
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_scope_type', '按仓库',     'WAREHOUSE', 1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   'scope_value_* 存仓库三件套'),
  ('wm_scope_type', '按库区',     'AREA',      2, 'N', 'info',    '', '0', '0', 'admin', now(),
   'scope_value_* 存库区三件套'),
  ('wm_scope_type', '按物料分类', 'ITEM_TYPE', 3, 'N', 'warning', '', '0', '0', 'admin', now(),
   'scope_value_* 存物料分类三件套');

-- 条码类型：wm_barcode.barcode_type
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_barcode_type', '物料码', 'ITEM',    1, 'N', 'primary', '', '0', '0', 'admin', now(),
   '标识一种物料，贴在物料包装/货架上'),
  ('wm_barcode_type', '批次码', 'BATCH',   2, 'N', 'success', '', '0', '0', 'admin', now(),
   '标识一个生产批次，是批次追溯的扫码入口'),
  ('wm_barcode_type', '箱码',   'PACKAGE', 3, 'N', 'warning', '', '0', '0', 'admin', now(),
   '标识一个包装箱，整箱出入库时扫它');

-- SN 状态：wm_sn.status
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('wm_sn_status', '在库',   'IN_STOCK', 1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   'SN 已生成且对应实物还在仓库/产线'),
  ('wm_sn_status', '已发货', 'SHIPPED',  2, 'N', 'success', '', '0', '0', 'admin', now(),
   'SN 对应的实物已出库发给客户'),
  ('wm_sn_status', '冻结',   'FROZEN',   3, 'N', 'info',    '', '0', '0', 'admin', now(),
   '因质量或维修原因暂停流转');

-- ----------------------------
-- 三、单据编号规则（sys_coding_rules）
-- ----------------------------
-- 14 类单据各配一条。编号组成 = 前缀 + 日期(日期字符串开关打开时) + 序列号补齐
-- 例：CGRK20260923 0001
--
-- 【为什么不用 wm_doc 的自增主键当编号】
-- 主键是内部标识，编号是对外凭证号。单据作废后编号不能回收，所以必须独立取号。
delete from sys_coding_rules where rule_code in
  ('WM_ITEM_RECPT', 'WM_PRODUCT_RECPT', 'WM_PRODUCT_PRODUCE', 'WM_RT_ISSUE',
   'WM_RT_SALES', 'WM_OUTSOURCE_RECPT', 'WM_MISC_RECPT', 'WM_RT_VENDOR',
   'WM_ISSUE', 'WM_ITEM_CONSUME', 'WM_PRODUCT_SALES', 'WM_OUTSOURCE_ISSUE',
   'WM_MISC_ISSUE', 'WM_TRANSFER',
   -- 通知单（3 类）也有自己的编号规则
   'WM_NOTICE_ARRIVAL', 'WM_NOTICE_SALES', 'WM_NOTICE_MATERIAL_REQUEST',
   -- S3：装箱 / 盘点 / SN
   'WM_PACKAGE', 'WM_TAKING_PLAN', 'WM_STOCK_TAKING', 'WM_SN');

insert into sys_coding_rules
  (rule_title, rule_code, rule_desc, prefix, date_str, serial_number, number_length,
   creat_time, create_by, update_time, update_by, status, del_flag)
values
  ('采购入库单编号',   'WM_ITEM_RECPT',      '入库类：供应商送货验收入库', 'CGRK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('产品入库单编号',   'WM_PRODUCT_RECPT',   '入库类：产品完工入成品库',   'CPRK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('生产入库单编号',   'WM_PRODUCT_PRODUCE', '入库类：生产完工直接入库',   'SCRK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('生产退料单编号',   'WM_RT_ISSUE',        '入库类：产线余料退回仓库',   'STLK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('销售退货单编号',   'WM_RT_SALES',        '入库类：客户退货回厂',       'XSTH',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('外协入库单编号',   'WM_OUTSOURCE_RECPT', '入库类：外协加工收回',       'WXRK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('杂项入库单编号',   'WM_MISC_RECPT',      '入库类：盘盈等无来源入库',   'ZXRK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('供应商退货单编号', 'WM_RT_VENDOR',       '出库类：不合格品退回供应商', 'GYSTH', 1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('生产领料单编号',   'WM_ISSUE',           '出库类：按工单领料到产线',   'SCLL',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('物料消耗单编号',   'WM_ITEM_CONSUME',    '出库类：产线实际消耗',       'WLXH',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('销售出库单编号',   'WM_PRODUCT_SALES',   '出库类：成品发货给客户',     'XSCK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('外协出库单编号',   'WM_OUTSOURCE_ISSUE', '出库类：发料给外协厂',       'WXCK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('杂项出库单编号',   'WM_MISC_ISSUE',      '出库类：盘亏报废等',         'ZXCK',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('调拨单编号',       'WM_TRANSFER',        '调拨类：仓库之间转移物料',   'DBD',   1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  -- 通知单不像出入库单据那样会改库存，它只是预告 + 触发检验，
  -- 但它的编号也要对外可见（供应商/客户/产线都看得到），所以同样独立取号
  ('到货通知单编号',   'WM_NOTICE_ARRIVAL',  '通知类：供应商送货前预告，触发 IQC', 'DH', 1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('发货通知单编号',   'WM_NOTICE_SALES',    '通知类：客户要货预告，触发 OQC',     'FH', 1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('备料申请单编号',   'WM_NOTICE_MATERIAL_REQUEST', '通知类：产线按工单申请备料，是生产领料单的上游', 'BL', 1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  -- S3：装箱单要贴在箱子上对外流转；盘点单/盘点计划是对外凭证；SN 打印在产品铭牌上
  ('装箱单编号',       'WM_PACKAGE',      'S3装箱：一个箱子一张编号，箱套箱时父子箱各有编号', 'ZX',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('盘点计划编号',     'WM_TAKING_PLAN',  'S3盘点：计划的编号，生成盘点单时计划号写入盘点单溯源', 'PDJH', 1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('盘点单编号',       'WM_STOCK_TAKING', 'S3盘点：实际执行盈亏过账的凭证编号', 'PDD',  1, 0, 6, now(), 'admin', now(), 'admin', 1, 0),
  ('SN 序列号前缀',    'WM_SN',           'S3 SN：一物一码，SN+日期+流水，终身不复用', 'SN',   1, 0, 8, now(), 'admin', now(), 'admin', 1, 0);

-- ----------------------------
-- 校验：跑完可以执行下面两句看一眼结果
-- ----------------------------
-- select dict_type, count(*) from sys_dict_data
--   where dict_type like 'wm\_%' group by dict_type;
-- select rule_code, rule_title, prefix from sys_coding_rules where rule_code like 'WM\_%';
