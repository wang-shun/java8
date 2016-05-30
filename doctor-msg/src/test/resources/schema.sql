CREATE TABLE IF NOT EXISTS `doctor_message_rules` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`rule_type`	SMALLINT(6) DEFAULT NULL COMMENT '预警规则类型',	  -- TODO
	`rule_value`	BIGINT(20) DEFAULT NULL COMMENT '预警默认值',
	`rule_start_value`	BIGINT(20) DEFAULT NULL COMMENT '预警默认开始值',
	`rule_end_value`	BIGINT(20) DEFAULT NULL COMMENT '预警默认结束值',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则描述',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`template_name`	VARCHAR(128) DEFAULT NULL COMMENT '规则模板名称, 对应parana_message_templates表name字段',
	`content`	text DEFAULT NULL COMMENT '规则的内容, 针对系统消息',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场软件消息规则表';

CREATE TABLE IF NOT EXISTS `doctor_message_rule_roles` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`role_id`	BIGINT(20) DEFAULT NULL COMMENT '子账号的角色id',
	`rule_id`	BIGINT(20) DEFAULT NULL COMMENT '预警类型id',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型, doctor_message_rules表冗余',
	`rule_type`	SMALLINT(6) DEFAULT NULL COMMENT '预警规则类型, doctor_message_rules表冗余',
	`rule_value`	BIGINT(20) DEFAULT NULL COMMENT '预警值',
	`rule_start_value`	BIGINT(20) DEFAULT NULL COMMENT '预警开始值',
	`rule_end_value`	BIGINT(20) DEFAULT NULL COMMENT '预警结束值',
	`channel`	VARCHAR(32) DEFAULT NULL COMMENT '消息发送渠道, 多个以逗号分隔. 0->站内信, 1->短信, 2->邮箱',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场软件消息规则与角色表';

CREATE TABLE IF NOT EXISTS `doctor_messages` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`user_id` BIGINT(20) DEFAULT NULL COMMENT '用户id',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型, doctor_message_rules表冗余',
	`content`	text DEFAULT NULL COMMENT '发送的内容',
	`channel`	VARCHAR(32) DEFAULT NULL COMMENT '消息发送渠道, 多个以逗号分隔. 0->站内信, 1->短信, 2->邮箱',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常未阅读, 2:正常已阅读, -1:删除, -2:发送失败',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人id',
	`operator_name` VARCHAR(128) DEFAULT NULL COMMENT '操作人name',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场软件消息表';

-- TODO

