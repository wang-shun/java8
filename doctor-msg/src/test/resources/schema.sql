-- 消息中心 2016-06-06
CREATE TABLE `doctor_message_rule_template_tracks` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则, 是farm对应的默认值, json值, 类: Rule',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`message_template`	VARCHAR(128) DEFAULT NULL COMMENT '规则数据模板名称, 对应parana_message_templates表name字段',
	`content`	TEXT DEFAULT NULL COMMENT '规则的内容, 针对系统消息',
	`producer`	VARCHAR(128) DEFAULT NULL COMMENT '消息生成者(类的简单类名)',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则模板描述',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	`updator_id`	BIGINT(20) DEFAULT NULL COMMENT '修改人id',
	`updator_name`	VARCHAR(32) DEFAULT NULL COMMENT '修改人name',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则模板表(历史跟踪)';

CREATE TABLE `doctor_message_rule_templates` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则, 是farm对应的默认值, json值, 类: Rule',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`message_template`	VARCHAR(128) DEFAULT NULL COMMENT '规则数据模板名称, 对应parana_message_templates表name字段',
	`content`	TEXT DEFAULT NULL COMMENT '规则的内容, 针对系统消息',
	`producer`	VARCHAR(128) DEFAULT NULL COMMENT '消息生成者(类的简单类名)',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则模板描述',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	`updated_by`	BIGINT(20) DEFAULT NULL COMMENT '修改人id',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则模板表';

CREATE TABLE `doctor_message_rules` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`template_name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则值, 是role对应表的默认值, json值, 类: Rule',
	`use_default`	SMALLINT(6) DEFAULT NULL COMMENT '是否使用默认配置, 0:不使用, 1:使用',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则模板描述',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则表';

CREATE TABLE `doctor_message_rule_roles` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`rule_id`	BIGINT(20) DEFAULT NULL COMMENT '消息规则id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`role_id`	BIGINT(20) DEFAULT NULL COMMENT '子账号的角色id',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则值, json值, 类: Rule',
	`use_default`	SMALLINT(6) DEFAULT NULL COMMENT '是否使用默认配置, 0:不使用, 1:使用',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则与角色表';

CREATE TABLE `doctor_messages` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`rule_id`	BIGINT(20) DEFAULT NULL COMMENT '消息规则id',
	`role_id`	BIGINT(20) DEFAULT NULL COMMENT '子账号的角色id',
	`user_id` BIGINT(20) DEFAULT NULL COMMENT '用户id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`message_template`	VARCHAR(128) DEFAULT NULL COMMENT '规则数据模板名称, 对应parana_message_templates表name字段',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`data`	TEXT DEFAULT NULL COMMENT '发送的内容填充数据, json(map). 或系统消息',
	`channel`	SMALLINT(6) DEFAULT NULL COMMENT '消息发送渠道. 0->站内信, 1->短信, 2->邮箱, 3->app推送',
	`url`		VARCHAR(4096)	DEFAULT NULL COMMENT 'app回调url',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败',
	`sended_at`	DATETIME DEFAULT NULL COMMENT '发送时间',
	`failed_by`	VARCHAR(4096) DEFAULT NULL COMMENT '失败原因',
	`created_by` BIGINT(20) DEFAULT NULL COMMENT '操作人id',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息表';



-- data
INSERT INTO `doctor_message_rule_templates`
(name, type, category, rule_value, status, message_template, content, producer, `describe`, created_at, updated_at, updated_by)
VALUES
-- 系统消息(可配多少小时后)
('系统消息测试', 0, 0,
	'{
		"frequence":-1,
		"channels":"0,1,2,3"
	}',
1, 'msg.sys.normal', '{"data":"系统消息测试"}', 'sysMessageProducer', '系统消息测试', now(), now(), null),

-- id:1 (断奶、流产、返情日期间隔(天))
('待配种母猪提示', 1, 1,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":7, "describe":"断奶、流产、返情日期间隔(天)"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.breed', null, 'sowBreedingProducer', '待配种母猪提示', now(), now(), null),
('待配种母猪警报', 2, 1,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":21, "describe":"断奶、流产、返情日期间隔(天)"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.breed', null, 'sowBreedingProducer', '待配种母猪警报', now(), now(), null),

-- id:1 (母猪已配种时间间隔(天))
('母猪需妊娠检查提示', 1, 2,
	'{
		"values":[
			{"id":1, "ruleType":2,"leftValue":19,"rightValue":25, "describe":"母猪已配种时间间隔(天)"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.preg.check', null, 'sowPregCheckProducer', '母猪需妊娠检查提示', now(), now(), null),

-- 母猪需转入妊娠舍提示
('母猪需转入妊娠舍提示', 1, 3,
	'{
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.preg.home', null, 'sowPregHomeProducer', '母猪需转入妊娠舍提示', now(), now(), null),

-- id:1 (预产期提前多少天提醒)
('母猪预产期提示', 1, 4,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":7, "describe":"预产期提前多少天提醒"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.birth.date', null, 'sowBirthDateProducer', '母猪预产期提示', now(), now(), null),

-- id:1 (母猪分娩日期起的天数)
('母猪需断奶提示', 1, 5,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":21, "describe":"母猪分娩日期起的天数"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶提示', now(), now(), null),
('母猪需断奶警报', 2, 5,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":35, "describe":"母猪分娩日期起的天数"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶警报', now(), now(), null),

-- id:1 (母猪胎次)
('母猪应淘汰提示', 1, 6,
	'{
		"values":[
			{"id":1, "ruleType":2, "leftValue":9,"rightValue":10, "describe":"胎次"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.eliminate', null, 'sowEliminateProducer', '母猪应淘汰提示', now(), now(), null),

-- id:1 (公猪配种次数)
('公猪应淘汰提示', 1, 7,
	'{
		"values":[
			{"id":1, "ruleType":2, "leftValue":10,"rightValue":15, "describe":"公猪配种次数"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.boar.eliminate', null, 'boarEliminateProducer', '公猪应淘汰提示', now(), now(), null),

-- id:1 (母猪配种日期起的天数)
('母猪未产仔警报', 2, 10,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":120, "describe":"母猪配种日期起的天数"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.sow.not.litter', null, 'sowNotLitterProducer', '母猪未产仔警报', now(), now(), null),

-- id:1 (库存量)
('仓库库存不足提示', 1, 9,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":7, "describe":"库存量"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足提示', now(), now(), null),
('仓库库存不足警报', 2, 9,
	'{
		"values":[
			{"id":1, "ruleType":1,"value":3, "describe":"库存量"}
		],
		"frequence":24,
		"channels":"0,1,2,3"
	}',
1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足警报', now(), now(), null)
;

INSERT INTO `doctor_message_rules` (`id`, `farm_id`, `template_id`, `template_name`, `type`, `category`, `rule_value`, `use_default`, `status`, `describe`, `created_at`, `updated_at`)
VALUES
	(1,1,2,'待配种母猪提示',1,1,'{	"values":[		{"id":1, "ruleType":1,"value":7, "describe":"断奶、流产、返情日期间隔(天)"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'待配种母猪提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(2,1,3,'待配种母猪警报',2,1,'{	"values":[		{"id":1, "ruleType":1,"value":21, "describe":"断奶、流产、返情日期间隔(天)"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'待配种母猪警报','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(3,1,4,'母猪需妊娠检查提示',1,2,'{	"values":[		{"id":1, "ruleType":2,"leftValue":19,"rightValue":25, "describe":"母猪已配种时间间隔(天)"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪需妊娠检查提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(4,1,5,'母猪需转入妊娠舍提示',1,3,'{	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪需转入妊娠舍提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(5,1,6,'母猪预产期提示',1,4,'{	"values":[		{"id":1, "ruleType":1,"value":7, "describe":"预产期提前多少天提醒"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪预产期提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(6,1,7,'母猪需断奶提示',1,5,'{	"values":[		{"id":1, "ruleType":1,"value":21, "describe":"母猪分娩日期起的天数"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪需断奶提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(7,1,8,'母猪需断奶警报',2,5,'{	"values":[		{"id":1, "ruleType":1,"value":35, "describe":"母猪分娩日期起的天数"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪需断奶警报','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(8,1,9,'母猪应淘汰提示',1,6,'{	"values":[		{"id":1, "ruleType":2, "leftValue":9,"rightValue":10, "describe":"胎次"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪应淘汰提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(9,1,10,'公猪应淘汰提示',1,7,'{	"values":[		{"id":1, "ruleType":2, "leftValue":10,"rightValue":15, "describe":"公猪配种次数"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'公猪应淘汰提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(10,1,11,'母猪未产仔警报',2,10,'{	"values":[		{"id":1, "ruleType":1,"value":120, "describe":"母猪配种日期起的天数"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'母猪未产仔警报','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(11,1,12,'仓库库存不足提示',1,9,'{	"values":[		{"id":1, "ruleType":1,"value":7, "describe":"库存量"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'仓库库存不足提示','2016-06-12 09:58:27','2016-06-12 09:58:27'),
	(12,1,13,'仓库库存不足警报',2,9,'{	"values":[		{"id":1, "ruleType":1,"value":3, "describe":"库存量"}	],	"frequence":24,	"channels":"0,1,2,3"}',1,1,'仓库库存不足警报','2016-06-12 09:58:27','2016-06-12 09:58:27');


