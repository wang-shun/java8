-- Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.

INSERT INTO
  parana_users
  (`name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `created_at`, `updated_at`)
VALUES
  ('jlchen','i@terminus.io', '18888888888', '9f8c@a97758b955efdaf60fe4', 2, 1, null, '{"seller":"haha"}', '{"good":"man"}', now(), now());


INSERT INTO `parana_message_templates` (`id`, `creator_id`, `creator_name`, `name`, `title`, `content`, `context`, `channel`, `disabled`, `description`, `created_at`, `updated_at`)
VALUES
	(1, 1, 'admin', 'user.register.code', '用户中心手机注册码', '{\"smsName\":\"大鱼测试\",\"smsTemplate\":\"SMS_5495133\",\"smsParam\":{\"code\":\"{{code}}\",\"product\":\"{{product}}\"}}', '{\"code\":\"123456\"}', 1, b'0', '用户中心手机注册码', now(), now());


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
