-- ============================================================
-- 报工冲销改造：加「冲销留痕」与「消耗→报工」关联字段
-- 幂等，可重复执行
-- ============================================================
-- 为什么必须加这两组字段：
--   1) 冲销不删行，只把报工状态置为 REVERSED。既然不删，就必须能查出
--      「谁、在什么时候、为什么」冲的 —— 否则这笔账事后无从追责。
--   2) 报工时会自动倒冲生成物料消耗。冲销要把这些消耗一并回退，
--      如果消耗记录不挂回来源报工，回退就只能按「任务 + 时间」去猜，
--      同工序分批报工的场景下必然误杀别人的消耗。
-- ============================================================
--
-- 用法：mysql -uroot -proot --default-character-set=utf8mb4 mes_elevn < _alter_reverse_20260923.sql
--
-- ============================================================

drop procedure if exists _mig_feedback_reverse;
delimiter $$
create procedure _mig_feedback_reverse()
begin
  -- ---- 1. 报工表：冲销留痕三件套 ----
  if not exists (select 1 from information_schema.columns
                 where table_schema = database()
                   and table_name = 'pro_feedback'
                   and column_name = 'reverse_reason') then
    alter table pro_feedback
      add column reverse_reason varchar(500) default '' comment '冲销原因（冲销必填，留痕）' after status,
      add column reverse_by     varchar(64)  default '' comment '冲销人' after reverse_reason,
      add column reverse_time   datetime              comment '冲销时间' after reverse_by;
  end if;

  -- ---- 2. 消耗表：挂回来源报工 ----
  if not exists (select 1 from information_schema.columns
                 where table_schema = database()
                   and table_name = 'pro_trans_consume'
                   and column_name = 'feedback_id') then
    alter table pro_trans_consume
      add column feedback_id bigint(20) comment '来源报工ID（倒冲自动生成时回填，冲销时据此精确回退）' after task_id;
  end if;

  -- ---- 3. 报工状态默认值纠正（原脚本写的是 PREPARE，那是工单的状态） ----
  alter table pro_feedback
    modify column status varchar(64) default 'SUBMITTED'
    comment '状态 SUBMITTED-已报工 CONFIRMED-已确认 REVERSED-已冲销';
end$$
delimiter ;

call _mig_feedback_reverse();
drop procedure if exists _mig_feedback_reverse;

-- ============================================================
-- 核对
-- ============================================================
select '--- pro_feedback 冲销字段 ---' as section;
select column_name, column_type, column_default, column_comment
from information_schema.columns
where table_schema = database() and table_name = 'pro_feedback'
  and column_name in ('status', 'reverse_reason', 'reverse_by', 'reverse_time')
order by ordinal_position;

select '--- pro_trans_consume 关联字段 ---' as section;
select column_name, column_type, column_comment
from information_schema.columns
where table_schema = database() and table_name = 'pro_trans_consume'
  and column_name in ('task_id', 'feedback_id')
order by ordinal_position;
