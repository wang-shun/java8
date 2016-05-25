-- 猪场类别测试方式
drop table if exists doctor_farm_ware_house_types;
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
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场仓库类型数量';
