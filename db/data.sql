-- Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.

INSERT INTO
  parana_users
  (`name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `created_at`, `updated_at`)
VALUES
  ('admin','admin@terminus.io', '18888888888', '9f8c@a97758b955efdaf60fe4', 1, 1, null, '["ADMIN(OWNER)","ADMIN"]', '{"good":"man"}', now(), now());

INSERT INTO `parana_message_templates` (`id`, `creator_id`, `creator_name`, `name`, `title`, `content`, `context`, `channel`, `disabled`, `description`, `created_at`, `updated_at`)
VALUES
	(1, 1, 'admin', 'user.register.code', '用户中心手机注册码', '您的注册手机验证码是：{{code}}', '{\"code\":\"123456\"}', 1, 0, '用户中心手机注册码', '2016-05-17 17:08:43', '2016-05-17 17:08:43');


INSERT INTO `parana_configs` (`biz_type`, `key`, `value`, `data_type`, `group`, `description`, `created_at`, `updated_at`)
VALUES
	(0, 'msg.umeng.appKey', 'appKey', 'string', '0', 'app消息推送网关umeng的key', now(), now()),
	(0, 'msg.umeng.appSecret', 'appSecret', 'string', '0', 'app消息推送网关umeng的secret', now(), now()),
	(0, 'msg.sendcloud.user', 'user', 'string', '0', '邮件网关sendcloud的user', now(), now()),
	(0, 'msg.sendcloud.batchUser', 'batchUser', 'string', '0', '邮件网关sendcloud的batchUser', now(), now()),
	(0, 'msg.sendcloud.triggerUser', 'triggerUser', 'string', '0', '邮件网关sendcloud的triggerUser', now(), now()),
	(0, 'msg.sendcloud.key', 'key', 'string', '0', '邮件网关sendcloud的key', now(), now()),
	(0, 'msg.alisms.appKey', 'appKey', 'string', '0', '短信网关alidayu的key', now(), now()),
	(0, 'msg.alisms.appSecret', 'appSecret', 'string', '0', '短信网关alidayu的secret', now(), now()),
	(0, 'msg.current.email.service', 'sendCloudEmailService', 'string', '0', '当前的邮件网关', now(), now()),
	(0, 'msg.current.sms.service', 'aliSmsService', 'string', '0', '当前的短信网关', now(), now()),
	(0, 'msg.current.app.push.service', 'umengAppPushService', 'string', '0', '当前的app消息推送网关', now(), now());

insert into `parana_configs`
(`biz_type`, `key`, `value`, `data_type`, `group`, `description`, created_at, updated_at)
values
(0, 'system.pigmall.password', 'pigmall', 'string', 0, '第三方用户登录pigmall系统的接口密码', NOW(), now()),
(0, 'system.pigmall.corp.id', '1', 'string', 0, '本系统在pigmall系统的corp_id', NOW(), now()),
(0, 'system.pigmall.domain', 'http://www.pigmall.com', 'string', 0, 'pigmall系统的完整域名', NOW(), now()),
(0, 'system.neverest.password', 'neverest', 'string', 0, '第三方用户登录neverest系统的接口密码', NOW(), now()),
(0, 'system.neverest.corp.id', '1', 'string', 0, '本系统在neverest系统的corp_id', NOW(), now()),
(0, 'system.neverest.domain', 'http://www.neverest.com', 'string', 0, 'neverest系统的完整域名', NOW(), now());


-- 测试数据
INSERT INTO `parana_addresses` (`id`, `pid`, `name`, `level`, `pinyin`, `english_name`, `unicode_code`, `order_no`)
VALUES (1,0,'中国',0,'zhong guo','China','\\u4e2d\\u56fd','');

INSERT INTO `parana_addresses` (`id`, `pid`, `name`, `level`, `pinyin`, `english_name`, `unicode_code`, `order_no`)
VALUES
  (110000,1,'北京',1,'bei jing','','\\u5317\\u4eac',''),
  (330000,1,'浙江省',1,'zhe jiang sheng','','\\u6d59\\u6c5f\\u7701','');

INSERT INTO `parana_addresses` (`id`, `pid`, `name`, `level`, `pinyin`, `english_name`, `unicode_code`, `order_no`)
VALUES
	(330100,330000,'杭州市',2,'hang zhou shi','','\\u676d\\u5dde\\u5e02',''),
	(330102,330100,'上城区',3,'shang cheng qu','','\\u4e0a\\u57ce\\u533a',''),
	(330103,330100,'下城区',3,'xia cheng qu','','\\u4e0b\\u57ce\\u533a',''),
	(330104,330100,'江干区',3,'jiang gan qu','','\\u6c5f\\u5e72\\u533a',''),
	(330105,330100,'拱墅区',3,'gong shu qu','','\\u62f1\\u5885\\u533a',''),
	(330106,330100,'西湖区',3,'xi hu qu','','\\u897f\\u6e56\\u533a',''),
	(330108,330100,'滨江区',3,'bin jiang qu','','\\u6ee8\\u6c5f\\u533a',''),
	(330109,330100,'萧山区',3,'xiao shan qu','','\\u8427\\u5c71\\u533a',''),
	(330110,330100,'余杭区',3,'yu hang qu','','\\u4f59\\u676d\\u533a',''),
	(330122,330100,'桐庐县',3,'tong lu xian','','\\u6850\\u5e90\\u53bf',''),
	(330127,330100,'淳安县',3,'chun an xian','','\\u6df3\\u5b89\\u53bf',''),
	(330182,330100,'建德市',3,'jian de shi','','\\u5efa\\u5fb7\\u5e02',''),
	(330183,330100,'富阳市',3,'fu yang shi','','\\u5bcc\\u9633\\u5e02',''),
	(330185,330100,'临安市',3,'lin an shi','','\\u4e34\\u5b89\\u5e02',''),
	(330186,330100,'其它区',3,'qi ta qu','','\\u5176\\u5b83\\u533a',''),
	(330200,330000,'宁波市',2,'ning bo shi','','\\u5b81\\u6ce2\\u5e02',''),
	(330203,330200,'海曙区',3,'hai shu qu','','\\u6d77\\u66d9\\u533a',''),
	(330204,330200,'江东区',3,'jiang dong qu','','\\u6c5f\\u4e1c\\u533a',''),
	(330205,330200,'江北区',3,'jiang bei qu','','\\u6c5f\\u5317\\u533a',''),
	(330206,330200,'北仑区',3,'bei lun qu','','\\u5317\\u4ed1\\u533a',''),
	(330211,330200,'镇海区',3,'zhen hai qu','','\\u9547\\u6d77\\u533a',''),
	(330212,330200,'鄞州区',3,'yin zhou qu','','\\u911e\\u5dde\\u533a',''),
	(330225,330200,'象山县',3,'xiang shan xian','','\\u8c61\\u5c71\\u53bf',''),
	(330226,330200,'宁海县',3,'ning hai xian','','\\u5b81\\u6d77\\u53bf',''),
	(330281,330200,'余姚市',3,'yu yao shi','','\\u4f59\\u59da\\u5e02',''),
	(330282,330200,'慈溪市',3,'ci xi shi','','\\u6148\\u6eaa\\u5e02',''),
	(330283,330200,'奉化市',3,'feng hua shi','','\\u5949\\u5316\\u5e02',''),
	(330284,330200,'其它区',3,'qi ta qu','','\\u5176\\u5b83\\u533a','');

INSERT INTO `parana_addresses` (`id`, `pid`, `name`, `level`, `pinyin`, `english_name`, `unicode_code`, `order_no`)
VALUES
  (330108001,330108,'西兴街道',4,'xi xing jie dao','','\\u897f\\u5174\\u8857\\u9053',''),
	(330108002,330108,'长河街道',4,'zhang he jie dao','','\\u957f\\u6cb3\\u8857\\u9053',''),
	(330108003,330108,'浦沿街道',4,'pu yan jie dao','','\\u6d66\\u6cbf\\u8857\\u9053','');

-- 消息规则模板初始化
INSERT INTO `doctor_message_rule_templates`
(name, type, category, rule_value, status, message_template, content, producer, `describe`, created_at, updated_at, updated_by)
VALUES
('待配种提醒', 1, 1, '{"values":[{"id":1, "ruleType":1,"value":7, "describe":"断奶、流产、返情日期间隔(天)"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}', 1, 'msg.sow.breed', null, 'sowBreedingProducer', '待配种母猪提示', now(), now(), null),
('待配种警示', 2, 1,'{"values":[{"id":1, "ruleType":1,"value":21, "describe":"断奶、流产、返情日期间隔(天)"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}', 1, 'msg.sow.breed', null, 'sowBreedingProducer', '待配种母猪警报', now(), now(), null),
('妊娠检查提醒', 1, 2,'{"values":[{"id":1, "ruleType":2,"leftValue":19,"rightValue":25, "describe":"母猪已配种时间间隔(天)"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.preg.check', null, 'sowPregCheckProducer', '母猪需妊娠检查提示', now(), now(), null),
('妊娠转入提醒', 1, 3,'{"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.preg.home', null, 'sowPregHomeProducer', '母猪需转入妊娠舍提示', now(), now(), null),
('预产提醒', 1, 4,'{"values":[{"id":1, "ruleType":1,"value":7, "describe":"预产期提前多少天提醒"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.birth.date', null, 'sowBirthDateProducer', '母猪预产期提示', now(), now(), null),
('断奶提醒', 1, 5,'{"values":[{"id":1, "ruleType":1,"value":21, "describe":"母猪分娩日期起的天数"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶提示', now(), now(), null),
('断奶警示', 2, 5,'{"values":[{"id":1, "ruleType":1,"value":35, "describe":"母猪分娩日期起的天数"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶警报', now(), now(), null),
('母猪淘汰提醒', 1, 6,'{"values":[{"id":1, "ruleType":1, "value":10, "describe":"胎次"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.eliminate', null, 'sowEliminateProducer', '母猪应淘汰提示', now(), now(), null),
('公猪淘汰提醒', 1, 7,'{"values":[{"id":1, "ruleType":1, "value":20, "describe":"公猪配种次数"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.boar.eliminate', null, 'boarEliminateProducer', '公猪应淘汰提示', now(), now(), null),
('免疫提醒', 1, 8,'{"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.pig.vaccination', null, 'pigVaccinationProducer', '猪只免疫提示', now(), now(), null),
('产仔警示', 2, 10,'{"values":[{"id":1, "ruleType":1,"value":120, "describe":"母猪配种日期起的天数"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.sow.not.litter', null, 'sowNotLitterProducer', '母猪未产仔警报', now(), now(), null),
('库存提醒', 1, 9,'{"values":[{"id":1, "ruleType":1,"value":7, "describe":"库存量"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足提示', now(), now(), null),
('库存警示', 2, 9,'{"values":[{"id":1, "ruleType":1,"value":3, "describe":"库存量"}],"frequence":24,"channels":"0,1,2,3","url":"/entry/message-detail"}',1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足警报', now(), now(), null);

-- 发送消息模板
INSERT INTO `parana_message_templates` (`creator_id`, `creator_name`, `name`, `title`, `content`, `context`, `channel`, `disabled`, `description`, `created_at`, `updated_at`)
VALUES
(1, 'admin', 'msg.sys.normal.sys',   '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 0, 0, '一般系统消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sys.normal.sms', 	 '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 1, 0, '一般系统消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sys.normal.email', '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 2, 0, '一般系统消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sys.normal.app',   '一般系统消息', '{{data}}', '{"data":"系统消息提示"}', 3, 0, '一般系统消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.sys',   '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪待配种消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.sms',   '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪待配种消息短息模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.email', '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪待配种消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.breed.app',   '母猪待配种消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行配种。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪待配种消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.sys',   '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪妊娠检查消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.sms',   '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪妊娠检查消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.email', '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪妊娠检查消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.check.app',   '母猪妊娠检查消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时进行妊娠检查。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪妊娠检查消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.sys',   '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪转舍消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.sms',   '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪转舍消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.email', '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪转舍消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.preg.home.app',   '母猪转舍消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 且尚未转舍, 应及时转舍。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪转舍消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.sys',   '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪预产消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.sms',   '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪预产消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.email', '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪预产消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.birth.date.app',   '母猪预产消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 即将抵达预产期时间, 预产期为{{judgePregDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪预产消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.sys',   '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪断奶消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.sms',   '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪断奶消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.email', '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪断奶消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.need.wean.app',   '母猪断奶消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 应及时断奶。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪断奶消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.sys',   '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪淘汰消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.sms',   '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪淘汰消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.email', '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪淘汰消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.eliminate.app',   '母猪淘汰消息', '{{pigCode}}母猪的胎次已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪淘汰消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.sys',   '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '公猪淘汰消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.sms',   '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '公猪淘汰消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.email', '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '公猪淘汰消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.boar.eliminate.app',   '公猪淘汰消息', '{{pigCode}}公猪的配种次数已达{{parity}}, 应需被淘汰。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '公猪淘汰消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.sys',   '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 0, 0, '母猪未产仔消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.sms',   '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 1, 0, '母猪未产仔消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.email', '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 2, 0, '母猪未产仔消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.sow.not.litter.app',   '母猪未产仔消息', '{{pigCode}}母猪处于{{statusName}}状态已经{{timeDiff}}天了, 还未产仔, 配种日期为{{matingDate}}。猪场为{{farmName}}, 猪舍为{{barnName}}。', '', 3, 0, '母猪未产仔消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.sys',   '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 0, 0, '仓库库存不足消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.sms',   '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 1, 0, '仓库库存不足消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.email', '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 2, 0, '仓库库存不足消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.warehouse.store.app',   '仓库库存不足消息', '{{wareHouseName}}仓库的{{materialName}}原料已经不足{{lotConsumeDay}}天, 剩余量为{{lotNumber}}。猪场为{{farmName}}。', '', 3, 0, '仓库库存不足消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.sys',   '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 0, 0, '猪只免疫消息消息站内模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.sms',   '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 1, 0, '猪只免疫消息消息短信模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.email', '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 2, 0, '猪只免疫消息消息邮件模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43'),
(1, 'admin', 'msg.pig.vaccination.app',   '猪只免疫消息', '{{#of pigType "4,5,6,7,8,9"}}{{#of vaccinationDateType "1"}}{{pigCode}}猪只日龄已经超过{{inputValue}}天, 当前日龄为{{dateAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{pigCode}}猪只应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{pigCode}}猪只体重已经超过{{inputValue}}kg, 当前体重为{{weight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "4"}}{{pigCode}}猪只转舍后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "6"}}{{pigCode}}猪只妊娠检查后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "7"}}{{pigCode}}猪只配种后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "8"}}{{pigCode}}猪只分娩后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "9"}}{{pigCode}}猪只断奶后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}{{#of pigType "1,2,3"}}{{#of vaccinationDateType "1"}}{{groupCode}}猪群平均日龄已经超过{{inputValue}}天, 当前日龄为{{avgDayAge}}, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "2"}}当前日期已经超过{{inputDate}}, {{groupCode}}猪群应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}} 猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "3"}}{{groupCode}}猪群平均体重已经超过{{inputValue}}kg, 当前体重为{{avgWeight}}kg, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{#of vaccinationDateType "5"}}{{groupCode}}猪群转群后已经超过{{inputValue}}天, 应及时免疫。使用疫苗{{materialName}}, 使用量为{{dose}}。{{#if vaccDate}}上次免疫时间为:{{vaccDate}}, {{/if}}猪场为{{farmName}}, 猪舍为{{barnName}}。{{/of}}{{/of}}', '', 3, 0, '猪只免疫消息消息推送模板', '2016-06-15 17:08:43', '2016-06-15 17:08:43');
