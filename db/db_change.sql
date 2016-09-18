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
