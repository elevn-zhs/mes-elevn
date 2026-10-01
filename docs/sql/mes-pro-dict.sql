-- ============================================================
-- MES【生产工单】字典数据
-- 补充工单类型 / 来源类型 / 工单状态 / 生产任务状态 / 报工三类 字典
--
-- 执行：mysql -uroot -p mes_elevn < docs/sql/mes-pro-dict.sql
-- 幂等：先按 dict_type 删干净再插，可重复执行
--
-- 说明：
--   1) production_order_status 字典类型原来就存在（dict_id=102），
--      但 status=1 是停用的，且一条数据都没有 —— 这里一并修正为启用并补数据；
--   2) 各字典的 dict_type 命名沿用系统既有风格（小写下划线）；
--   3) 不写死 dict_code 主键，交给 auto_increment，避免与已有数据冲突；
--   4) pro_task_status 是为排产新增的（pro_task.status 默认值就是 NORMAL，
--      与字典默认值保持一致）；
--   5) feedback_type / feedback_channel / pro_feedback_status 是为报工新增的。
-- ============================================================

set names utf8mb4;
use mes_elevn;

-- ----------------------------
-- 一、字典类型
-- ----------------------------
-- production_order_status 已存在，改为启用
update sys_dict_type
set dict_name = '工单流转状态', status = '0', del_flag = '0',
    update_by = 'admin', update_time = now()
where dict_type = 'production_order_status';

-- 其余六类新增（幂等：先删后插）
delete from sys_dict_type where dict_type in
  ('workorder_type', 'order_source', 'pro_task_status',
   'feedback_type', 'feedback_channel', 'pro_feedback_status',
   'pro_card_status', 'pro_consume_source');

insert into sys_dict_type
  (dict_name, dict_type, dict_sort, status, del_flag, create_by, create_time, remark)
values
  ('工单类型', 'workorder_type', 13, '0', '0', 'admin', now(), '生产工单类型：自产/外协/外购'),
  ('工单来源类型', 'order_source', 14, '0', '0', 'admin', now(), '生产工单来源：客户订单/库存备货'),
  ('生产任务状态', 'pro_task_status', 15, '0', '0', 'admin', now(), '排产拆出的工序任务状态：待生产/生产中/已完工/已取消'),
  ('报工类型', 'feedback_type', 16, '0', '0', 'admin', now(), '生产报工类型：首件报工/过程报工/完工报工'),
  ('报工途径', 'feedback_channel', 17, '0', '0', 'admin', now(), '报工的录入方式：PC端/扫码/终端机'),
  ('报工状态', 'pro_feedback_status', 18, '0', '0', 'admin', now(), '报工单状态：已报工/已确认/已冲销'),
  ('流转卡状态', 'pro_card_status', 19, '0', '0', 'admin', now(), '工序流转卡状态：待流转/流转中/已完工/已取消'),
  ('消耗来源', 'pro_consume_source', 20, '0', '0', 'admin', now(), '物料消耗记录来源：倒冲/生产领料/备料申请/手工补录');

-- ----------------------------
-- 二、字典数据
-- ----------------------------
-- 先清掉这几类的旧数据，保证可重复执行
delete from sys_dict_data where dict_type in
  ('workorder_type', 'order_source', 'production_order_status', 'pro_task_status',
   'feedback_type', 'feedback_channel', 'pro_feedback_status', 'pro_card_status',
   'pro_consume_source');

-- 工单类型：SELF 为默认值（与 pro_workorder.workorder_type 的 DEFAULT 'SELF' 一致）
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('workorder_type', '自产', 'SELF',      1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   '本厂生产，需要排产，按产品制程展开工序任务'),
  ('workorder_type', '外协', 'OUTSOURCE', 2, 'N', 'warning', '', '0', '0', 'admin', now(),
   '发给外协厂加工，需要指定供应商，不需要排产'),
  ('workorder_type', '外购', 'PURCHASE',  3, 'N', 'info',    '', '0', '0', 'admin', now(),
   '成品直接采购回来，需要指定供应商，不需要排产');

-- 来源类型：ORDER 为默认值
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('order_source', '客户订单', 'ORDER', 1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   '来源于客户订单，必须填写订单编号并选择客户'),
  ('order_source', '库存备货', 'STORE', 2, 'N', 'success', '', '0', '0', 'admin', now(),
   '为库存备货而生产，不需要填订单编号与客户');

-- 工单状态：PREPARE 为默认值（与 pro_workorder.status 的 DEFAULT 'PREPARE' 一致）
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('production_order_status', '待下达', 'PREPARE',   1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '草稿状态，可编辑可删除'),
  ('production_order_status', '已下达', 'CONFIRMED', 2, 'N', 'primary', '', '0', '0', 'admin', now(),
   '已下达，不再允许修改产品与数量，可进行排产'),
  ('production_order_status', '已完工', 'FINISHED',  3, 'N', 'success', '', '0', '0', 'admin', now(),
   '生产完成，记录完成时间'),
  ('production_order_status', '已取消', 'CANCELED',  4, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '单据取消，记录取消日期');

-- 生产任务状态：NORMAL 为默认值（与 pro_task.status 的 DEFAULT 'NORMAL' 一致）
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('pro_task_status', '待生产', 'NORMAL',   1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '已排产但还没开工'),
  ('pro_task_status', '生产中', 'WORKING',  2, 'N', 'primary', '', '0', '0', 'admin', now(),
   '已经开工，报工中'),
  ('pro_task_status', '已完工', 'FINISHED', 3, 'N', 'success', '', '0', '0', 'admin', now(),
   '工序任务完成，记录完成时间'),
  ('pro_task_status', '已取消', 'CANCELED', 4, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '任务取消，不再参与进度统计');

-- 报工类型：PROCESS 为默认值（与 pro_feedback.feedback_type 的 DEFAULT 'PROCESS' 一致）
-- 业务口径：首件报工用于首检放行，过程报工是日常批量报工，完工报工是末件收尾
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('feedback_type', '首件报工', 'FIRST',   1, 'N', 'warning', '', '0', '0', 'admin', now(),
   '每批开工的第一件，报工后需做首检（IPQC）才能批量生产'),
  ('feedback_type', '过程报工', 'PROCESS', 2, 'Y', 'primary', '', '0', '0', 'admin', now(),
   '生产过程中的常规报工，最常用'),
  ('feedback_type', '完工报工', 'FINISH',  3, 'N', 'success', '', '0', '0', 'admin', now(),
   '该工序最后一批报工，报完任务可完工');

-- 报工途径：PC 为默认值
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('feedback_channel', 'PC端',   'PC',     1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   '在系统页面录入，用于手工补录与管理员报工'),
  ('feedback_channel', '扫码报工', 'SCAN',  2, 'N', 'success', '', '0', '0', 'admin', now(),
   '扫工单/任务条码快速报工，车间现场主用方式'),
  ('feedback_channel', '车间终端', 'TERMINAL', 3, 'N', 'info',  '', '0', '0', 'admin', now(),
   '工作站终端机上报，无需登录 PC');

-- 报工状态：这条报工单本身的状态（与工单/任务状态不是一回事）
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('pro_feedback_status', '已报工', 'SUBMITTED', 1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   '已提交，数量已计入任务与工单进度'),
  ('pro_feedback_status', '已确认', 'CONFIRMED', 2, 'N', 'success', '', '0', '0', 'admin', now(),
   '班组长已审核确认，可参与绩效统计'),
  ('pro_feedback_status', '已冲销', 'REVERSED',  3, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '报错后冲销，数量从任务与工单进度中扣回');

-- 流转卡状态：卡跟着工单走，一工单一卡
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('pro_card_status', '待流转', 'PENDING',  1, 'Y', 'info',    '', '0', '0', 'admin', now(),
   '排产建卡后还没人开工'),
  ('pro_card_status', '流转中', 'RUNNING',  2, 'N', 'primary', '', '0', '0', 'admin', now(),
   '首道工序已有产出，卡在工序间按序推进'),
  ('pro_card_status', '已完工', 'FINISHED', 3, 'N', 'success', '', '0', '0', 'admin', now(),
   '末道工序产出达到流转数量'),
  ('pro_card_status', '已取消', 'CANCELED', 4, 'N', 'danger',  '', '0', '0', 'admin', now(),
   '工单取消时一并取消');

-- 消耗来源：BACKFLUSH 是本模块报工自动倒冲出来的，其余三种等 B 线领料单接入后才有
insert into sys_dict_data
  (dict_type, dict_label, dict_value, dict_sort, is_default, list_class, css_class,
   status, del_flag, create_by, create_time, remark)
values
  ('pro_consume_source', '倒冲消耗', 'BACKFLUSH',        1, 'Y', 'primary', '', '0', '0', 'admin', now(),
   '报工时按产出数量×制程BOM单位用量自动倒推生成'),
  ('pro_consume_source', '生产领料', 'ISSUE',            2, 'N', 'success', '', '0', '0', 'admin', now(),
   '由仓储的领料单过账后核销，B线接入后启用'),
  ('pro_consume_source', '备料申请', 'MATERIAL_REQUEST', 3, 'N', 'info',    '', '0', '0', 'admin', now(),
   '由备料申请单核销，B线接入后启用'),
  ('pro_consume_source', '手工补录', 'MANUAL',           4, 'N', 'warning', '', '0', '0', 'admin', now(),
   '现场漏报工时的人工补录，需在备注写明原因');

-- ----------------------------
-- 三、核对
-- ----------------------------
select '--- 字典类型 ---' as section;
select dict_id, dict_name, dict_type, dict_sort, status
from sys_dict_type
where dict_type in ('workorder_type', 'order_source', 'production_order_status', 'pro_task_status',
                    'feedback_type', 'feedback_channel', 'pro_feedback_status', 'pro_card_status',
                    'pro_consume_source')
order by dict_sort;

select '--- 字典数据 ---' as section;
select dict_code, dict_type, dict_label, dict_value, dict_sort, is_default, list_class, status
from sys_dict_data
where dict_type in ('workorder_type', 'order_source', 'production_order_status', 'pro_task_status',
                    'feedback_type', 'feedback_channel', 'pro_feedback_status', 'pro_card_status',
                    'pro_consume_source')
order by dict_type, dict_sort;
