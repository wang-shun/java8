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

