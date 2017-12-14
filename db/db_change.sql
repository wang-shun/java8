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
CREATE index idx_doctor_pig_events_event_at ON doctor_pig_events(event_at);

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

-- 2017-05-27
alter table doctor_range_reports add column `fatten_feed_conversion` double default null comment '育肥料肉比' after `mate_in_seven`;
alter table doctor_range_reports add column `nursery_feed_conversion` double default null comment '育肥料肉比' after `mate_in_seven`;


-- 2017-06-01
alter table doctor_feed_formulas drop index feed_id ;

-- 2017-06-05 猪群批次总结
alter table doctor_group_batch_summaries add column `vaccine_amount` double default null comment '疫苗金额' after `updated_at`;
alter table doctor_group_batch_summaries add column `medicine_amount` double default null comment '药品金额' after `vaccine_amount`;
alter table doctor_group_batch_summaries add column `consumables_amount` double default null comment '易耗品金额' after `medicine_amount`;
alter table doctor_group_batch_summaries add column `fend_number` double default null comment '饲料金额' after `consumables_amount`;
alter table doctor_group_batch_summaries add column `feed_amount` double default null comment '饲料数量' after `fend_number`;

-- 2017-06-06 报表新增字段
alter table doctor_daily_reports add column sow_ph_reserve_in int(11) default null comment '后备猪转入' after `updated_at`;
alter table doctor_daily_reports add column sow_ph_chg_farm_in int(11) default null comment '配怀舍转场转入' after `sow_ph_reserve_in`;
alter table doctor_daily_reports add column sow_cf_in_farm_in int(11) default null comment '产房转场转入' after `sow_ph_reserve_in`;
alter table doctor_daily_reports add column sow_ph_mating int(11) default null comment '配怀舍配种母猪' after `sow_cf_in_farm_in`;
alter table doctor_daily_reports add column sow_ph_konghuai int(11) default null comment '配怀舍空怀母猪' after `sow_ph_mating`;
alter table doctor_daily_reports add column sow_ph_pregnant int(11) default null comment '配怀舍怀孕母猪' after `sow_ph_konghuai`;

alter table doctor_daily_groups add column day_wean_count int(11) default null comment '日断奶数' after `updated_at`;
alter table doctor_daily_groups add column farrowing_in int(11) default null comment '分娩转入' after `day_wean_count`;

-- 2017-07-04
alter table doctor_messages add column `dose` int(11) default null comment '剂量' after `avg_day_age`;
alter table doctor_messages add column `material_id` bigint(20) default null comment '疫苗id' after `dose`;
alter table doctor_messages add column `material_name` VARCHAR(20) default null comment '疫苗名称' after `material_id`;

-- 2017-07-13
alter table doctor_range_reports add column `org_id` bigint(20) default null comment '公司id' after `farm_id`;
alter table doctor_range_reports modify column `farm_id` bigint(20) default null comment '猪场id' after `id`;

-- 2017-07-18
alter table doctor_orgs add column `parent_id` bigint(20) default null comment '父公司id' after `mobile`;
alter table doctor_orgs add column `type` smallint(4) default null comment '公司类型' after `parent_id`;
update doctor_orgs set parent_id = 0, type = 1;

-- 2017-07-21
ALTER table doctor_daily_reports modify column `fatten_feed_amount` bigint(20) DEFAULT NULL COMMENT '育肥猪饲料消耗金额';

--2017-08-28 仓库模块重构表结构

DROP TABLE IF EXISTS `doctor_warehouse_material_apply`;
CREATE TABLE `doctor_warehouse_material_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `material_handle_id` bigint(20) NOT NULL COMMENT '物料处理编号',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `warehouse_type` smallint(6) NOT NULL COMMENT '仓库类型',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名',
  `pig_barn_id` bigint(20) NOT NULL COMMENT '领用猪舍编号',
  `pig_barn_name` varchar(64) DEFAULT NULL COMMENT '领用猪舍名称',
  `pig_group_id` bigint(20) DEFAULT NULL COMMENT '领用猪群编号',
  `pig_group_name` varchar(64) DEFAULT NULL COMMENT '领用猪群名称',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `apply_date` datetime DEFAULT NULL COMMENT '领用日期',
  `apply_staff_name` varchar(64) DEFAULT NULL COMMENT '领用人',
  `apply_year` smallint(12) NOT NULL COMMENT '领用年',
  `apply_month` tinyint(4) NOT NULL COMMENT '领用月',
  `material_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `type` smallint(6) DEFAULT NULL COMMENT '物料类型，易耗品，原料，饲料，药品，饲料',
  `unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `quantity` decimal(23,2) NOT NULL COMMENT '数量',
  `unit_price` bigint(20) NOT NULL COMMENT '单价，单位分',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='仓库物料领用表';
DROP TABLE IF EXISTS `doctor_warehouse_material_handle`;
CREATE TABLE `doctor_warehouse_material_handle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `warehouse_type` smallint(6) DEFAULT NULL COMMENT '仓库类型',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `other_trasnfer_handle_id` bigint(20) DEFAULT NULL COMMENT '另一条调拨物料处理单的编号',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '物料供应商名称',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `material_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `type` tinyint(4) NOT NULL COMMENT '处理类别，入库，出库，调拨，盘点',
  `unit_price` bigint(20) NOT NULL COMMENT '单价，单位分',
  `unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `delete_flag` tinyint(2) DEFAULT NULL COMMENT '删除标志，0正常，1删除',
  `quantity` decimal(23,2) NOT NULL COMMENT '数量',
  `handle_date` datetime DEFAULT NULL COMMENT '处理日期',
  `handle_year` smallint(12) NOT NULL COMMENT '处理年',
  `handle_month` tinyint(4) NOT NULL COMMENT '处理月',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='仓库物料处理表';
DROP TABLE IF EXISTS `doctor_warehouse_purchase`;
CREATE TABLE `doctor_warehouse_purchase` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `warehouse_type` smallint(6) DEFAULT NULL COMMENT '仓库类型',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物料编号',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '物料供应商名称',
  `unit_price` bigint(20) DEFAULT NULL COMMENT '单价，单位分',
  `quantity` decimal(23,2) DEFAULT NULL COMMENT '数量',
  `handle_date` datetime DEFAULT NULL COMMENT '处理日期',
  `handle_year` smallint(12) DEFAULT NULL COMMENT '处理年',
  `handle_month` tinyint(4) DEFAULT NULL COMMENT '处理月份',
  `handle_quantity` decimal(23,2) DEFAULT NULL COMMENT '已出库的数量',
  `handle_finish_flag` tinyint(2) DEFAULT NULL COMMENT '是否该批入库已出库完。0出库完，1未出库完。handle_quantity=quantity就表示出库完',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COMMENT='仓库物料入库表';
DROP TABLE IF EXISTS `doctor_warehouse_stock`;
CREATE TABLE `doctor_warehouse_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `warehouse_type` smallint(6) DEFAULT NULL COMMENT '仓库类型，冗余，方便查询',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '物料供应商',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `manager_id` bigint(20) DEFAULT NULL COMMENT '管理员编号',
  `material_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物料编号',
  `quantity` decimal(23,2) DEFAULT NULL COMMENT '数量',
  `unit` varchar(45) DEFAULT NULL COMMENT '单位',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8 COMMENT='仓库物料库存表';

-- 2017-08-29 物料处理表添加操作人字段
ALTER TABLE `doctor_warehouse_material_handle`
ADD COLUMN `operator_id` BIGINT(20) NULL COMMENT '操作人编号' AFTER `handle_month`,
ADD COLUMN `operator_name` VARCHAR(64) NULL COMMENT '操作人名' AFTER `operator_id`;


-- 2017-08-30
ALTER TABLE doctor_group_tracks ADD COLUMN `close_at` datetime DEFAULT NULL  comment '猪群关闭事件(如果猪群关闭的话)' after updator_name;


-- 2017-09-05
ALTER table doctor_farm_exports ADD column `error_reason` text DEFAULT NULL COMMENT 'Excel导入错误原因' after status;


-- 2017-09-05 旧场迁移错误记录表
drop table if exists `doctor_farm_move_errors`;
create table `doctor_farm_move_errors` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `farm_name` VARCHAR(40)  DEFAULT NULL COMMENT '猪场名',
  `code` VARCHAR(255)  DEFAULT NULL COMMENT '猪/猪群code',
  `type` tinyint(4) DEFAULT NULL COMMENT '目标类型',
  `out_id` VARCHAR(255)  DEFAULT NULL COMMENT '源目标id',
  `event_name` VARCHAR(40)  DEFAULT NULL COMMENT '事件名称',
  `event_At` DATE  DEFAULT NULL COMMENT '事件时间',
  `event_out_id` VARCHAR(255)  DEFAULT NULL COMMENT '源事件id',
  `error` text  DEFAULT NULL COMMENT '错误原因',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`)

)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='旧场迁移错误记录表';

-- 添加物料编码表 2017-09-11
CREATE TABLE `doctor_material_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库编号',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物料编号',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '供应商名',
  `specification` varchar(64) DEFAULT NULL COMMENT '规格',
  `code` varchar(64) DEFAULT NULL COMMENT '编码',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='物料编码表';
-- 添加采购扣减明细表 2017-09-11
CREATE TABLE `doctor_warehouse_handle_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细编号',
  `material_purchase_id` bigint(20) NOT NULL COMMENT '物料采购记录编号',
  `material_handle_id` bigint(20) NOT NULL COMMENT '物料处理记录编号',
  `handle_year` smallint(6) DEFAULT NULL,
  `handle_month` tinyint(2) DEFAULT NULL,
  `quantity` decimal(23,2) NOT NULL COMMENT '处理数量',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `index_material_handle_id` (`material_handle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8 COMMENT='仓库物料处理明细';

-- 添加物料供应商表 2017-09-11
CREATE TABLE `doctor_material_vendor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `vendor_name` varchar(64) NOT NULL COMMENT '供应商名称',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='物料供应商表';

-- 添加库存处理表 2017-09-12
CREATE TABLE `doctor_warehouse_stock_handle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `farm_id` bigint(20) NOT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `serial_no` varchar(45) NOT NULL COMMENT '流水号',
  `handle_date` date NOT NULL COMMENT '处理日期',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_serial_no_warehouse_id` (`serial_no`,`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='库存处理表';
-- 物料处理表添加备注字段 2017-09-13
ALTER TABLE `doctor_warehouse_material_handle`
ADD COLUMN `remark` VARCHAR(64) NULL COMMENT '备注' AFTER `operator_name`;

-- 修改物料处理表处理日期为date类型
ALTER TABLE doctor_warehouse_material_handle MODIFY handle_date DATE COMMENT '处理日期';

-- 添加仓库物料月度余量统计表
CREATE TABLE `doctor_warehouse_stock_monthly` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `handle_year` smallint(6) NOT NULL COMMENT '处理年',
  `handle_month` tinyint(2) NOT NULL COMMENT '处理月',
  `balance_quantity` decimal(23,2) NOT NULL DEFAULT '0.00' COMMENT '余量',
  `balacne_amount` bigint(20) NOT NULL DEFAULT '0' COMMENT '余额',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `warehouse_id_year_month_material_id_index` (`warehouse_id`,`handle_year`,`handle_month`,`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='仓库物料月度结余表';

-- 仓库物料领用表的领用猪群名和领用日期字段修改 2017-09-30
ALTER TABLE doctor_warehouse_material_apply MODIFY pig_group_name VARCHAR(512) COMMENT '领用猪群名称';
ALTER TABLE doctor_warehouse_material_apply MODIFY apply_date DATE COMMENT '领用日期';

ALTER TABLE `doctor_daily_reports`
ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号' AFTER `sow_ph_chg_farm_in`;
ALTER TABLE `doctor_daily_groups`
ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号' AFTER `farrowing_in`;

-- 仓库添加sku表 2017-10-13
CREATE TABLE `doctor_warehouse_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司编号',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `name` varchar(128) NOT NULL COMMENT '物料名称',
  `code` varchar(64) NOT NULL COMMENT '编码,用于跨厂调拨',
  `srm` varchar(32) DEFAULT NULL COMMENT '短码,用于查询',
  `vendor_id` bigint(20) DEFAULT NULL COMMENT '供应商编号',
  `unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `specification` varchar(64) DEFAULT NULL COMMENT '规格',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='仓库物料表';

ALTER TABLE doctor_warehouse_stock CHANGE material_name sku_name VARCHAR(64) COMMENT '物料名称';
ALTER TABLE doctor_warehouse_stock CHANGE material_id sku_id BIGINT(20) COMMENT '物料编号';
ALTER TABLE doctor_warehouse_stock DROP vendor_name;
ALTER TABLE doctor_warehouse_stock DROP manager_id;
ALTER TABLE doctor_warehouse_stock DROP unit;


-- 添加厂商表，厂商-公司关系表，单位-公司关系表 2017-10-30
CREATE TABLE `doctor_warehouse_vendor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NOT NULL COMMENT '供应商名称',
  `short_name` varchar(32) DEFAULT NULL COMMENT '简称',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COMMENT='物料供应商表';
CREATE TABLE `doctor_warehouse_vendor_org` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司编号',
  `vendor_id` bigint(20) DEFAULT NULL COMMENT '供应商编号',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='sku供应商与公司关系表';
CREATE TABLE `doctor_warehouse_unit_org` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `org_id` BIGINT(20) NOT NULL COMMENT '公司编号',
  `unit_id` BIGINT(20) NOT NULL COMMENT '单位编号',
  `created_at` DATETIME NULL,
  `updated_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `index_org` (`org_id` ASC));

-- 厂商表添加删除标志字段 2017-10-30
ALTER TABLE doctor_warehouse_vendor ADD delete_flag TINYINT NULL COMMENT '删除标志，0正常，1删除';
ALTER TABLE doctor_warehouse_vendor
  MODIFY COLUMN delete_flag TINYINT COMMENT '删除标志，0正常，1删除' AFTER short_name;

-- sku表添加status字段 2017-10-31
ALTER TABLE doctor_warehouse_sku ADD status TINYINT NULL COMMENT '状态';

-- 物料处理明细表添加批次号 2017-10-31
ALTER TABLE doctor_warehouse_material_handle ADD handle_no VARCHAR(64) NULL COMMENT '物料处理批次号';
ALTER TABLE doctor_warehouse_material_handle
  MODIFY COLUMN handle_no VARCHAR(64) COMMENT '物料处理批次号' AFTER id;

-- 修改物料处理明细表调入仓库编号 2017-10-31
ALTER TABLE doctor_warehouse_material_handle CHANGE other_trasnfer_handle_id other_transfer_handle_id BIGINT(20) COMMENT '另一条调拨物料处理单的编号';

-- 库存处理表添加字段 2017-10-31
ALTER TABLE doctor_warehouse_stock_handle ADD handle_sub_type TINYINT NULL COMMENT '事件子类型';
ALTER TABLE doctor_warehouse_stock_handle ADD handle_type TINYINT NULL COMMENT '事件类型';
ALTER TABLE doctor_warehouse_stock_handle ADD operator_name VARCHAR(64) NULL COMMENT '创建人名';
ALTER TABLE doctor_warehouse_stock_handle ADD operator_id BIGINT(20) NULL COMMENT '创建人';
ALTER TABLE doctor_warehouse_stock_handle ADD warehouse_name VARCHAR(64) NULL COMMENT '仓库名';
ALTER TABLE doctor_warehouse_stock_handle
  MODIFY COLUMN updated_at DATETIME AFTER handle_sub_type,
  MODIFY COLUMN created_at DATETIME AFTER handle_sub_type,
  MODIFY COLUMN warehouse_name VARCHAR(64) COMMENT '仓库名' AFTER warehouse_id;

-- 添加默认值 2017-10-31
ALTER TABLE doctor_warehouse_material_handle MODIFY delete_flag TINYINT(2) DEFAULT 1 COMMENT '删除标志';
ALTER TABLE doctor_warehouse_sku ALTER COLUMN status SET DEFAULT 1;
ALTER TABLE doctor_warehouse_vendor MODIFY delete_flag TINYINT(4) DEFAULT 1 COMMENT '删除标志';

-- sku表添加item字段
ALTER TABLE doctor_warehouse_sku ADD item_id BIGINT(20) NULL COMMENT '物料类型编号';
ALTER TABLE doctor_warehouse_sku ADD item_name VARCHAR(128) NULL COMMENT '基础物料名称';
ALTER TABLE doctor_warehouse_sku ADD type SMALLINT(6) NULL COMMENT '基础物料类型';

-- 物料处理表的批次号字段修改 2017-10-31
ALTER TABLE doctor_warehouse_material_handle CHANGE handle_no stock_handle_id BIGINT(20) COMMENT '库存处理ID';

-- 添加物料与公司关系表 2017-11-3
CREATE TABLE `doctor_warehouse_item_org` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `item_id` bigint(20) NOT NULL COMMENT '物料类目编号',
  `org_id` bigint(20) NOT NULL COMMENT '公司编号',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_org` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='物料公司关系表';

-- 添加仓库类型 2017-11-07
ALTER TABLE doctor_warehouse_stock_handle ADD warehouse_type TINYINT(4) NULL COMMENT '仓库类型';
ALTER TABLE doctor_warehouse_material_handle ADD before_inventory_quantity DECIMAL(23,2) NULL COMMENT '盘点前库存数量';


-- 物料领用添加领用类型 2017-11-28
ALTER TABLE `doctor_warehouse_material_apply`
ADD COLUMN `apply_type` TINYINT(4) NOT NULL COMMENT '领用类型。0猪舍，1猪群，2母猪' AFTER `updated_at`;

-- 物料领用添加领用人编号 2017-12-08
ALTER TABLE `doctor_warehouse_material_apply`
ADD COLUMN `apply_staff_id` BIGINT(20) NULL COMMENT '领用人编号' AFTER `apply_date`;

-- 物料月度统计添加处理日期 2017-12-10
ALTER TABLE `doctor_warehouse_stock_monthly`
ADD COLUMN `handle_date` DATE NULL COMMENT '处理日期' AFTER `updated_at`;

-- 标识处猪群转场触发的转入，刷新历史数据
update
doctor_group_events a left join doctor_group_events b on a.rel_group_event_id = b.id
set a.in_type = 5, a.extra = replace(replace(a.extra, '"inType":3', '"inType":5'), '群间转移', '转场转入')
where a.type = 2 and b.type = 9 and a.status = 1;

-- 新的猪群报表 2017-12-12
CREATE TABLE `doctor_group_dailies` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) NOT NULL COMMENT '猪场id',
  `pig_type` tinyint(4) NOT NULL COMMENT '猪群类型',
  `sum_at` date NOT NULL COMMENT '日期',
  `start` int(11) DEFAULT NULL COMMENT '期初',
  `turn_into` int(11) DEFAULT NULL COMMENT '转入数量',
  `chg_farm_in` int(11) DEFAULT NULL COMMENT '转场转入数量',
  `turn_into_weight` double DEFAULT NULL COMMENT '转入总重',
  `turn_into_age` int(11) DEFAULT NULL COMMENT '转入总日龄',
  `chg_farm` int(11) DEFAULT NULL COMMENT '转场数量',
  `chg_farm_weight` double DEFAULT NULL COMMENT '转场总重',
  `sale` int(11) DEFAULT NULL COMMENT '销售',
  `sale_weight` double DEFAULT NULL COMMENT '销售总重',
  `dead` int(11) DEFAULT NULL COMMENT '死亡',
  `weed_out` int(11) DEFAULT NULL COMMENT '淘汰',
  `other_change` int(11) DEFAULT NULL COMMENT '其他变动减少',
  `to_nursery` int(11) DEFAULT NULL COMMENT '转保育',
  `to_fatten` int(11) DEFAULT NULL COMMENT '转育肥',
  `to_fatten_weight` double DEFAULT NULL COMMENT '转育肥总重',
  `to_houbei` int(11) DEFAULT NULL COMMENT '转后备',
  `to_houbei_weight` double DEFAULT NULL COMMENT '转后备总重',
  `turn_seed` int(11) DEFAULT NULL COMMENT '转种猪',
  `turn_out_weight` double DEFAULT NULL COMMENT '转出总重',
  `end` int(11) DEFAULT NULL COMMENT '期末',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新事件',
  `version` int(11) DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_daily_groups_farm_id_type_sum_at` (`farm_id`,`pig_type`,`sum_at`),
  KEY `idx_doctor_group_dailies_farm_id` (`farm_id`),
  KEY `idx_doctor_group_dailies_sum_at` (`sum_at`)
) DEFAULT CHARSET=utf8 COMMENT='猪群相关的指标';

create table doctor_pig_dailies (
  id bigint(20) NOT NULL COMMENT 'id',
  sum_at date NOT NULL COMMENT '日期（yyyy-MM-dd）',
  farm_id bigint(20) NOT NULL COMMENT '猪场',
  sow_ph_start int(11) DEFAULT NULL COMMENT '配怀母猪期初头数',
  sow_ph_reserve_in int(11) DEFAULT NULL COMMENT '后备转入',
  sow_ph_wean_in int(11) DEFAULT NULL COMMENT '配怀母猪断奶转入',
  sow_ph_entry_in int(11) DEFAULT NULL COMMENT '配怀母猪进场',
  sow_ph_chg_farm_in int(11) DEFAULT NULL COMMENT '转场转入',
  sow_ph_dead int(11) DEFAULT NULL COMMENT '配怀死亡母猪',
  sow_ph_weed_out int(11) DEFAULT NULL COMMENT '配怀淘汰母猪',
  sow_ph_sale int(11) DEFAULT NULL COMMENT '配怀母猪销售',
  sow_ph_chg_farm int(11) DEFAULT NULL COMMENT '配怀母猪转场',
  sow_ph_other_out int(11) DEFAULT NULL COMMENT '配怀母猪其他减少',
  mating_count int(11) DEFAULT NULL COMMENT '配种数',
  preg_positive int(11) DEFAULT NULL COMMENT '妊娠检查阳性头数',
  preg_negative int(11) DEFAULT NULL COMMENT '妊娠检查阴性头数',
  preg_fanqing int(11) DEFAULT NULL COMMENT '妊娠检查返情头数',
  preg_liuchan int(11) DEFAULT NULL COMMENT '妊娠检查流产头数',
  sow_ph_end int(11) DEFAULT NULL COMMENT '配怀母猪期末头数',
  sow_cf_start int(11) DEFAULT NULL COMMENT '产房母猪期初存栏',
  sow_cf_end int(11) DEFAULT NULL COMMENT '产房母猪期末存栏',
  sow_cf_in int(11) DEFAULT NULL COMMENT '产房母猪，从配怀转入',
  sow_cf_in_farm_in int(11) DEFAULT NULL COMMENT '产房母猪其他转入 = 转场转入的数量',
  sow_cf_dead int(11) DEFAULT NULL COMMENT '产房母猪死亡数量',
  sow_cf_weed_out int(11) DEFAULT NULL COMMENT '产房母猪淘汰数量',
  sow_cf_sale int(11) DEFAULT NULL COMMENT '产房母猪销售数量',
  sow_cf_chg_farm int(11) DEFAULT NULL COMMENT '产房母猪转场数量',
  sow_cf_other_out int(11) DEFAULT NULL COMMENT '产房母猪其他减少数量',
  farrow_nest int(11) DEFAULT NULL COMMENT '产房分娩窝数',
  farrow_health int(11) DEFAULT NULL COMMENT '产房产健仔数',
  farrow_weak int(11) DEFAULT NULL COMMENT '产房产弱仔数',
  farrow_dead int(11) DEFAULT NULL COMMENT '产房死胎数',
  farrowjmh int(11) DEFAULT NULL COMMENT '产房黑木畸数',
  weight int(11) DEFAULT NULL COMMENT '产房窝重之和',
  wean_nest int(11) DEFAULT NULL COMMENT '断奶窝数',
  wean_qualified_count int(11) DEFAULT NULL COMMENT '断奶合格数',
  wean_count int(11) DEFAULT NULL COMMENT '断奶仔猪数',
  wean_day_age double DEFAULT NULL COMMENT '每头猪的断奶日龄之和',
  wean_weight double DEFAULT NULL COMMENT '每一窝的断奶均重之和',
  boar_start int(11) DEFAULT NULL COMMENT '公猪期初存栏',
  boar_in int(11) DEFAULT NULL COMMENT '公猪转入数量',
  boar_dead int(11) DEFAULT NULL COMMENT '公猪死亡数量',
  boar_weed_out int(11) DEFAULT NULL COMMENT '公猪淘汰数量',
  boar_sale int(11) DEFAULT NULL COMMENT '公猪销售数量',
  boar_other_out int(11) DEFAULT NULL COMMENT '公猪其他减少',
  boar_end int(11) DEFAULT NULL COMMENT '公猪期末存栏',
  updated_at date DEFAULT NULL COMMENT '更新日期',
  created_at date DEFAULT NULL COMMENT '创建日期',
  version int(11) DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_daily_pigs_farm_id_sum_at` (`farm_id`,`sum_at`),
  KEY `idx_doctor_pig_dailies_farm_id` (`farm_id`),
  KEY `idx_doctor_pig_dailies_sum_at` (`sum_at`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='猪相关报表';