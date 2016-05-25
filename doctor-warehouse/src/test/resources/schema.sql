-- 猪场类别测试方式
create table doctor_farm_ware_house_types(
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `type` SMALLINT(6) unsigned DEFAULT NULL COMMENT '猪场仓库类型',
  `log_number` bigint(20) DEFAULT NULL COMMENT '类型原料的数量',
  `extra` text default null comment '扩展字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
   primary key(id)
) COMMENT='猪场仓库类型数量';

CREATE TABLE `doctor_ware_houses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `manager_id` bigint(20) unsigned DEFAULT NULL COMMENT '管理员Id',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '管理人员姓名',
  `address` varchar(64) DEFAULT NULL COMMENT '地址信息',
  `type` smallint(6) DEFAULT NULL comment '仓库类型，一个仓库只能属于一个',
  `extra` text DEFAULT NULL comment '扩展信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)COMMENT='仓库信息数据表';
create index doctor_ware_houses_farm_id on doctor_ware_houses(farm_id);

CREATE TABLE `doctor_ware_house_tracks` (
  `ware_house_id` bigint(20) unsigned NOT NULL COMMENT 'ware_house_id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `manager_id` bigint(20) unsigned DEFAULT NULL COMMENT '管理员Id',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '管理人员姓名',
  `material_lot_number` text DEFAULT NULL comment '各种原料的数量信息',
  `lot_number` bigint(20) DEFAULT NULL comment '仓库物品的总数量信息',
  `is_default` smallint(6) DEFAULT NULL COMMENT '默认仓库信息',
  `extra` text DEFAULT NULL comment '扩展信息',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`ware_house_id`)
) COMMENT='仓库信息Track数据表';
create index doctor_ware_house_tracks_farm_id on doctor_ware_house_tracks(farm_id);

CREATE TABLE `doctor_material_infos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `type` smallint(6) DEFAULT NULL comment '物料所属原料的名称',
  `material_name` VARCHAR (128) DEFAULT NULL comment '物料名称',
  `remark` text COMMENT '标注',
  `unit_group_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位组Id',
  `unit_group_name` varchar(64) DEFAULT NULL COMMENT '单位组名称',
  `unit_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位Id',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '单位名称',
  `default_consume_count` bigint(20) DEFAULT NULL COMMENT '原料默认消耗数量',
  `price` bigint(20) DEFAULT NULL COMMENT '价格',
  `extra` text COMMENT '扩展信息: 药品-默认计量的大小',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物料信息表内容';
CREATE index doctor_material_infos_farm_id on doctor_material_infos(farm_id);

CREATE TABLE `doctor_material_in_ware_houses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场姓名',
  `ware_house_id` bigint(20) unsigned DEFAULT NULL COMMENT '仓库信息',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `material_id` bigint(20) DEFAULT NULL COMMENT '原料Id',
  `material_name` varchar(64) DEFAULT NULL COMMENT '原料名称',
  `type` smallint(6) DEFAULT NULL comment '仓库类型, 冗余',
  `lot_number` bigint(20) DEFAULT NULL COMMENT '数量信息',
  `unit_group_name` varchar(64) DEFAULT NULL COMMENT '单位组信息',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '单位信息',
  `extra` text COMMENT '扩展',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='仓库原料信息表';
CREATE index doctor_material_in_ware_houses_farm_id on doctor_material_in_ware_houses(farm_id);
CREATE index doctor_material_in_ware_houses_ware_house_id on doctor_material_in_ware_houses(ware_house_id);
CREATE index doctor_material_in_ware_houses_material_id on doctor_material_in_ware_houses(material_id);

-- create consume provider event table
CREATE TABLE `doctor_material_consume_providers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` bigint(20) unsigned DEFAULT NULL COMMENT '领取货物属于的类型',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场姓名',
  `ware_house_id` bigint(20) unsigned DEFAULT NULL COMMENT '仓库信息',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `material_id` bigint(20) DEFAULT NULL COMMENT '原料Id',
  `material_name` varchar(64) DEFAULT NULL COMMENT '原料名称',
  `event_time` datetime DEFAULT NULL COMMENT '事件日期',
  `event_type` smallint(6) DEFAULT NULL COMMENT '事件类型, provider 提供， consumer 消费',
  `event_count` bigint(20) DEFAULT NULL COMMENT '事件数量',
  `staff_id` bigint(20) unsigned DEFAULT NULL COMMENT '工作人员Id',
  `staff_name` varchar(64) DEFAULT NULL COMMENT '关联事件人',
  `extra` text COMMENT '领用: 领用猪群的信息, 消耗天数。 提供： 供应商的名称， 相关信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='领用记录信息表';
CREATE index doctor_material_consume_providers_farm_id on doctor_material_consume_providers(farm_id);
create index doctor_material_consume_providers_ware_house_id on doctor_material_consume_providers(ware_house_id);
create index doctor_material_consume_providers_material_id on doctor_material_consume_providers(material_id);

-- create consume avg
CREATE TABLE `doctor_material_consume_avgs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `ware_house_id` bigint(20) unsigned DEFAULT NULL COMMENT '仓库信息',
  `material_id` bigint(20) DEFAULT NULL COMMENT '原料Id',
  `consume_avg_count` bigint(20) DEFAULT NULL COMMENT '平均消耗数量',
  `consume_count` bigint(20) DEFAULT NULL COMMENT '消耗数量',
  `consime_date` datetime DEFAULT NULL comment '消耗日期',
  `extra` text DEFAULT NULL comment 'extra',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物料消耗信息统计方式';
CREATE index doctor_material_consume_avgs_farm_id on doctor_material_consume_avgs(farm_id);
create index doctor_material_consume_avgs_wware_house_id on doctor_material_consume_avgs(ware_house_id);
create index doctor_material_consume_avgs_material_id on doctor_material_consume_avgs(material_id);