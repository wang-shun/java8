
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
)COMMENT='流程定义表';


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
)COMMENT='流程定义节点表';


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
)COMMENT='流程定义节点连线事件表';


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
)COMMENT='流程实例表';


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
)COMMENT='流程实例的当前活动节点表';



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
)COMMENT='流程处理节点跟踪表';



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
)COMMENT='流程实例历史表';



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
)COMMENT='流程实例的活动节点历史表';