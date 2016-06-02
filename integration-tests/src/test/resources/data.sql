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

-- warehouse content
-- ware house type
INSERT INTO `doctor_farm_ware_house_types` (`id`, `farm_id`, `farm_name`, `type`, `log_number`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'testFarm', 1, 10000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(2, 12345, 'testFarm', 2, 10000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(3, 12345, 'testFarm', 3, 10000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(4, 12345, 'testFarm', 4, 10000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(5, 12345, 'testFarm', 5, 10000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17');

-- doctor_ware_houses
INSERT INTO `doctor_ware_houses` (`id`, `ware_house_name`, `farm_id`, `farm_name`, `manager_id`, `manager_name`, `address`, `type`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 'wareHouseName1', 12345, 'farmIdName', 1, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(2, 'wareHouseName2', 12345, 'farmIdName', 1, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(3, 'wareHouseName3', 12345, 'farmIdName', 1, 'managerName', 'addressName', 2, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(4, 'wareHouseName4', 12345, 'farmIdName', 1, 'managerName', 'addressName', 2, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(5, 'wareHouseName5', 12345, 'farmIdName', 1, 'managerName', 'addressName', 3, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(6, 'wareHouseName6', 12345, 'farmIdName', 1, 'managerName', 'addressName', 3, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(7, 'wareHouseName7', 12345, 'farmIdName', 1, 'managerName', 'addressName', 4, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(8, 'wareHouseName8', 12345, 'farmIdName', 1, 'managerName', 'addressName', 4, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(9, 'wareHouseName9', 12345, 'farmIdName', 1, 'managerName', 'addressName', 5, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10'),
	(10, 'wareHouseName10', 12345, 'farmIdName', 1, 'managerName', 'addressName', 5, NULL, 1, 'craetorName', 1, 'updateName', '2016-06-02 16:01:10', '2016-06-02 16:01:10');

--