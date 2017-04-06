alter table parana_user_profiles
modify column user_id  bigint(20) NOT NULL COMMENT '用户id',
modify column `province_id` bigint(20) NULL COMMENT '省id',
modify column `province` VARCHAR(100) NULL COMMENT '省',
modify column `avatar` VARCHAR(512) NULL COMMENT '头像';

alter table doctor_service_reviews
add column `real_name` VARCHAR (16) DEFAULT NULL COMMENT '用户申请服务时填写的真实姓名' after user_mobile;

-- 消息规则模板初始化
alter table parana_message_templates
modify column content text NOT NULL COMMENT '消息的内容模板, handlebars格式';

INSERT INTO `doctor_message_rule_templates`
(name, type, category, rule_value, status, message_template, content, producer, `describe`, created_at, updated_at, updated_by)
VALUES
('待配种提醒', 1, 1, '{"values":[{"id":1, "ruleType":1,"value":7, "describe":"断奶、流产、返情日期间隔(天)"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}', 1, 'msg.sow.breed', null, 'sowBreedingProducer', '待配种母猪提示', now(), now(), null),
('待配种警示', 2, 1,'{"values":[{"id":1, "ruleType":1,"value":21, "describe":"断奶、流产、返情日期间隔(天)"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}', 1, 'msg.sow.breed', null, 'sowBreedingProducer', '待配种母猪警报', now(), now(), null),
('妊娠检查提醒', 1, 2,'{"values":[{"id":1, "ruleType":2,"leftValue":19,"rightValue":25, "describe":"母猪已配种时间间隔(天)"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.preg.check', null, 'sowPregCheckProducer', '母猪需妊娠检查提示', now(), now(), null),
('妊娠转入提醒', 1, 3,'{"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.preg.home', null, 'sowPregHomeProducer', '母猪需转入妊娠舍提示', now(), now(), null),
('预产提醒', 1, 4,'{"values":[{"id":1, "ruleType":1,"value":7, "describe":"预产期提前多少天提醒"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.birth.date', null, 'sowBirthDateProducer', '母猪预产期提示', now(), now(), null),
('断奶提醒', 1, 5,'{"values":[{"id":1, "ruleType":1,"value":21, "describe":"母猪分娩日期起的天数"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶提示', now(), now(), null),
('断奶警示', 2, 5,'{"values":[{"id":1, "ruleType":1,"value":35, "describe":"母猪分娩日期起的天数"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶警报', now(), now(), null),
('母猪淘汰提醒', 1, 6,'{"values":[{"id":1, "ruleType":1, "value":10, "describe":"胎次"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.eliminate', null, 'sowEliminateProducer', '母猪应淘汰提示', now(), now(), null),
('公猪淘汰提醒', 1, 7,'{"values":[{"id":1, "ruleType":1, "value":20, "describe":"公猪配种次数"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.boar.eliminate', null, 'boarEliminateProducer', '公猪应淘汰提示', now(), now(), null),
('免疫提醒', 1, 8,'{"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.pig.vaccination', null, 'pigVaccinationProducer', '猪只免疫提示', now(), now(), null),
('产仔警示', 2, 10,'{"values":[{"id":1, "ruleType":1,"value":120, "describe":"母猪配种日期起的天数"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.sow.not.litter', null, 'sowNotLitterProducer', '母猪未产仔警报', now(), now(), null),
('库存提醒', 1, 9,'{"values":[{"id":1, "ruleType":1,"value":7, "describe":"库存量"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足提示', now(), now(), null),
('库存警示', 2, 9,'{"values":[{"id":1, "ruleType":1,"value":3, "describe":"库存量"}],"frequence":24,"channels":"0,1,2,3","url":"/message/detail"}',1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足警报', now(), now(), null);

-- 发送消息模板
INSERT INTO `parana_message_templates` (`creator_id`, `creator_name`, `name`, `title`, `content`, `context`, `channel`, `disabled`, `description`, `created_at`, `updated_at`)
VALUES
(1, 'admin', 'msg.sys.normal.sys',   '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 0, 0, '一般系统消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sys.normal.sms', 	 '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 1, 0, '一般系统消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sys.normal.email', '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 2, 0, '一般系统消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sys.normal.app',   '一般系统消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{data}}","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{data}}"}}}', '{"data":"系统消息提示"}', 3, 0, '一般系统消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.sys',   '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪待配种消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.sms',   '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪待配种消息短息模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.email', '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪待配种消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.app',   '母猪待配种消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪待配种消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.sys',   '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪妊娠检查消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.sms',   '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪妊娠检查消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.email', '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪妊娠检查消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.app',   '母猪妊娠检查消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪妊娠检查消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.sys',   '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪转舍消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.sms',   '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪转舍消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.email', '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪转舍消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.app',   '母猪转舍消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪转舍消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.sys',   '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪预产消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.sms',   '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪预产消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.email', '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪预产消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.app',   '母猪预产消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪预产消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.sys',   '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪断奶消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.sms',   '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪断奶消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.email', '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪断奶消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.app',   '母猪断奶消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪断奶消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.sys',   '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪淘汰消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.sms',   '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪淘汰消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.email', '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪淘汰消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.app',   '母猪淘汰消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪淘汰消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.sys',   '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '公猪淘汰消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.sms',   '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '公猪淘汰消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.email', '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '公猪淘汰消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.app',   '公猪淘汰消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '公猪淘汰消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.sys',   '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪未产仔消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.sms',   '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪未产仔消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.email', '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪未产仔消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.app',   '母猪未产仔消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。"}}}', '', 3, 0, '母猪未产仔消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.sys',   '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 0, 0, '仓库库存不足消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.sms',   '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 1, 0, '仓库库存不足消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.email', '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 2, 0, '仓库库存不足消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.app',   '仓库库存不足消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。"}}}', '', 3, 0, '仓库库存不足消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.sys',   '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 0, 0, '猪只免疫消息消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.sms',   '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 1, 0, '猪只免疫消息消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.email', '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 2, 0, '猪只免疫消息消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.app',   '猪只免疫消息', '{"payload":{"body":{"ticker":"{{ticker}}","title":"{{title}}","text":"{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}","after_open":"{{after_open}}", "url":"{{{url}}}"}, "aps":{"alert":"{{title}}\\n{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}"}}}', '', 3, 0, '猪只免疫消息消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43');

-- 2016-06-24 增加输入码, 增加基础数据表
ALTER TABLE doctor_diseases ADD COLUMN `srm` VARCHAR (32) DEFAULT NULL COMMENT '输入码(快捷输入)' AFTER farm_name;

-- 新增基础数据表, 整合一些基础数据
DROP TABLE IF EXISTS `doctor_basics`;
CREATE TABLE `doctor_basics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(32) DEFAULT NULL COMMENT '基础数据内容',
  `type` smallint(6) DEFAULT NULL COMMENT '基础数据类型 枚举',
  `type_name` varchar(32) DEFAULT NULL COMMENT '数据类型名称',
  `srm` varchar(32) DEFAULT NULL COMMENT '输入码(快捷输入用)',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='基础数据表';

ALTER TABLE doctor_basics ADD COLUMN `is_valid` smallint(6) DEFAULT NULL COMMENT '逻辑删除字段, -1 表示删除' AFTER type_name;
ALTER TABLE doctor_basics ADD COLUMN `context` VARCHAR(64) DEFAULT NULL COMMENT '基础数据内容' AFTER srm;

-- 2016-06-28 doctor_pig_tracks 表增加 pig_type 猪类冗余字段
ALTER TABLE doctor_pig_tracks ADD COLUMN `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类型(公猪，母猪， 仔猪)' AFTER pig_id;

ALTER TABLE doctor_change_reasons ADD COLUMN `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id' AFTER id;

-- 2016-07-04 groupEvent表增加更新信息字段
ALTER TABLE doctor_group_events ADD COLUMN `updated_at` datetime DEFAULT NULL COMMENT '更新时间' AFTER creator_name;
ALTER TABLE doctor_group_events ADD COLUMN `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id' AFTER updated_at;
ALTER TABLE doctor_group_events ADD COLUMN `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name' AFTER updator_id;

alter table doctor_user_subs
add column `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名 (冗余),跟随 user_profile 表的 real_name 字段' after `user_name`;

update doctor_user_subs o
set o.real_name = (select i.realname from parana_user_profiles i where i.user_id = o.user_id);

-- 20160-07-06 doctor_pig_track 表增加消息提醒字段
ALTER TABLE doctor_pig_tracks
ADD COLUMN `extra_message` text DEFAULT NULL COMMENT '每只猪的消息提醒' AFTER `extra`;

-- 2016-07-16 新建基础物料表
DROP TABLE IF EXISTS `doctor_basic_materials`;
CREATE TABLE `doctor_basic_materials` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` smallint(6) DEFAULT NULL COMMENT '物料类型',
  `name` varchar(128) DEFAULT NULL COMMENT '物料名称',
  `srm` varchar(32) DEFAULT NULL,
  `unit_group_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位组id',
  `unit_group_name` varchar(64) DEFAULT NULL COMMENT '单位组名称',
  `unit_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位id',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '单位名称',
  `default_consume_count` int(11) DEFAULT NULL COMMENT '默认消耗数量',
  `price` bigint(20) DEFAULT NULL COMMENT '价格(元)',
  `remark` text COMMENT '标注',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='基础物料表';

-- 2016-07-19 猪场日报表
CREATE TABLE `doctor_daily_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `data` text COMMENT '日报数据，json存储',
  `extra` text COMMENT '附加字段',
  `sum_at` date DEFAULT NULL COMMENT '统计时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间(仅做记录创建时间，不参与查询)',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_daily_reports_farm_id_agg_sumat` (`farm_id`,`sum_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场日报表';

-- 数据迁移的数据源信息
CREATE TABLE `doctor_move_datasource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(128) DEFAULT NULL COMMENT '数据源名称',
  `username` varchar(32) DEFAULT NULL COMMENT '数据库用户名',
  `password` varchar(32) DEFAULT NULL COMMENT '数据库密码',
  `driver` varchar(32) DEFAULT NULL COMMENT 'jdbc driver',
  `url` varchar(128) DEFAULT NULL COMMENT '链接url',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='move-data数据源信息';

-- 添加灵宝融利的数据源信息
INSERT INTO `doctor_move_datasource` (`id`, `name`, `username`, `password`, `driver`, `url`)
VALUES
	(1, '灵宝融利', 'sa', 'pigmall', 'net.sourceforge.jtds.jdbc.Driver', 'jdbc:jtds:sqlserver://101.201.146.171:1433/lbrl;tds=8.0;lastupdatecount=true');

-- 2016-07-28 变动原因删除farm_id, 增加srm列
ALTER TABLE doctor_change_reasons DROP COLUMN farm_id;
ALTER TABLE doctor_change_reasons ADD COLUMN `srm` VARCHAR(64) DEFAULT NULL COMMENT 'reason字段的输入码' AFTER reason;

-- 增加当前配种次数
alter table `doctor_pig_tracks` add COLUMN `current_mating_count` int(11) DEFAULT 0 COMMENT '当前配种次数' after `current_parity`;

-- 2016-08-03 更新猪跟踪的 当前配种次数
update `doctor_pig_tracks` set current_mating_count = 1 where pig_type = 1 and status in (3,4,7,8,9);

-- 2016-08-09 工作流历史实例关联实例id
alter table workflow_history_process_instances add column `external_history_id` BIGINT(20) DEFAULT NULL COMMENT '记录删除的实例id' AFTER `parent_instance_id`;

-- 2016-08-10 生产月报冗余数据
-- 猪群事件表
ALTER TABLE doctor_group_events ADD COLUMN change_type_id BIGINT(20) DEFAULT NULL COMMENT '变动类型id' AFTER is_auto;
ALTER TABLE doctor_group_events ADD COLUMN price BIGINT(20) DEFAULT NULL COMMENT '销售单价(分)' AFTER change_type_id;
ALTER TABLE doctor_group_events ADD COLUMN amount BIGINT(20) DEFAULT NULL COMMENT '销售总额(分)' AFTER price;
ALTER TABLE doctor_group_events ADD COLUMN trans_group_type SMALLINT(6) DEFAULT NULL COMMENT '转群类型 0 内转 1 外转' AFTER amount;

-- 猪事件表
ALTER TABLE doctor_pig_events ADD COLUMN change_type_id BIGINT(20) DEFAULT NULL COMMENT '变动类型id' AFTER rel_event_id;
ALTER TABLE doctor_pig_events ADD COLUMN price BIGINT(20) DEFAULT NULL COMMENT '销售单价(分)' AFTER change_type_id;
ALTER TABLE doctor_pig_events ADD COLUMN amount BIGINT(20) DEFAULT NULL COMMENT '销售总额(分)' AFTER price;
ALTER TABLE doctor_pig_events ADD COLUMN pig_status_before SMALLINT(6) DEFAULT NULL COMMENT '事件发生之前猪的状态' AFTER amount;
ALTER TABLE doctor_pig_events ADD COLUMN pig_status_after SMALLINT(6) DEFAULT NULL COMMENT '事件发生之后猪的状态' AFTER pig_status_before;
ALTER TABLE doctor_pig_events ADD COLUMN parity INT(11) DEFAULT NULL COMMENT '事件发生母猪的胎次），记住是前一个事件是妊娠检查事件' AFTER pig_status_after;
ALTER TABLE doctor_pig_events ADD COLUMN is_impregnation SMALLINT(6) DEFAULT NULL COMMENT '是否可以进行受胎统计，就是妊娠检查阳性之后这个字段为true 0否1是' AFTER parity;
ALTER TABLE doctor_pig_events ADD COLUMN is_delivery SMALLINT(6) DEFAULT NULL COMMENT '是否可以进行分娩，就是分娩事件之后这个字段为true 0否1是' AFTER is_impregnation;
ALTER TABLE doctor_pig_events ADD COLUMN preg_days INT(11) DEFAULT NULL COMMENT '孕期，分娩时候统计' AFTER is_delivery;
ALTER TABLE doctor_pig_events ADD COLUMN feed_days INT(11) DEFAULT NULL COMMENT '哺乳天数，断奶事件发生统计' AFTER preg_days;
ALTER TABLE doctor_pig_events ADD COLUMN preg_check_result SMALLINT(6) DEFAULT NULL COMMENT '妊娠检查结果，从extra中拆出来' AFTER feed_days;
ALTER TABLE doctor_pig_events ADD COLUMN dp_npd INT(11) DEFAULT 0 COMMENT '断奶到配种的非生产天数' AFTER preg_check_result;
ALTER TABLE doctor_pig_events ADD COLUMN pf_npd INT(11) DEFAULT 0 COMMENT '配种到返情非生产天数' AFTER dp_npd;
ALTER TABLE doctor_pig_events ADD COLUMN pl_npd INT(11) DEFAULT 0 COMMENT '配种到流产非生产天数' AFTER pf_npd;
ALTER TABLE doctor_pig_events ADD COLUMN ps_npd INT(11) DEFAULT 0 COMMENT '配种到死亡非生产天数' AFTER pl_npd;
ALTER TABLE doctor_pig_events ADD COLUMN py_npd INT(11) DEFAULT 0 COMMENT '配种到阴性非生产天数' AFTER ps_npd;
ALTER TABLE doctor_pig_events ADD COLUMN pt_npd INT(11) DEFAULT 0 COMMENT '配种到淘汰非生产天数' AFTER py_npd;
ALTER TABLE doctor_pig_events ADD COLUMN jp_npd INT(11) DEFAULT 0 COMMENT '配种到配种非生产天数' AFTER pt_npd;
ALTER TABLE doctor_pig_events ADD COLUMN npd INT(11) DEFAULT 0 COMMENT '非生产天数 前面的总和' AFTER jp_npd;
ALTER TABLE doctor_pig_events ADD COLUMN live_count INT(11) DEFAULT NULL COMMENT '活仔数' AFTER npd;
ALTER TABLE doctor_pig_events ADD COLUMN health_count INT(11) DEFAULT NULL COMMENT '键仔数' AFTER live_count;
ALTER TABLE doctor_pig_events ADD COLUMN weak_count INT(11) DEFAULT NULL COMMENT '弱仔数' AFTER health_count;
ALTER TABLE doctor_pig_events ADD COLUMN mny_count INT(11) DEFAULT NULL COMMENT '木乃伊数' AFTER weak_count;
ALTER TABLE doctor_pig_events ADD COLUMN jx_count INT(11) DEFAULT NULL COMMENT '畸形数' AFTER mny_count;
ALTER TABLE doctor_pig_events ADD COLUMN dead_count INT(11) DEFAULT NULL COMMENT '死胎数' AFTER jx_count;
ALTER TABLE doctor_pig_events ADD COLUMN black_count INT(11) DEFAULT NULL COMMENT '黑胎数' AFTER dead_count;
ALTER TABLE doctor_pig_events ADD COLUMN wean_count INT(11) DEFAULT NULL COMMENT '断奶数' AFTER black_count;
ALTER TABLE doctor_pig_events ADD COLUMN wean_avg_weight DOUBLE DEFAULT NULL COMMENT '断奶均重(kg)' AFTER wean_count;

-- 2016-08-11 生产日报增加
ALTER TABLE doctor_daily_reports ADD COLUMN sow_count int(11) DEFAULT 0 COMMENT '母猪存栏' AFTER farm_name;
ALTER TABLE doctor_daily_reports ADD COLUMN farrow_count int(11) DEFAULT 0 COMMENT '产房仔猪存栏' AFTER sow_count;
ALTER TABLE doctor_daily_reports ADD COLUMN nursery_count int(11) DEFAULT 0 COMMENT '保育猪存栏' AFTER farrow_count;
ALTER TABLE doctor_daily_reports ADD COLUMN fatten_count int(11) DEFAULT 0 COMMENT '育肥猪存栏' AFTER nursery_count;

ALTER TABLE doctor_pig_events ADD COLUMN current_mating_count DOUBLE DEFAULT NULL COMMENT '当前配种次数' AFTER wean_avg_weight;

-- 月报报表
CREATE TABLE `doctor_monthly_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `data` text COMMENT '月报数据，json存储',
  `extra` text COMMENT '附加字段',
  `sum_at` date DEFAULT NULL COMMENT '统计时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间(仅做记录创建时间，不参与查询)',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_monthly_reports_farm_id_agg_sumat` (`farm_id`,`sum_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场月报表';


ALTER TABLE doctor_pig_events ADD COLUMN check_date datetime DEFAULT NULL COMMENT '检查时间' AFTER current_mating_count;
ALTER TABLE doctor_pig_events ADD COLUMN matting_date datetime DEFAULT NULL COMMENT '配种时间' AFTER check_date;
ALTER TABLE doctor_pig_events ADD COLUMN farrowing_date datetime DEFAULT NULL COMMENT '分娩时间' AFTER matting_date;
ALTER TABLE doctor_pig_events ADD COLUMN abortion_date datetime DEFAULT NULL COMMENT '流产时间' AFTER farrowing_date;
ALTER TABLE doctor_pig_events ADD COLUMN partwean_date datetime DEFAULT NULL COMMENT '断奶时间' AFTER abortion_date;
ALTER TABLE doctor_pig_events ADD COLUMN doctor_mate_type SMALLINT(6) DEFAULT NULL COMMENT '配种类型' AFTER partwean_date;

-- 2016-08-15 workflow 新增 itimer 字段
ALTER TABLE workflow_definition_nodes ADD COLUMN `itimer` VARCHAR(128) DEFAULT NULL COMMENT '定时事件处理类(一般为类标识)' AFTER timer;

alter table doctor_material_consume_providers add column `unit_price` bigint(20) DEFAULT NULL COMMENT '本次出库/入库的单价。入库单价,由前台传入,直接保存即可；出库单价,需要计算: 先入库的先出库, 然后对涉及到的每一次入库的单价作加权平均。单位为“分”' after event_count;

-- 仓库物资数量类型有 Long 改为 Double
alter table doctor_material_consume_providers modify `event_count` decimal(23,3) DEFAULT NULL COMMENT '事件数量';
alter table doctor_material_price_in_ware_houses modify `remainder` decimal(23,3) NOT NULL COMMENT '本次入库量的剩余量，比如本次入库100个，那么就是这100个的剩余量，减少到0时删除';
alter table doctor_material_consume_avgs modify `consume_avg_count` decimal(23,3) DEFAULT NULL COMMENT '平均消耗数量';
alter table doctor_material_consume_avgs modify `consume_count` decimal(23,3) DEFAULT NULL COMMENT '最后一次领用数量';
alter table doctor_material_in_ware_houses modify `lot_number` decimal(23,3) DEFAULT NULL COMMENT '数量信息';
alter table doctor_farm_ware_house_types modify `lot_number` decimal(23,3) DEFAULT NULL COMMENT '类型原料的数量';
alter table doctor_ware_house_tracks modify `lot_number` decimal(23,3) DEFAULT NULL COMMENT '仓库物品的总数量信息';

-- 2016-08-18 workflow 新增 tracker 字段
alter table workflow_definition_node_events ADD Column `tacker` VARCHAR(128) DEFAULT NULL COMMENT '配种次数判断';

-- 2016-08-24 猪事件新增分娩总重
ALTER TABLE doctor_pig_events ADD COLUMN farrow_weight DOUBLE DEFAULT NULL COMMENT '分娩总重(kg)' AFTER npd;

-- 2016-08-29 增加后备猪统计字段
ALTER TABLE doctor_pig_type_statistics ADD COLUMN houbei int(11) DEFAULT NULL COMMENT '后备猪数' AFTER fatten;

-- 2016-08-31 //消息中增加操作id 与是否过期
Alter TABLE doctor_messages ADD COLUMN business_id bigint(20) DEFAULT NULL COMMENT '消息对应的操作id: 猪id、猪群id、物料id' AFTER template_name;
Alter TABLE doctor_messages ADD COLUMN is_expired SMALLINT (2) DEFAULT NULL COMMENT '消息是否过期: 0 未过期、1 过期' AFTER business_id;

-- 2016-09-05 猪事件表增加操作人字段
alter table doctor_pig_events
add column `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人id' after remark,
add column `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人' after operator_id;

-- 2016-09-12 增加猪群销售字段
ALTER TABLE doctor_group_events ADD COLUMN base_weight SMALLINT(6) DEFAULT NULL COMMENT '销售基础重量: 10, 15(kg)' AFTER avg_weight;
ALTER TABLE doctor_group_events ADD COLUMN over_price DOUBLE DEFAULT NULL COMMENT '超出价格(分/kg)' AFTER amount;

-- 2016-09-13 新增猪群批次总结表
CREATE TABLE `doctor_group_batch_summaries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `group_id` bigint(20) DEFAULT NULL COMMENT '猪群id',
  `group_code` varchar(512) DEFAULT NULL COMMENT '猪群号',
  `status` smallint(6) DEFAULT NULL COMMENT '枚举: 1:已建群, -1:已关闭',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类',
  `avg_day_age` int(11) DEFAULT NULL COMMENT '平均日龄',
  `open_at` datetime DEFAULT NULL COMMENT '建群时间',
  `close_at` datetime DEFAULT NULL COMMENT '关群时间',
  `barn_id` bigint(20) DEFAULT NULL COMMENT '猪舍id',
  `barn_name` varchar(64) DEFAULT NULL COMMENT '猪舍名称',
  `user_id` bigint(20) DEFAULT NULL COMMENT '工作人员id',
  `user_name` varchar(64) DEFAULT NULL COMMENT '工作人员name',
  `nest` smallint(6) DEFAULT NULL COMMENT '窝数',
  `live_count` int(11) DEFAULT NULL COMMENT '活仔数',
  `health_count` int(11) DEFAULT NULL COMMENT '健仔数',
  `weak_count` int(11) DEFAULT NULL COMMENT '弱仔数',
  `birth_cost` bigint(20) DEFAULT NULL COMMENT '出生成本(分)',
  `birth_avg_weight` double DEFAULT NULL COMMENT '出生均重(kg)',
  `dead_rate` double DEFAULT NULL COMMENT '死淘率',
  `wean_count` int(11) DEFAULT NULL COMMENT '断奶数',
  `unq_count` int(11) DEFAULT NULL COMMENT '不合格数',
  `wean_avg_weight` double DEFAULT NULL COMMENT '断奶均重(kg)',
  `sale_count` int(11) DEFAULT NULL COMMENT '销售头数',
  `sale_amount` bigint(20) DEFAULT NULL COMMENT '销售金额(分)',
  `to_nursery_cost` bigint(20) DEFAULT NULL COMMENT '转保育成本(分)',
  `to_nursery_count` int(11) DEFAULT NULL COMMENT '转保育数量',
  `to_nursery_avg_weight` double DEFAULT NULL COMMENT '转保育均重(kg)',
  `to_fatten_cost` bigint(20) DEFAULT NULL COMMENT '转育肥成本(分)',
  `to_fatten_count` int(11) DEFAULT NULL COMMENT '转育肥数量',
  `to_fatten_avg_weight` double DEFAULT NULL COMMENT '转育肥均重(kg)',
  `in_count` int(11) DEFAULT NULL COMMENT '转入数',
  `in_avg_weight` double DEFAULT NULL COMMENT '转入均重(kg)',
  `in_cost` bigint(20) DEFAULT NULL COMMENT '转入成本(均)',
  `fcr` double DEFAULT NULL COMMENT '料肉比',
  `out_cost` bigint(20) DEFAULT NULL COMMENT '出栏成本(分)',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_group_batch_summaries_group_id` (`group_id`),
  KEY `idx_doctor_group_batch_summaries_barn_id` (`barn_id`),
  KEY `idx_doctor_group_batch_summaries_farm_id` (`farm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪群批次总结表';

-- 2016-09-13 猪群跟踪新增统计字段
ALTER TABLE doctor_group_tracks ADD COLUMN wean_avg_weight double DEFAULT NULL COMMENT '断奶均重kg' AFTER avg_weight;
ALTER TABLE doctor_group_tracks ADD COLUMN birth_avg_weight double DEFAULT NULL COMMENT '出生均重kg' AFTER wean_avg_weight;
ALTER TABLE doctor_group_tracks ADD COLUMN weak_qty int(11) DEFAULT NULL COMMENT '弱仔数' AFTER birth_avg_weight;
ALTER TABLE doctor_group_tracks ADD COLUMN unwean_qty int(11) DEFAULT NULL COMMENT '未断奶数' AFTER weak_qty;
ALTER TABLE doctor_group_tracks ADD COLUMN unq_qty int(11) DEFAULT NULL COMMENT '不合格数' AFTER unwean_qty;

ALTER TABLE doctor_pig_tracks ADD COLUMN group_id bigint(20) DEFAULT NULL COMMENT '哺乳猪群id(断奶后此id置为null)' AFTER weight;
ALTER TABLE doctor_pig_tracks ADD COLUMN farrow_qty int(11) DEFAULT NULL COMMENT '分娩仔猪数' AFTER group_id;
ALTER TABLE doctor_pig_tracks ADD COLUMN unwean_qty int(11) DEFAULT NULL COMMENT '未断奶数量' AFTER farrow_qty;
ALTER TABLE doctor_pig_tracks ADD COLUMN wean_qty int(11) DEFAULT NULL COMMENT '断奶数量' AFTER unwean_qty;
ALTER TABLE doctor_pig_tracks ADD COLUMN farrow_avg_weight double DEFAULT NULL COMMENT '分娩均重(kg)' AFTER wean_qty;
ALTER TABLE doctor_pig_tracks ADD COLUMN wean_avg_weight double DEFAULT NULL COMMENT '断奶均重(kg)' AFTER farrow_avg_weight;

alter table doctor_pig_events add column boar_code varchar(64) default null comment '配种的公猪' after doctor_mate_type;
create index doctor_pig_events_boar_code on doctor_pig_events(boar_code);

-- 2016年09月13日 基础物料表增加逻辑删除字段
alter table doctor_basic_materials
add column is_valid smallint(6) NOT NULL DEFAULT 1 COMMENT '逻辑删除字段, -1 表示删除, 1 表示可用' after srm;

-- 2016年09月13日, 把物资变动关联的猪舍从 extra 中拆出来
alter table doctor_material_consume_providers
add column `barn_id` bigint(20) unsigned DEFAULT NULL COMMENT '领用物资的猪舍, 仅 event_type=1 时才会有值' after staff_name,
add column `barn_name` varchar(64) DEFAULT NULL COMMENT '猪舍名称' after barn_id;

-- 2016年09月13日, 基础物料表新增字段 "子类别"
alter table doctor_basic_materials
add column sub_type bigint(20) unsigned DEFAULT NULL COMMENT '物料的子类别，关联 doctor_basics 表的id' after `type`;

-- 2016-09-14 胎次产仔分析月报
CREATE TABLE `doctor_parity_monthly_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `parity` int(11) DEFAULT NULL COMMENT '胎次',
  `nest_count` int(11) DEFAULT NULL COMMENT '总窝数',
  `percent` double DEFAULT NULL COMMENT '占比',
  `farrow_all` int(11) DEFAULT NULL COMMENT '总产仔',
  `farrow_avg` double DEFAULT NULL COMMENT '平均产仔',
  `sum_at` varchar(32) DEFAULT NULL COMMENT '统计时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `doctor_parity_monthly_reports_id` (`id`),
  KEY `doctor_parity_monthly_reports_farm_id` (`farm_id`),
  KEY `doctor_parity_monthly_reports_parity` (`parity`),
  KEY `doctor_parity_monthly_reports_sum_at` (`sum_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT='胎次产仔分析月报';

-- 2016-09-18 猪群新增转入类型, 猪舍id字段
ALTER TABLE doctor_group_events ADD COLUMN in_type smallint(6) DEFAULT NULL COMMENT '仔猪转入事件: 转入类型' AFTER trans_group_type;
ALTER TABLE doctor_group_events ADD COLUMN other_barn_id bigint(20) DEFAULT NULL COMMENT '猪群转入转出事件的来源/目标id' AFTER in_type;
ALTER TABLE doctor_group_events ADD COLUMN other_barn_type varchar(64) DEFAULT NULL COMMENT '猪群转入转出事件的来源/目标猪舍类型' AFTER other_barn_id;

-- 2016年09月14日, 物资变动关联猪群
alter table doctor_material_consume_providers
add column group_id bigint(20) DEFAULT NULL COMMENT '领用物资的猪群Id, 仅 event_type=1 时才会有值' after barn_name,
add column group_code varchar(640) DEFAULT NULL COMMENT '猪群名称' after group_id;

-- 2016-9-21
ALTER TABLE doctor_messages ADD COLUMN event_type INT (11) DEFAULT NULL comment '需要操作的事件类型' after   `type`;
-- 2016-09-22 增加索引
create index doctor_pig_events_type on doctor_pig_events(type);
create index doctor_pig_events_parity on doctor_pig_events(parity);
create index doctor_pigs_is_removal on doctor_pigs(is_removal);
create index doctor_pig_tracks_current_parity on doctor_pig_tracks(current_parity);

-- 2016-09-27 公猪生产成绩月报
DROP table if exists `doctor_boar_monthly_reports`;
create table `doctor_boar_monthly_reports` (
 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
 `farm_id` bigint(20) NULL comment '猪场id',
 `boar_code` varchar(60) NULL comment '公猪号',
 `pz_count` int(11) NULL comment '配种次数',
 `spmz_count` int(11) NULL comment '首配母猪头数',
 `st_count` int(11) NULL comment '受胎头数',
 `cmz_count` int(11) NULL comment '产仔母猪头数',
 `pjcz_count` double NULL comment '平均产仔数',
 `pjchz_count` double NULL comment '平均产活仔数',
 `st_rate` double NULL comment '受胎率',
 `fm_rate` double NULL comment '分娩率',
 `sum_at` varchar(32) NULL comment '汇总时间',
 `created_at` datetime default null,
 `updated_at` datetime default null,
 primary key(id)
) comment '公猪生产成绩月报';
create index doctor_boar_monthly_reports_farm_id on doctor_boar_monthly_reports(farm_id);
create index doctor_boar_monthly_reports_boar_code on doctor_boar_monthly_reports(boar_code);
create index doctor_boar_monthly_reports_sum_at on doctor_boar_monthly_reports(sum_at);

-- 2016-09-20 猪事件增加是否是自动生成标识
ALTER TABLE doctor_pig_events ADD COLUMN is_auto smallint(6) DEFAULT NULL COMMENT '是否是自动生成事件, 0 不是, 1 是' AFTER pig_code;

-- 2016-09-22 回滚日志表增加
ALTER TABLE doctor_revert_logs ADD COLUMN farm_id bigint(20) DEFAULT NULL COMMENT '猪场id' AFTER id;
ALTER TABLE doctor_revert_logs ADD COLUMN group_id bigint(20) DEFAULT NULL COMMENT '猪群id' AFTER farm_id;
ALTER TABLE doctor_revert_logs ADD COLUMN pig_id bigint(20) DEFAULT NULL COMMENT '猪id' AFTER group_id;

ALTER TABLE doctor_group_events ADD COLUMN rel_group_event_id bigint(20) DEFAULT NULL COMMENT '关联猪群事件id' AFTER other_barn_type;
ALTER TABLE doctor_group_events ADD COLUMN rel_pig_event_id bigint(20) DEFAULT NULL COMMENT '关联猪事件id' AFTER rel_group_event_id;

-- 2016-09-23 猪增加字段，关联猪群事件
ALTER TABLE doctor_pig_events ADD COLUMN rel_group_event_id BIGINT(20) DEFAULT NULL COMMENT '关联猪群事件id(比如转种猪事件)' AFTER rel_event_id;
ALTER TABLE doctor_pig_events ADD COLUMN rel_pig_event_id BIGINT(20) DEFAULT NULL COMMENT '关联猪事件id(比如拼窝事件)' AFTER rel_group_event_id;

CREATE INDEX idx_doctor_group_events_rel_group_event_id ON doctor_group_events(rel_group_event_id);
CREATE INDEX idx_doctor_group_events_rel_pig_event_id ON doctor_group_events(rel_pig_event_id);
CREATE INDEX idx_doctor_pig_events_rel_group_event_id ON doctor_pig_events(rel_group_event_id);
CREATE INDEX idx_doctor_pig_events_rel_pig_event_id ON doctor_pig_events(rel_pig_event_id);
CREATE INDEX idx_doctor_group_events_other_barn_id ON doctor_group_events(other_barn_id);

-- 2016-09-29 物资入库时可选择的厂家
CREATE TABLE `doctor_material_factorys` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `farm_id` bigint(20) unsigned NOT NULL,
  `farm_name` varchar(64) DEFAULT NULL,
  `factory_name` varchar(64) NOT NULL DEFAULT '',
  `extra` text,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `farm_id` (`farm_id`,`factory_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='物资入库时可选择的厂家';

alter table doctor_material_consume_providers
add column `provider_factory_id` bigint(20) unsigned DEFAULT NULL COMMENT '供货厂家id' after group_code,
add column `provider_factory_name` varchar(64) DEFAULT NULL COMMENT '供货厂家' after provider_factory_id;

-- 2016-10-12 优化消息
-- 用户消息权限表
DROP TABLE IF EXISTS `doctor_message_user`;
CREATE TABLE IF NOT EXISTS `doctor_message_user` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`user_id` BIGINT(20) DEFAULT NULL COMMENT '用户id',
	`message_id` BIGINT(20) DEFAULT NULL COMMENT '消息id',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '关联的操作对象id',
	`status_sys` SMALLINT(6) DEFAULT NULL COMMENT '状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败',
	`status_sms` SMALLINT(6) DEFAULT NULL COMMENT '状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败',
	`status_email` SMALLINT(6) DEFAULT NULL COMMENT '状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败',
	`status_app` SMALLINT(6) DEFAULT NULL COMMENT '状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY (`id`)
	)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户消息关联表';
	CREATE INDEX idx_message_user_id ON doctor_message_user(`user_id`);

	ALTER TABLE `doctor_messages`
	DROP COLUMN `user_id`,
	DROP COLUMN `status`,
	DROP COLUMN `channel`,
	DROP COLUMN `is_expired`,
	DROP COLUMN `content`,
	DROP COLUMN `sended_at`,
	DROP COLUMN `failed_by`;

-- 2016-10-10 猪群批次总结增加日增重
ALTER TABLE doctor_group_batch_summaries ADD COLUMN daily_weight_gain DOUBLE DEFAULT NULL COMMENT '日增重(kg)' AFTER unq_count;

alter table doctor_user_data_permissions modify column  `barn_ids` text COMMENT '猪舍ids, 逗号分隔';

-- 2016-10-19
ALTER TABLE doctor_message_user ADD COLUMN rule_value_id BIGINT(20) DEFAULT NULL COMMENT '规则值id' AFTER business_id;

-- 2016年10月19日， 由于猪的类别枚举发生变化，需要刷新已有的字段
update doctor_groups set pig_type = 7 where pig_type = 1;
update doctor_groups set pig_type = 3 where pig_type = 10;
update doctor_groups set pig_type = 4 where pig_type = 8;

update doctor_barns set pig_type = 7 where pig_type = 1;
update doctor_barns set pig_type = 3 where pig_type = 10;
update doctor_barns set pig_type = 4 where pig_type = 8;

update doctor_vaccination_pig_warns set pig_type = 7 where pig_type = 1;
update doctor_vaccination_pig_warns set pig_type = 3 where pig_type = 10;
update doctor_vaccination_pig_warns set pig_type = 4 where pig_type = 8;
-- 2016-10-25
ALTER TABLE doctor_messages ADD COLUMN rule_value_id BIGINT(20) DEFAULT NULL COMMENT '规则值id' AFTER business_id;

-- 2016-10-26 修改消息用户关联表索引
DROP INDEX idx_message_user_id ON doctor_message_user;
CREATE INDEX   idx_userid_farmid_templateid ON doctor_message_user(user_id, farm_id, template_id);

-- 2016-11-16 在message_id 字段添加索引
CREATE INDEX   idx_messageid ON doctor_message_user(message_id);

-- 2016-11-21 增加猪场id与基础数据关联表
CREATE TABLE `doctor_farm_basics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` BIGINT(20) DEFAULT NULL COMMENT '猪场id',
  `basic_ids` TEXT COMMENT '基础数据ids, 逗号分隔',
  `reason_ids` TEXT COMMENT '变动原因ids, 逗号分隔',
  `extra` TEXT COMMENT '附加字段',
  `created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_farm_basics_farm_id` (`farm_id`)
) ENGINE=INNODB  DEFAULT CHARSET=utf8 COMMENT='猪场基础数据关联表';

-- 2016-11-18 新建周报表
CREATE TABLE `doctor_weekly_reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `data` text COMMENT '周报数据，json存储',
  `extra` text COMMENT '附加字段',
  `sum_at` date DEFAULT NULL COMMENT '统计时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间(仅做记录创建时间，不参与查询)',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_monthly_reports_farm_id_agg_sumat` (`farm_id`,`sum_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场周报表';

-- 2016-12-05 猪群跟踪字段重构
ALTER TABLE doctor_group_tracks DROP COLUMN customer_id;
ALTER TABLE doctor_group_tracks DROP COLUMN customer_name;
ALTER TABLE doctor_group_tracks DROP COLUMN price;
ALTER TABLE doctor_group_tracks DROP COLUMN weight;
ALTER TABLE doctor_group_tracks DROP COLUMN avg_weight;
ALTER TABLE doctor_group_tracks DROP COLUMN amount;
ALTER TABLE doctor_group_tracks DROP COLUMN sale_qty;

ALTER TABLE doctor_group_tracks CHANGE unq_qty  qua_qty INT(11)  COMMENT '合格数';
ALTER TABLE doctor_group_tracks CHANGE wean_avg_weight  wean_weight DOUBLE COMMENT '断奶重kg';
ALTER TABLE doctor_group_tracks CHANGE birth_avg_weight  birth_weight DOUBLE COMMENT '出生重kg';

ALTER TABLE doctor_group_tracks ADD COLUMN unq_qty INT(11) DEFAULT NULL COMMENT '不合格数(分娩时累加)' AFTER qua_qty;
ALTER TABLE doctor_group_tracks ADD COLUMN nest INT(11) DEFAULT NULL COMMENT '窝数(分娩时累加)' AFTER birth_weight;
ALTER TABLE doctor_group_tracks ADD COLUMN live_qty INT(11) DEFAULT NULL COMMENT '活仔数(分娩时累加)' AFTER nest;
ALTER TABLE doctor_group_tracks ADD COLUMN healthy_qty INT(11) DEFAULT NULL COMMENT '健仔数(分娩时累加)' AFTER live_qty;
ALTER TABLE doctor_group_tracks ADD COLUMN wean_qty INT(11) DEFAULT NULL COMMENT '断奶数(断奶时累加)' AFTER unwean_qty;

-- 2016-12-15 增加org_ids 字段
ALTER TABLE doctor_user_data_permissions ADD COLUMN org_ids text DEFAULT NULL COMMENT '公司id,逗号分隔';
ALTER TABLE doctor_messages ADD COLUMN business_type INT(11) DEFAULT NULL COMMENT '消息目标类型,1、猪 2、猪群 3、仓库' AFTER business_id;

-- 2016-12-28
ALTER TABLE doctor_pig_events ADD COLUMN group_id BIGINT(20) DEFAULT NULL COMMENT '哺乳状态的母猪关联的猪群id' AFTER npd;
CREATE INDEX idx_doctor_pig_events_group_id ON doctor_pig_events(group_id);

-- 2017-01-09
alter table doctor_farm_basics add column material_ids TEXT default null comment '物料基础数据ids' after reason_ids;

-- 2017-01-12
ALTER TABLE doctor_pig_tracks ADD COLUMN current_barn_type smallint(6) DEFAULT NULL COMMENT '猪舍类型' after current_barn_name;

-- 2017-01-18
ALTER TABLE doctor_pigs ADD COLUMN boar_type SMALLINT(6) DEFAULT NULL COMMENT '公猪类型: 1 活公猪, 2 冷冻精液, 3 新鲜精液' AFTER genetic_name;

-- 2017-01-20
alter table doctor_messages add column event_at DATE default null comment '事件发生日期' after `data`;
alter table doctor_messages add column other_at DATE default null comment '其他事件发生日期' after `event_at`;
alter table doctor_messages add column rule_time_diff INT(11) default null comment '规则已过多少天' after other_at;
alter table doctor_messages add column time_diff INT(11) default null comment '事件已过天数' after rule_time_diff;
alter table doctor_messages add column barn_id bigint(20) default null comment '产生消息时猪舍id' after time_diff;
alter table doctor_messages add column barn_name VARCHAR (64) default null comment '产生消息时猪舍名' after barn_id;
alter table doctor_messages add column status smallint (6) default null comment '产生消息时消息对象的状态' after barn_name;
alter table doctor_messages add column status_name VARCHAR (64) default null comment '产生消息时消息对象的状态名' after status;
alter table doctor_messages add column operator_id bigint(20) default null comment '事件操作人id' after status_name;
alter table doctor_messages add column operator_name VARCHAR(64) default null comment '事件操作人' after operator_id;
alter table doctor_messages add column reason VARCHAR(64) default null comment '消息产生的原因' after operator_name;
alter table doctor_messages add column code VARCHAR(64) default null comment '消息对象的code' after reason;
alter table doctor_messages add column parity int(11) default null comment '母猪胎次' after code;

-- 2017-02-06
alter table doctor_messages add column ware_house_id bigint(20) default null comment '仓库id' after parity;
alter table doctor_messages add column ware_house_name VARCHAR(64) default null comment '仓库名' after ware_house_id;
alter table doctor_messages add column lot_number double default null comment '物料剩余量' after ware_house_name;

-- 2017-02-16 用户相关重构
drop table doctor_service_review_tracks;

ALTER TABLE doctor_staffs
  DROP COLUMN org_name,
  DROP COLUMN role_id,
  DROP COLUMN role_name,
  DROP COLUMN sex,
  DROP COLUMN avatar,
  DROP COLUMN out_id,
  DROP COLUMN extra,
  DROP COLUMN creator_id,
  DROP COLUMN creator_name,
  DROP COLUMN updator_id,
  DROP COLUMN updator_name;

ALTER TABLE doctor_staffs CHANGE org_id farm_id BIGINT(20);

-- 2017-02-14
alter table doctor_messages add column quantity  int(11) default null comment '猪只数' after lot_number;
alter table doctor_messages add column avg_day_age  int(11) default null comment '猪群平均日龄' after quantity;

-- 2017-03-03
alter table doctor_user_subs add column farm_id BIGINT(20) default null comment '猪场id' after parent_user_id;
alter table doctor_sub_roles add column farm_id BIGINT(20) default null comment '关联猪场id,猪场拥有的角色' after user_id;
alter table doctor_user_primarys add column rel_farm_id BIGINT(20) default null comment '此账号关联猪场id' after user_name;

-- 2017-03-05
alter table doctor_user_primarys add column `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名 (冗余),跟随 user_profile 表的 real_name 字段' after `user_name`;
alter table doctor_farms add column `farm_code` VARCHAR(64) DEFAULT NULL COMMENT '猪场号' after `name`;

-- 2017-03-21 事件编辑相关
drop table if exists `doctor_event_modify_requests`;
CREATE TABLE `doctor_event_modify_requests` (
`id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`farm_id`    bigint(20) NOT NULL COMMENT '猪场ID',
`business_id`  bigint(20) NOT NULL COMMENT '目标ID',
`business_code` varchar(64) NOT NULL COMMENT '目标code',
`start_event_id` bigint(20) DEFAULT NULL COMMENT '上一个事件',
`event_id`   bigint(20) NOT NULL COMMENT '处理的事件ID',
`type`       tinyint(4) NOT NULL COMMENT '处理的事件类型1：猪事件，2：猪群事件',
`content`    text NOT NULL COMMENT '事件类型',
`status`     tinyint(4) NOT NULL COMMENT '状态，0：待处理，1：处理中，2：处理成功， -1：处理失败',
`reason`     varchar(255) DEFAULT NULL COMMENT '失败原因',
`user_id`    bigint(20) NOT NULL COMMENT '操作人ID',
`user_name`  varchar(64) NOT NULL COMMENT '操作人名称',
`created_at` datetime NOT NULL COMMENT '创建时间',
`updated_at` datetime NOT NULL COMMENT '更新事件',
primary key(id),
key `doctor_event_modify_requests_farm_id` (farm_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件编辑请求表';

alter table doctor_event_modify_requests add column error_stack text  Default null comment '没有具体错误原因是保存错误堆栈' after reason;


alter table doctor_group_events add column status tinyint(4)  not null comment '是否有效1：有效，0：正在处理， -1：无效' after out_id ;

alter table doctor_pig_events add column status tinyint(4)  not null comment '是否有效1：有效，0：正在处理， -1：无效' after out_id ;

alter table doctor_pig_tracks add column `current_event_id` bigint(20) NOT NULL COMMENT '最新的事件ID' after rel_event_ids ;

alter table doctor_pig_snapshots drop column org_id, drop column farm_id;

alter table doctor_pig_snapshots add column from_event_id bigint(20) NOT NULL COMMENT '操作前的事件ID' after pig_id;
alter table doctor_pig_snapshots change event_id to_event_id bigint(20) NOT NULL COMMENT '操作后的事件ID';
alter table doctor_pig_snapshots change pig_info to_pig_info text COMMENT '猪快照信息';

alter table doctor_group_snapshots drop column event_type, drop column from_group_id;
alter table doctor_group_snapshots change to_group_id group_id bigint(20) NOT NULL COMMENT '猪群ID';
alter table doctor_group_snapshots drop column from_info;


drop table if exists `doctor_event_relations`;
create table doctor_event_relations (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `origin_event_id` bigint(20) not null comment '原事件id',
  `trigger_event_id` bigint(20) not null comment '触发事件id',
  `trigger_target_type` tinyint(4) not null comment '1猪事件、2猪群事件',
  `status` tinyint(4) not null comment '1有效、0正在处理、 -1无效',
  `created_at` datetime not null,
  `updated_at` datetime not null,
  primary key (id),
  key idx_relations_origin_event_id(origin_event_id),
  key idx_relations_trigger_event_id(trigger_event_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件关联表';

-- 2017-03-23 导入记录表
drop table if exists `doctor_farm_exports`;
create table doctor_farm_exports (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场名',
  `farm_name` VARCHAR(40)  DEFAULT NULL COMMENT '猪场名',
  `url` VARCHAR(255)  DEFAULT NULL COMMENT '导入的文件地址',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  key idx_farm_exports_farm_id(farm_id)

)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='导入猪场记录表';


-- 2017-03-24 导入状态
ALTER TABLE doctor_farm_exports ADD COLUMN status tinyint(4) DEFAULT NULL COMMENT 'Excel导入状态, ' after url;


-- 2017-03-25 修改数据权限表farm_ids类型
ALTER TABLE doctor_user_data_permissions MODIFY COLUMN farm_ids TEXT DEFAULT NULL COMMENT '猪场ids,逗号分隔';

-- 2017-03-26 校验猪群数量
drop table if exists `doctor_group_info_checks`;
create table `doctor_group_info_checks`(
`id` bigint(20) unsigned not null auto_increment,
`farm_id` bigint(20) default null comment '猪场id',
`farm_name` varchar(64) default null comment '猪场名称',
`group_id` bigint(20) default null comment '猪群id',
`group_code` varchar(512) default null comment '猪群code',
`event_count` int(11) default null comment '根据时间算出的猪群数量',
`track_count` int(11) default null comment 'track中的数量',
`sum_at` date default null comment '日期',
`status` tinyint(4) default null comment '状态，-1：track数据不正确，0：正在处理，1：track数据正确， 2：已处理正确',
`extra` text default null comment '',
`remark` varchar(256) default null comment '备注',
`created_at` datetime DEFAULT NULL COMMENT '创建时间',
`creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
`creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
`updated_at` datetime DEFAULT NULL COMMENT '更新时间',
`updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
`updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
primary key(`id`),
key `idx_doctor_group_info_checks_farm_id` (`farm_id`),
key `idx_doctor_group_info_checks_group_id` (`group_id`)
)CHARSET=utf8 COMMENT='猪群数据校验表';

-- 2017-03-29 猪track推演记录表
drop table if exists `doctor_pig_elicit_records`;
create table `doctor_pig_elicit_records`(
`id` bigint(20) unsigned not null auto_increment,
`farm_id` bigint(20) default null comment '猪场id',
`farm_name` varchar(64) default null comment '猪场名称',
`pig_id` bigint(20) default null comment '猪id',
`pig_code` varchar(512) default null comment '猪code',
`status` tinyint(4) default null comment '状态，-1：错误，1：成功',
`from_track` text default null comment '原track',
`to_track` text default null comment '推演后track',
`error_reason` text default null comment '推演时错误原因',
`version` int(11) NOT NULL DEFAULT 1 comment '版本',
`created_at` datetime DEFAULT NULL COMMENT '创建时间',
primary key(`id`),
key `idx_doctor_pig_elicit_records_farm_id` (`farm_id`),
key `idx_doctor_pig_elicit_records_pig_id` (`pig_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪数据推演记录表';

-- 2017-03-30 猪群track推演记录表
drop table if exists `doctor_group_elicit_records`;
create table `doctor_group_elicit_records`(
`id` bigint(20) unsigned not null auto_increment,
`farm_id` bigint(20) default null comment '猪场id',
`farm_name` varchar(64) default null comment '猪场名称',
`group_id` bigint(20) default null comment '猪群id',
`group_code` varchar(512) default null comment '猪群code',
`status` tinyint(4) default null comment '状态，-1：错误，1：成功',
`from_track` text default null comment '原track',
`to_track` text default null comment '推演后track',
`error_reason` text default null comment '推演时错误原因',
`version` int(11) NOT NULL DEFAULT 1 comment '版本',
`created_at` datetime DEFAULT NULL COMMENT '创建时间',
primary key(`id`),
key `idx_doctor_group_elicit_records_farm_id` (`farm_id`),
key `idx_doctor_group_elicit_records_group_id` (`group_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪群数据推演记录表';

-- 2017-03-28 增加事件来源字段
ALTER TABLE doctor_farms ADD COLUMN  source tinyint(4) DEFAULT NULL COMMENT '来源,1:软件录入,2:excel导入,3:旧软件迁移，' after out_id;
ALTER TABLE doctor_pig_events ADD COLUMN event_source tinyint(4)  DEFAULT NULL COMMENT '事件来源,1、软件录入,2、excel导入,3、旧场迁移' after status;
ALTER TABLE doctor_group_events ADD COLUMN event_source tinyint(4)  DEFAULT NULL COMMENT '事件来源,1、软件录入,2、excel导入,3、旧场迁移' after status;

-- 修复2017-03-20之前excel导入的母猪,进场胎次多减了1
update doctor_farms set source = 3 where out_id is not null;
update doctor_farms set source = 2 where out_id is null;
-- 迁移
update doctor_pig_events set event_source = 3 where out_id  is not null;
-- excel导入
update doctor_pig_events a, doctor_farms b
set a.event_source = 2
where a.farm_id = b.id
and b.source = 2
and a.created_at <= date_add(b.created_at, INTERVAL 2 minute);
-- 系统录入
update doctor_pig_events set event_source = 1 where event_source is null;

-- 猪群的事件来源
update doctor_group_events
set event_source = 2
where id in (select id from
(select a.* from
  (select id, created_at from doctor_farms where source = 2) b
  left join doctor_group_events a
  on a.farm_id = b.id
  where a.farm_id not in (2,81)
  and type =2
  and a.created_at <= date_add(b.created_at, INTERVAL 3 minute)
  and is_auto = 1
  -- and event_source is null
  and extra is null
  or remark = 'excel导入'
  ) t1
  )
  ;
-- 导入的猪群事件的初始转入事件有谁触发
update doctor_group_events set rel_pig_event_id = -1, rel_group_event_id = null where pig_type = 7 and event_source = 2;
update doctor_group_events set rel_group_event_id = -1 , rel_pig_event_id = null where pig_type <> 7 and event_source = 2;

-- 2017-04-01 关联关系
drop table if exists `doctor_event_relations`;
CREATE TABLE `doctor_event_relations` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `origin_pig_event_id` bigint(20) DEFAULT NULL COMMENT '原猪事件id',
  `origin_group_event_id` bigint(20) DEFAULT NULL COMMENT '原猪群事件id',
  `trigger_pig_event_id` bigint(20) DEFAULT NULL COMMENT '触发猪事件id',
  `trigger_group_event_id` bigint(20) DEFAULT NULL COMMENT '触发猪事件id',
  `status` tinyint(4) NOT NULL COMMENT '1有效、0正在处理、 -1无效',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_relations_origin_pig_event_id` (`origin_pig_event_id`),
  KEY `idx_relations_origin_group_event_id` (`origin_group_event_id`),
  KEY `idx_relations_trigger_pig_event_id` (`trigger_pig_event_id`),
  KEY `idx_relations_trigger_group_event_id` (`trigger_group_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件关联表';