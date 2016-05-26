-- 卖家角色表: doctor_seller_roles
DROP TABLE IF EXISTS `doctor_seller_roles`;
CREATE TABLE `doctor_seller_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NULL COMMENT '用户名',
  `desc` VARCHAR(32) NULL COMMENT '角色描述',
  `shop_id` bigint(20) NULL COMMENT '店铺 ID',
  `status` SMALLINT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` VARCHAR(1024) NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` VARCHAR(1024) NULL COMMENT '',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='卖家角色表';
CREATE INDEX idx_seller_roles_shop_id ON `doctor_seller_roles` (`shop_id`);

-- 运营角色表: doctor_operator_roles
DROP TABLE IF EXISTS `doctor_operator_roles`;
CREATE TABLE `doctor_operator_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NULL COMMENT '用户名',
  `desc` VARCHAR(32) NULL COMMENT '角色描述',
  `status` SMALLINT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` VARCHAR(1024) NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` VARCHAR(1024) NULL COMMENT '',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='运营角色表';

-- 用户运营表: doctor_user_operators
DROP TABLE IF EXISTS `doctor_user_operators`;
CREATE TABLE `doctor_user_operators` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT          NULL COMMENT '用户 ID',
  `role_id`    BIGINT          NULL COMMENT '运营角色 ID',
  `status`     TINYINT         NULL COMMENT '运营状态',
  `extra_json` VARCHAR(1024)   NULL COMMENT '运营额外信息, 建议json字符串',
  `created_at` DATETIME        NOT NULL,
  `updated_at` DATETIME        NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '用户运营表';
CREATE INDEX idx_user_operator_user_id ON `doctor_user_operators` (`user_id`);
CREATE INDEX idx_user_operator_role_id ON `doctor_user_operators` (`role_id`);

-- 商家子账户表: doctor_user_sub_sellers
DROP TABLE IF EXISTS `doctor_user_sub_sellers`;
CREATE TABLE `doctor_user_sub_sellers` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT(20)          NULL COMMENT '用户 ID',
  `user_name`  VARCHAR(64)         NULL COMMENT '用户名 (冗余)',
  `shop_id`    BIGINT(20)          NULL COMMENT '店铺 ID',
  `status`     TINYINT             NULL COMMENT '状态',
  `roles_json` VARCHAR(1024)       NULL COMMENT '角色 ID 列表',
  `extra_json` VARCHAR(1024)       NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` DATETIME            NOT NULL,
  `updated_at` DATETIME            NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '商家子账户表';
CREATE INDEX idx_user_sub_seller_user_id ON `doctor_user_sub_sellers` (`user_id`);
CREATE INDEX idx_user_sub_seller_sub_id ON `doctor_user_sub_sellers` (shop_id);


-- 用户设备信息表 doctor_user_devices
DROP TABLE IF EXISTS `doctor_user_devices`;
create table `doctor_user_devices` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint null COMMENT '用户ID',
  `user_name` VARCHAR(64) COMMENT '用户名',
  `device_token` VARCHAR(128) COMMENT '',
  `device_type` VARCHAR(128) COMMENT '',
  `created_at` datetime NULL ,
  `updated_at` datetime NULL ,
   PRIMARY KEY (`id`)
) COMMENT = '用户设备信息表';
CREATE INDEX idx_user_devices_user_id ON `doctor_user_devices` (`user_id`);
CREATE INDEX idx_user_devices_token ON `doctor_user_devices` (`device_token`);


-- 商家子账户表: doctor_user_sub_sellers
DROP TABLE IF EXISTS `doctor_user_sellers`;
CREATE TABLE `doctor_user_sellers` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT(20)          NULL COMMENT '用户 ID',
  `user_name`  VARCHAR(64)         NULL COMMENT '用户名 (冗余)',
  `shop_id`    BIGINT(20)          NULL COMMENT '店铺 ID',
  `shop_name`  VARCHAR(64)         NULL COMMENT '店铺名 (冗余)',
  `status`     TINYINT             NULL COMMENT '状态',
  `extra_json` VARCHAR(1024)       NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` DATETIME            NOT NULL,
  `updated_at` DATETIME            NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '商家子账户表';
CREATE INDEX idx_user_seller_user_id ON `doctor_user_sellers` (`user_id`);
CREATE INDEX idx_user_seller_sub_id ON `doctor_user_sellers` (shop_id);



-- -----------------------------------------------------------------
-- ------- 流程定义表 ------------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_definitions`;
CREATE TABLE IF NOT EXISTS `workflow_definitions` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识, 按照版本号区分',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程定义name属性名称',
	`version` BIGINT(20) DEFAULT NULL COMMENT '版本号，用于获取最新的流程定义',
	`resource_name` VARCHAR(128) DEFAULT NULL COMMENT '资源文件的名称(通常指流程定义的xml文件)',
	`resource` text COMMENT '流程定义的资源内容(通常是xml中的内容)',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程定义的状态(1:正常, -1:删除, -2:禁用)',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '发布者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '发布者姓名',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程定义表';
CREATE INDEX idx_flow_definition_key ON workflow_definitions(`key`);


-- -----------------------------------------------------------------
-- ------- 流程节点表 ------------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_definition_nodes`;
CREATE TABLE IF NOT EXISTS `workflow_definition_nodes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`name` VARCHAR(32) DEFAULT NULL COMMENT '节点name属性名称',
	`node_name` VARCHAR(32) DEFAULT NULL COMMENT '节点标签名称',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '节点类型, 1->开始节点, 2->任务节点, 3->选择节点, 4->并行节点, 5->并行汇聚节点, 10->子流程开始节点, -10->子流程结束节点, -1->结束节点',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`point_x` DOUBLE DEFAULT NULL COMMENT '节点x轴偏移量',
	`point_y` DOUBLE DEFAULT NULL COMMENT '节点y轴偏移量',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程定义节点表';
CREATE INDEX idx_flow_definition_nodes_def_id ON workflow_definition_nodes(flow_definition_id);


-- -----------------------------------------------------------------
-- ------- 流程节点事件驱动表 -----------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_definition_node_events`;
CREATE TABLE IF NOT EXISTS `workflow_definition_node_events`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '连线name属性名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id,冗余',
	`source_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程源节点的id',
	`handler` VARCHAR(128) DEFAULT NULL COMMENT '事件驱动处理类(一般为类标识)',
	`expression` text COMMENT '事件驱动表达式',
	`target_node_id` BIGINT(20) DEFAULT NULL COMMENT '驱动的目标节点id',
	`describe` text COMMENT '连线描述',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程定义节点连线事件表';
CREATE INDEX idx_flow_node_event_def_id ON workflow_definition_node_events(flow_definition_id);
CREATE INDEX idx_flow_node_event_src_id ON workflow_definition_node_events(source_node_id);
CREATE INDEX idx_flow_node_event_handler ON workflow_definition_node_events(handler);


-- -----------------------------------------------------------------
-- ------- 流程实例表 ------------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_process_instances`;
CREATE TABLE IF NOT EXISTS `workflow_process_instances`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程实例的名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`flow_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识,冗余',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '与流程实例相关联的业务id',
	`business_data` text COMMENT '流程实例全局业务数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程实例的状态, 1->正常, 2->正常结束, -1->删除, -2->挂起',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '流程实例类型, 1-> 主流程, 2-> 子流程',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`parent_instance_id` BIGINT(20) DEFAULT NULL COMMENT '父流程实例id',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例表';
CREATE INDEX idx_flow_instance_def_id ON workflow_process_instances(flow_definition_id);
CREATE INDEX idx_flow_instance_def_key ON workflow_process_instances(flow_definition_key);
CREATE INDEX idx_flow_instance_busi_id ON workflow_process_instances(business_id);


-- -----------------------------------------------------------------
-- ------- 当前流程实例的活动节点表 ------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_processes`;
CREATE TABLE IF NOT EXISTS `workflow_processes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态, 1->正常, 2->正常结束, -1->删除, -2->挂起',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留),该值优先于流程定义节点的值',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例的当前活动节点表';
CREATE INDEX idx_flow_process_ins_id ON workflow_processes(flow_instance_id);



-- -----------------------------------------------------------------
-- ------- 流程处理节点跟踪表 -----------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_process_tracks`;
CREATE TABLE IF NOT EXISTS `workflow_process_tracks`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态,冗余workflow_process表',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程处理节点跟踪表';
CREATE INDEX idx_flow_process_track_ins_id ON workflow_process_tracks(flow_instance_id);



-- -----------------------------------------------------------------
-- ------- 流程实例历史表 --------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_history_process_instances`;
CREATE TABLE IF NOT EXISTS `workflow_history_process_instances`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程实例的名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`flow_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识,冗余',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '与流程实例相关联的业务id',
	`business_data` text COMMENT '流程实例全局业务数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程实例的状态,冗余流程实例表',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '流程实例类型, 1-> 主流程, 2-> 子流程',
	`describe` text COMMENT '历史流程描述',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`parent_instance_id` BIGINT(20) DEFAULT NULL COMMENT '父流程实例id',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例历史表';
CREATE INDEX idx_flow_instance_his_def_id ON workflow_history_process_instances(flow_definition_id);
CREATE INDEX idx_flow_instance_his_def_key ON workflow_history_process_instances(flow_definition_key);
CREATE INDEX idx_flow_instance_his_busi_id ON workflow_history_process_instances(business_id);



-- -----------------------------------------------------------------
-- ------- 流程实例的活动节点历史表 ------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_history_processes`;
CREATE TABLE IF NOT EXISTS `workflow_history_processes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`describe` text COMMENT '历史节点流转描述',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态,冗余workflow_process表',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例的活动节点历史表';
CREATE INDEX idx_flow_process_his_ins_id ON workflow_history_processes(flow_instance_id);