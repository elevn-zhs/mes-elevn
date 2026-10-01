-- ============================================================
-- MES 数据库初始化 ——【必须第一个执行】
--
-- 说明：同目录下的 mes-*.sql 只含 create table / insert，
--       没有 create database 和 use，直接执行会报
--       "ERROR 1046 (3D000): No database selected"
--       所以先跑本文件建库并切换。
--
-- 库名与字符集可按实际环境修改（改 DB 名需同步改 _run_all.bat）。
--
-- 默认采用「安全模式」：库已存在则跳过创建，不会误删数据。
-- 若要彻底重建整个库，把下面 drop 语句前面的注释去掉即可
-- （注意：会清空库内所有表和数据，不可恢复）。
-- ============================================================

set names utf8mb4;

-- drop database if exists mes;

create database if not exists mes_elevn
  default character set utf8mb4
  collate utf8mb4_general_ci;

use mes_elevn;

select '>>> database ready, run mes-*.sql next' as msg;
