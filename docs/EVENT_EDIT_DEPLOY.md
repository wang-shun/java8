### 1. 删除无效数据
```sql
-- 删除没有用的公司信息
delete
from doctor_orgs
where id not in
(
select distinct org_id
from doctor_farms
);

-- 删除事件表中猪场已经被删除的数据
delete from doctor_pig_events
where farm_id not in
(
select id from doctor_farms
);

delete from doctor_group_events
where farm_id not in
(
select id from doctor_farms
);

delete from doctor_pigs
where farm_id not in
(
select id from doctor_farms
);

delete from doctor_groups
where farm_id not in
(
select id from doctor_farms
);

delete from doctor_group_tracks
where group_id not in
(
select id from doctor_groups
);

-- 删除猪事件、猪群事件快照表
truncate doctor_group_snapshots;
truncate doctor_pig_snapshots;
-- 清空日报、周报、月报
truncate doctor_daily_reports;
truncate doctor_monthly_reports;
truncate doctor_weekly_reports;
-- 清空消息
truncate doctor_message_user;
truncate doctor_messages;
-- 删除工作流相关的表
drop table if exists workflow_definition_node_events;
drop table if exists workflow_definition_nodes;
drop table if exists workflow_definitions;
drop table if exists workflow_history_process_instances;
drop table if exists workflow_history_processes;
drop table if exists workflow_process_instances;
drop table if exists workflow_process_tracks;
drop table if exists workflow_processes;
```
### 2. 执行db_change

```sql

-- 2017-04-07 猪事件拆分
alter table doctor_pig_events add column source tinyint(4) default null comment '进场来源，1：本场，2：外购' after extra;
alter table doctor_pig_events add column boar_type tinyint(4) default null comment '公猪类型,1：活公猪，2：冷冻精液，3：新鲜精液' after source;
alter table doctor_pig_events add column breed_id bigint(20) default null comment '品种' after source;
alter table doctor_pig_events add column breed_name varchar(32) default null comment '品种' after breed_id;
alter table doctor_pig_events add column breed_type_id bigint(20) default null comment '品系' after breed_name;
alter table doctor_pig_events add column breed_type_name varchar(32) default null comment '品系' after breed_type_id;
alter table doctor_pig_events add column quantity int(11) default null comment '数量(拼窝数量,被拼窝数量,仔猪变动数量)' after amount;
alter table doctor_pig_events add column weight DOUBLE default null comment '重量(变动重量)' after quantity;
alter table doctor_pig_events add column basic_id bigint(20) default null comment '基础数据id(流产原因id,疾病id,防疫项目id)' after change_type_id;
alter table doctor_pig_events add column basic_name varchar(32) default null comment '基础数据名(流产原因,疾病,防疫)' after basic_id;
alter table doctor_pig_events add column customer_id bigint(20) default null comment '客户id' after basic_name;
alter table doctor_pig_events add column customer_name varchar(64) default null comment '客户名' after customer_id;
alter table doctor_pig_events add column vaccination_id bigint(20) default null comment '疫苗' after customer_name;
alter table doctor_pig_events add column vaccination_name varchar(32) default null comment '疫苗名称' after vaccination_id;
alter table doctor_pig_events add column mate_type tinyint(4) default null comment '配种类型(人工、自然)' after doctor_mate_type;
alter table doctor_pig_events add column judge_preg_date date default null comment '预产期' after partwean_date;
alter table doctor_pig_events add column barn_type tinyint(4) default null comment '猪舍类型' after barn_name;

-- 2017-04-07 猪群拆分
ALTER TABLE doctor_group_events ADD COLUMN sow_id bigint(20) DEFAULT NULL comment '有母猪触发的事件关联的猪id' after barn_name;
ALTER TABLE doctor_group_events ADD COLUMN sow_code varchar(32) DEFAULT NULL comment '有母猪触发的事件关联的猪code' after sow_id;
ALTER TABLE doctor_group_events ADD COLUMN customer_id bigint(20) DEFAULT NULL comment '销售时客户id' after over_price;
ALTER TABLE doctor_group_events ADD COLUMN customer_name varchar(64) DEFAULT NULL comment '销售时客户名' after customer_id;
ALTER TABLE doctor_group_events ADD COLUMN basic_id bigint(20) DEFAULT NULL comment '基础数据id(疾病id,防疫项目id)' after customer_name;
ALTER TABLE doctor_group_events ADD COLUMN basic_name varchar(32) DEFAULT NULL comment '基础数据名(疾病,防疫)' after basic_id;
ALTER TABLE doctor_group_events ADD COLUMN vaccin_result tinyint(4) DEFAULT NULL comment '防疫结果' after basic_name;
alter table doctor_group_events add column vaccination_id bigint(20) default null comment '疫苗' after vaccin_result;
alter table doctor_group_events add column vaccination_name varchar(32) default null comment '疫苗名称' after vaccination_id;
ALTER TABLE doctor_group_events ADD COLUMN operator_id bigint(20) DEFAULT NULL comment '操作人id' after creator_name;
ALTER TABLE doctor_group_events ADD COLUMN operator_name varchar(32) DEFAULT NULL comment '操作人姓名' after operator_id;

create index idx_doctor_pig_events_barn_id  on doctor_pig_events(`barn_id`);
create index idx_doctor_pig_events_event_at  on doctor_pig_events(`event_at`);
create index idx_doctor_group_events_event_at  on doctor_group_events(`event_at`);

-- 2017-04-06 编辑事件记录
drop table if exists `doctor_event_modify_logs`;
CREATE TABLE `doctor_event_modify_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) NOT NULL COMMENT '猪场ID',
  `business_id` bigint(20) NOT NULL COMMENT '猪/猪群id',
  `business_code` varchar(64) NOT NULL COMMENT '猪/猪群code',
  `type` tinyint(4) NOT NULL COMMENT '处理的事件类型1：猪事件，2：猪群事件',
  `modify_request_id` bigint(20) DEFAULT NULL COMMENT '编辑申请id',
  `delete_event` text DEFAULT NULL COMMENT '删除事件',
  `from_event` text DEFAULT NULL COMMENT '修改前事件',
  `to_event` text DEFAULT NULL COMMENT '修改后事件',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新事件',
  PRIMARY KEY (`id`),
  KEY `doctor_event_modify_logs_farm_id` (`farm_id`)
) DEFAULT CHARSET=utf8;


-- 2017-04-18 销售视图
create or replace view
v_doctor_sales
AS
select
null as
batch_no,
farm_id,
farm_name,
5 as pig_type,
"种猪" as pig_type_name,
null as day_age,
barn_id,
barn_name,
date_format(event_at, '%Y-%m-%d') as event_at,
quantity,
weight,
price,
amount,
customer_id,
customer_name
from doctor_pig_events
where type = 6
and change_type_id = 109
group by farm_id, farm_name, barn_id, barn_name, date_format(event_at, '%Y-%m-%d'), customer_id, customer_name,day_age

union all
select
group_code as batch_no,
farm_id,
farm_name,
pig_type,
"育肥猪" as pig_type_name,
avg_day_age as day_age,
barn_id,
barn_name,
date_format(event_at, '%Y-%m-%d') as event_at,
quantity,
weight,
price,
amount,
customer_id,
customer_name
from doctor_group_events
where type = 3
and pig_type = 3
and change_type_id = 109
group by  group_code,farm_id, farm_name, barn_id, barn_name, date_format(event_at, '%Y-%m-%d'), customer_id, customer_name,day_age

union all
select
group_code as batch_no,
farm_id,
farm_name,
pig_type,
"后备猪" as pig_type_name,
avg_day_age as day_age,
barn_id,
barn_name,
date_format(event_at, '%Y-%m-%d') as event_at,
quantity,
weight,
price,
amount,
customer_id,
customer_name
from doctor_group_events
where type = 3
and pig_type = 4
and change_type_id = 109
group by group_code, farm_id, farm_name, barn_id, barn_name, date_format(event_at, '%Y-%m-%d'), customer_id, customer_name,day_age

union all
select
group_code as batch_no,
farm_id,
farm_name,
'7_2' as pig_type,
"仔猪" as pig_type_name,
avg_day_age as day_age,
barn_id,
barn_name,
date_format(event_at, '%Y-%m-%d') as event_at,
sum(quantity) as quantity,
sum(weight) as weight,
sum(amount)/sum(quantity) as price,
sum(amount) as amount,
customer_id,
customer_name
from doctor_group_events
where type = 3
and pig_type in ( 2,7 )
and change_type_id = 109
group by group_code , farm_id, farm_name, barn_id, barn_name, date_format(event_at, '%Y-%m-%d'), customer_id, customer_name,day_age;

-- 新的周报和月报指标表
drop table if exists `doctor_range_reports`;
CREATE TABLE `doctor_range_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) NOT NULL COMMENT '猪场id',
  `type` tinyint(4) DEFAULT NULL COMMENT '类型，1月报，2周报',
  `sum_at` varchar(10) DEFAULT NULL COMMENT '统计时间',
  `sum_from` date DEFAULT NULL COMMENT '开始时间',
  `sum_to` date DEFAULT NULL COMMENT '结算时间',
  `mate_estimate_preg_rate` double DEFAULT NULL COMMENT '估算受胎率',
  `mate_real_preg_rate` double DEFAULT NULL COMMENT '实际受胎率',
  `mate_estimate_farrowing_rate` double DEFAULT NULL COMMENT '估算配种分娩率',
  `mate_real_farrowing_rate` double DEFAULT NULL COMMENT '实际配种分娩率',
  `wean_avg_count` double DEFAULT NULL COMMENT '窝均断奶数',
  `wean_avg_day_age` double DEFAULT NULL COMMENT '窝均断奶日龄',
  `dead_farrow_rate` double DEFAULT NULL COMMENT '产房死淘率',
  `dead_nursery_rate` double DEFAULT NULL COMMENT '保育死淘率',
  `dead_fatten_rate` double DEFAULT NULL COMMENT '育肥死淘率',
  `npd` double DEFAULT NULL COMMENT '非生产天数',
  `psy` double DEFAULT NULL COMMENT 'psy',
  `mate_in_seven` double DEFAULT NULL COMMENT '断奶七天配种率',
  `extra` text,
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_range_reports_farm_id_type_sum_at` (`farm_id`,`type`,`sum_at`),
  KEY `idx_doctor_range_reports_farm_id` (`farm_id`),
  KEY `idx_doctor_range_reports_sum_at` (`sum_at`)
) DEFAULT CHARSET=utf8 COMMENT='指标周报和月报';

-- 新的日报表，存放一些可以时间段累加的数量指标
drop table if exists `doctor_daily_reports`;
CREATE TABLE `doctor_daily_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) NOT NULL COMMENT '猪场id',
  `sum_at` varchar(10) DEFAULT NULL COMMENT '日期',
  `sow_ph` int(11) DEFAULT NULL COMMENT '配怀母猪头数',
  `sow_cf` int(11) DEFAULT NULL COMMENT '产房母猪头数',
  `sow_start` int(11) DEFAULT NULL COMMENT '母猪期初头数',
  `sow_in` int(11) DEFAULT NULL COMMENT '母猪转入',
  `sow_dead` int(11) DEFAULT NULL COMMENT '死亡母猪',
  `sow_weed_out` int(11) DEFAULT NULL COMMENT '淘汰母猪',
  `sow_sale` int(11) DEFAULT NULL COMMENT '母猪销售',
  `sow_other_out` int(11) DEFAULT NULL COMMENT '母猪其他减少',
  `sow_chg_farm` int(11) DEFAULT NULL COMMENT '母猪转场',
  `sow_end` int(11) DEFAULT NULL COMMENT '母猪期末头数',
  `sow_ph_start` int(11) DEFAULT NULL COMMENT '配怀母猪期初',
  `sow_ph_in_farm_in` int(11) DEFAULT NULL COMMENT '配怀母猪进场',
  `sow_ph_wean_in` int(11) DEFAULT NULL COMMENT '配怀母猪断奶转入',
  `sow_ph_dead` int(11) DEFAULT NULL COMMENT '配怀母猪死亡',
  `sow_ph_weed_out` int(11) DEFAULT NULL COMMENT '配怀母猪淘汰',
  `sow_ph_sale` int(11) DEFAULT NULL COMMENT '配怀母猪销售',
  `sow_ph_other_out` int(11) DEFAULT NULL COMMENT '配怀母猪其他离场',
  `sow_ph_to_cf` int(11) DEFAULT NULL COMMENT '配怀母猪转产房',
  `sow_ph_chg_farm` int(11) DEFAULT NULL COMMENT '配怀母猪转场',
  `sow_ph_end` int(11) DEFAULT NULL COMMENT '配怀母猪期末',
  `sow_cf_start` int(11) DEFAULT NULL COMMENT '配怀母猪期初',
  `sow_cf_in` int(11) DEFAULT NULL COMMENT '产房母猪转入',
  `sow_cf_dead` int(11) DEFAULT NULL COMMENT '产房母猪死亡',
  `sow_cf_weed_out` int(11) DEFAULT NULL COMMENT '产房母猪淘汰',
  `sow_cf_sale` int(11) DEFAULT NULL COMMENT '产房母猪销售',
  `sow_cf_other_out` int(11) DEFAULT NULL COMMENT '产房母猪放其他离场',
  `sow_cf_wean_out` int(11) DEFAULT NULL COMMENT '产房母猪断奶转出',
  `sow_cf_chg_farm` int(11) DEFAULT NULL COMMENT '产房母猪转场',
  `sow_cf_end` int(11) DEFAULT NULL COMMENT '产房母猪期末',
  `boar_start` int(11) DEFAULT NULL COMMENT '公猪期初头数',
  `boar_in` int(11) DEFAULT NULL COMMENT '公猪转入',
  `boar_dead` int(11) DEFAULT NULL COMMENT '死亡公猪',
  `boar_weed_out` int(11) DEFAULT NULL COMMENT '淘汰公猪',
  `boar_sale` int(11) DEFAULT NULL COMMENT '公猪销售',
  `boar_other_out` int(11) DEFAULT NULL COMMENT '公猪其他减少',
  `boar_chg_farm` int(11) DEFAULT NULL COMMENT '公猪转场',
  `boar_end` int(11) DEFAULT NULL COMMENT '公猪期末头数',
  `mate_hb` int(11) DEFAULT NULL COMMENT '配后备头数',
  `mate_dn` int(11) DEFAULT NULL COMMENT '配断奶头数',
  `mate_fq` int(11) DEFAULT NULL COMMENT '配返情头数',
  `mate_lc` int(11) DEFAULT NULL COMMENT '配流产头数',
  `mate_yx` int(11) DEFAULT NULL COMMENT '配阴性头数',
  `preg_positive` int(11) DEFAULT NULL COMMENT '妊娠检查阳性头数',
  `preg_negative` int(11) DEFAULT NULL COMMENT '妊娠检查阴性头数',
  `preg_fanqing` int(11) DEFAULT NULL COMMENT '妊娠检查返情头数',
  `preg_liuchan` int(11) DEFAULT NULL COMMENT '妊娠检查流产头数',
  `farrow_nest` int(11) DEFAULT NULL COMMENT '分娩窝数',
  `farrow_all` int(11) DEFAULT NULL COMMENT '总产仔数',
  `farrow_live` int(11) DEFAULT NULL COMMENT '产活仔数',
  `farrow_health` int(11) DEFAULT NULL COMMENT '产健仔数',
  `farrow_weak` int(11) DEFAULT NULL COMMENT '产弱仔数',
  `farrow_dead` int(11) DEFAULT NULL COMMENT '产死胎数',
  `farrow_jx` int(11) DEFAULT NULL COMMENT '产畸形仔数',
  `farrow_mny` int(11) DEFAULT NULL COMMENT '产木乃伊仔数',
  `farrow_black` int(11) DEFAULT NULL COMMENT '产黑胎仔数',
  `farrow_sjmh` int(11) DEFAULT NULL COMMENT '产死畸木黑数',
  `farrow_weight` double DEFAULT NULL COMMENT '出生总重',
  `farrow_avg_weight` double DEFAULT NULL COMMENT '出生均重',
  `wean_nest` int(11) DEFAULT NULL COMMENT '断奶窝数',
  `wean_count` int(11) DEFAULT NULL COMMENT '断奶仔猪数',
  `wean_avg_weight` double DEFAULT NULL COMMENT '断奶均重',
  `wean_day_age` double DEFAULT NULL COMMENT '断奶日龄',
  `base_price_10` int(11) DEFAULT NULL COMMENT '10kg基础价格',
  `base_price_15` int(11) DEFAULT NULL COMMENT '15kg基础价格',
  `fatten_price` double DEFAULT NULL COMMENT '育肥猪价格',
  `sow_ph_feed` double DEFAULT NULL COMMENT '配怀母猪饲料消耗数量',
  `sow_ph_feed_amount` int(11) DEFAULT NULL COMMENT '配怀母猪饲料消耗金额',
  `sow_ph_medicine_amount` int(11) DEFAULT NULL COMMENT '配怀母猪药品消耗金额',
  `sow_ph_vaccination_amount` int(11) DEFAULT NULL COMMENT '配怀母猪疫苗消耗金额',
  `sow_ph_consumable_amount` int(11) DEFAULT NULL COMMENT '配怀母猪易耗品消耗金额',
  `sow_cf_feed` double DEFAULT NULL COMMENT '产房母猪饲料消耗数量',
  `sow_cf_feed_amount` int(11) DEFAULT NULL COMMENT '产房母猪饲料消耗金额',
  `sow_cf_medicine_amount` int(11) DEFAULT NULL COMMENT '产房母猪药品消耗金额',
  `sow_cf_vaccination_amount` int(11) DEFAULT NULL COMMENT '产房母猪疫苗消耗金额',
  `sow_cf_consumable_amount` int(11) DEFAULT NULL COMMENT '产房母猪易耗品消耗金额',
  `farrow_feed` double DEFAULT NULL COMMENT '产房仔猪饲料消耗数量',
  `farrow_feed_amount` int(11) DEFAULT NULL COMMENT '产房仔猪饲料消耗金额',
  `farrow_medicine_amount` int(11) DEFAULT NULL COMMENT '产房仔猪药品消耗金额',
  `farrow_vaccination_amount` int(11) DEFAULT NULL COMMENT '产房仔猪疫苗消耗金额',
  `farrow_consumable_amount` int(11) DEFAULT NULL COMMENT '产房仔猪易耗品消耗金额',
  `nursery_feed` double DEFAULT NULL COMMENT '保育猪饲料消耗数量',
  `nursery_feed_amount` int(11) DEFAULT NULL COMMENT '保育猪饲料消耗金额',
  `nursery_medicine_amount` int(11) DEFAULT NULL COMMENT '保育猪药品消耗金额',
  `nursery_vaccination_amount` int(11) DEFAULT NULL COMMENT '保育猪疫苗消耗金额',
  `nursery_consumable_amount` int(11) DEFAULT NULL COMMENT '保育猪易耗品消耗金额',
  `fatten_feed` double DEFAULT NULL COMMENT '育肥猪饲料消耗数量',
  `fatten_feed_amount` int(11) DEFAULT NULL COMMENT '育肥猪饲料消耗金额',
  `fatten_medicine_amount` int(11) DEFAULT NULL COMMENT '育肥猪药品消耗金额',
  `fatten_vaccination_amount` int(11) DEFAULT NULL COMMENT '育肥猪疫苗消耗金额',
  `fatten_consumable_amount` int(11) DEFAULT NULL COMMENT '育肥猪易耗品消耗金额',
  `houbei_feed` double DEFAULT NULL COMMENT '后备猪饲料消耗数量',
  `houbei_feed_amount` int(11) DEFAULT NULL COMMENT '后备猪饲料消耗金额',
  `houbei_medicine_amount` int(11) DEFAULT NULL COMMENT '后备猪药品消耗金额',
  `houbei_vaccination_amount` int(11) DEFAULT NULL COMMENT '后备猪疫苗消耗金额',
  `houbei_consumable_amount` int(11) DEFAULT NULL COMMENT '后备猪易耗品消耗金额',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新事件',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_daily_reports_farm_id_sum_at` (`farm_id`,`sum_at`),
  KEY `doctor_daily_reports_farm_id` (`farm_id`),
  KEY `doctor_daily_reports_sum_at` (`sum_at`)
) DEFAULT CHARSET=utf8 COMMENT='日报，存放一些可以时间段累加的数量指标';

-- 猪群每天的数量汇总表，存放可以时间段累加数量指标
drop table if exists `doctor_daily_groups`;
CREATE TABLE `doctor_daily_groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) NOT NULL COMMENT '猪场id',
  `group_id` bigint(20) NOT NULL COMMENT '猪群id',
  `type` tinyint(4) DEFAULT NULL COMMENT '猪群类型',
  `sum_at` date DEFAULT NULL COMMENT '日期',
  `start` int(11) DEFAULT NULL COMMENT '期初',
  `wean_count` int(11) DEFAULT NULL COMMENT '断奶数量',
  `unwean_count` int(11) DEFAULT NULL COMMENT '未断奶数量',
  `inner_in` int(11) DEFAULT NULL COMMENT '同类型猪群转入，后面统计不计入该类型猪群转入',
  `outer_in` int(11) DEFAULT NULL COMMENT '不同类型猪群转入，外部转入',
  `sale` int(11) DEFAULT NULL COMMENT '销售',
  `dead` int(11) DEFAULT NULL COMMENT '死亡',
  `weed_out` int(11) DEFAULT NULL COMMENT '淘汰',
  `other_change` int(11) DEFAULT NULL COMMENT '其他变动减少',
  `chg_farm` int(11) DEFAULT NULL COMMENT '转场',
  `inner_out` int(11) DEFAULT NULL COMMENT '同类型猪群转群，不计入该类型猪	群减少',
  `to_nursery` int(11) DEFAULT NULL COMMENT '转保育',
  `to_fatten` int(11) DEFAULT NULL COMMENT '转育肥',
  `to_houbei` int(11) DEFAULT NULL COMMENT '转后备',
  `turn_seed` int(11) DEFAULT NULL COMMENT '转种猪',
  `end` int(11) DEFAULT NULL COMMENT '期末',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新事件',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_daily_groups_farm_id_group_id_sum_at` (`farm_id`,`group_id`,`sum_at`),
  KEY `doctor_daily_groups_farm_id` (`farm_id`),
  KEY `doctor_daily_groups_group_id` (`group_id`),
  KEY `idx_doctor_daily_groups_sum_at` (`sum_at`)
) DEFAULT CHARSET=utf8 COMMENT='猪群数量每天记录表，存放可以时间段累加数量指标';

create index doctor_pig_events_barn_id on doctor_pig_events(barn_id);
-- 更新猪事件中barn_type
update doctor_pig_events a, doctor_barns b
set a.barn_type = b.pig_type where a.barn_id = b.id;

-- 2017-04-12
-- 如果因子没有范围，from和to的值一直，from没有值时默认double最小值， to没有值时默认double最大值
drop table if exists `doctor_data_factors`;
CREATE TABLE `doctor_data_factors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` tinyint(4) DEFAULT NULL COMMENT '类型',
  `type_name` varchar(64) DEFAULT NULL,
  `sub_type` tinyint(4) DEFAULT NULL COMMENT '小类：猪类等',
  `sub_type_name` varchar(64) DEFAULT NULL,
  `factor` double DEFAULT NULL COMMENT '系数',
  `range_from` double DEFAULT NULL COMMENT '范围,默认double最小值',
  `range_to` double DEFAULT NULL COMMENT '范围，默认double最大值',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='大数据信用价值计算因子';
alter table doctor_data_factors ADD `is_delete` smallint(6) DEFAULT 0  AFTER `range_to`;


-- 2017-04-15 利润表
drop table if exists `doctor_export_porfit`;
CREATE TABLE `doctor_export_porfit` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场ID',
  `feed_type_name` varchar(20) DEFAULT NULL COMMENT '饲料名',
  `feed_type_id` tinyint(11) DEFAULT NULL COMMENT '饲料ID',
  `feed_amount` double DEFAULT NULL COMMENT '饲料金额',
  `vaccine_type_name` varchar(20) DEFAULT NULL COMMENT '疫苗名称',
  `vaccine_type_id` tinyint(11) DEFAULT NULL COMMENT '疫苗ID',
  `vaccine_amount` double DEFAULT NULL COMMENT '疫苗金额',
  `medicine_type_name` varchar(20) DEFAULT NULL COMMENT '药品名称',
  `medicine_type_id` tinyint(11) DEFAULT NULL COMMENT '药品ID',
  `medicine_amount` double DEFAULT NULL COMMENT '药品金额',
  `consumables_type_name` varchar(20) DEFAULT NULL COMMENT '消耗品名称',
  `consumables_type_id` tinyint(11) DEFAULT NULL COMMENT '消耗品ID',
  `consumables_amount` double DEFAULT NULL COMMENT '消耗品金额',
  `material_type_name` varchar(20) DEFAULT NULL COMMENT '原料名称',
  `material_type_id` tinyint(11) DEFAULT NULL COMMENT '原料ID',
  `material_amount` double DEFAULT NULL COMMENT '原料金额',
  `pig_type_name` varchar(20) DEFAULT NULL COMMENT '猪类型名称',
  `pig_type_name_id` varchar(20) DEFAULT NULL COMMENT '猪类型ID',
  `amount_pig` double DEFAULT NULL COMMENT '猪金额',
  `amount_year_pig` double DEFAULT NULL COMMENT '猪的年利润',
  `amount_year_material` double DEFAULT NULL COMMENT '物料的年消耗',
  `sum_time` datetime DEFAULT NULL COMMENT '统计时间',
  `refresh_time` datetime DEFAULT NULL COMMENT '刷新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9727 DEFAULT CHARSET=utf8;

-- 2017-04-18 领用统计
drop table if exists `doctor_masterial_datails_groups`;
CREATE TABLE `doctor_masterial_datails_groups` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '唯一ID',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '公司ID',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物料ID',
  `material_name` varchar(512) DEFAULT NULL COMMENT '物料名',
  `material_type` int(11) DEFAULT NULL COMMENT '物料类型',
  `types` int(11) DEFAULT NULL COMMENT '类型',
  `type_name` varchar(20) DEFAULT NULL COMMENT '类型名',
  `barn_id` bigint(20) DEFAULT NULL COMMENT '猪舍ID',
  `barn_name` varchar(20) DEFAULT NULL COMMENT '猪舍名',
  `numbers` double DEFAULT NULL COMMENT '数量',
  `price` bigint(20) DEFAULT NULL COMMENT '单价',
  `price_sum` double DEFAULT NULL COMMENT '金额',
  `unit_name` varchar(20) DEFAULT NULL COMMENT '物料单位',
  `group_id` bigint(20) DEFAULT NULL COMMENT '猪群ID',
  `group_name` varchar(512) DEFAULT NULL COMMENT '猪群名',
  `ware_house_id` bigint(20) DEFAULT NULL COMMENT '仓库ID',
  `ware_house_name` varchar(20) DEFAULT NULL COMMENT '仓库名',
  `event_at` datetime DEFAULT NULL,
  `people` varchar(20) DEFAULT NULL COMMENT '饲养员',
  `flag` smallint(2) DEFAULT NULL COMMENT '标志位',
  `open_at` datetime DEFAULT NULL COMMENT '建群时间',
  `close_at` datetime DEFAULT NULL COMMENT '关群时间',
  `flush_date` datetime DEFAULT NULL COMMENT '刷新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=471866 DEFAULT CHARSET=utf8;

-- 2017-05-03 公猪生产成绩月报
alter table doctor_boar_monthly_reports add column `boar_type` varchar(32) default null comment '公猪类型' after `boar_code`;
```

### 3. 迁移猪事件、猪群事件
1、2可以同时执行，3、4在1、2之后顺序执行
> 1. 执行 migratePigEventExtraToColumn.py ，将猪事件新增的字段刷好
> 2. 执行 migrateGroupEventExtraToColumn.py ， 将猪群事件新增字段刷好   16W条大概3分钟
> 3. 执行 repairGroupEventExtra.py ， 按最新的input完善猪群的extra，主要是eventAt, quantity 字段
> 4. 执行 rebuildGroupCloseEvent.py ， 重新生成猪群关闭事件，保证猪群关闭事件的id在该猪群里最大
> 5. 执行 repairPigEvents.py


### 4. 重新生成报表
统一先只刷今年的报表；1、2可以同时执行，3必须要在1、2执行完之后执行
> 1. 执行 日报[http://doctor-move.xnrm.com/api/doctor/move/data/daily/since?since=2017-01-01](http://doctor-move.xnrm.com/api/doctor/move/data/daily/since?since=2017-01-01)
> 2. 执行 猪群日统计[http://doctor-move.xnrm.com/api/doctor/move/data/group/daily?since=2017-01-01](http://doctor-move.xnrm.com/api/doctor/move/data/group/daily?since=2017-01-01)
> 3. 执行 月报指标[http://doctor-move.xnrm.com/api/doctor/move/data/monthly/since?since=2017-01-01](http://doctor-move.xnrm.com/api/doctor/move/data/monthly/since?since=2017-01-01)
> 4. 仓库报表
