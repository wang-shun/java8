-- Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.

INSERT INTO
  parana_users
  (`id`, `name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `created_at`, `updated_at`)
VALUES
  (1, 'admin','admin@terminus.io', '18888888888', '9f8c@a97758b955efdaf60fe4', 1, 1, null, null, '{"good":"man"}', now(), now());
INSERT INTO
  `parana_users` (`id`, `name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `item_info_md5`, `created_at`, `updated_at`)
VALUES
	(2, 'primary', NULL, '18888888889', '9f8c@a97758b955efdaf60fe4', 5, 1, '["PRIMARY(OWNER)","PRIMARY"]', NULL, NULL, NULL, '2016-05-26 19:21:25', '2016-06-02 13:34:36');
INSERT INTO `parana_users` (`id`, `name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `item_info_md5`, `created_at`, `updated_at`)
VALUES
	(3, '18888888890@18888888889', NULL, NULL, '9f8c@a97758b955efdaf60fe4', 6, 1, '["SUB","SUB(SUB(1))"]', '{"seller":"haha"}', '{"good":"man"}', NULL, '2016-05-17 17:08:43', '2016-06-02 12:28:15');

INSERT INTO `parana_user_profiles` (`id`, `user_id`, `realname`, `gender`, `province_id`, `province`, `city_id`, `city`, `region_id`, `region`, `street`, `extra_json`, `avatar`, `birth`, `created_at`, `updated_at`)
VALUES
	(1, 3, '测试真实姓名', 1, 10001, '', NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, '2016-06-06 00:00:00', '2016-06-06 00:00:00');

INSERT
  INTO `doctor_user_primarys` (`id`, `user_id`, `user_name`, `status`, `extra_json`, `created_at`, `updated_at`)
VALUES
	(1, 2, '18888888889', 1, NULL, '2016-05-26 19:21:25', '2016-05-26 19:21:25');


INSERT INTO `doctor_user_subs` (`id`, `user_id`, `user_name`, `parent_user_id`, `parent_user_name`, `role_id`, `role_name`, `contact`, `status`, `extra_json`, `created_at`, `updated_at`)
VALUES
	(1, 3, '18888888890@18888888889', 2, '18888888889', 1, '测试', '18888888890', 1, NULL, '2016-06-02 00:00:00', '2016-06-02 00:00:00');


INSERT INTO `doctor_sub_roles` (`id`, `name`, `desc`, `user_id`, `app_key`, `status`, `extra_json`, `allow_json`, `created_at`, `updated_at`)
VALUES
	(1, '测试', '测试', 2, 'MOBILE', 1, NULL, '["manage_back_category"]', '2016-06-02 00:00:00', '2016-06-02 00:00:00');



INSERT INTO `parana_message_templates` (`id`, `creator_id`, `creator_name`, `name`, `title`, `content`, `context`, `channel`, `disabled`, `description`, `created_at`, `updated_at`)
VALUES
	(1, 1, 'admin', 'user.register.code', '用户中心手机注册码', '您的注册手机验证码是：{{code}}', '{"code":"123456"}', 1, 0, '用户中心手机注册码', '2016-05-17 17:08:43', '2016-05-17 17:08:43');


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
	(1, 12345, 'testFarm', 1, 10000000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(2, 12345, 'testFarm', 2, 10000000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(3, 12345, 'testFarm', 3, 10000000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(4, 12345, 'testFarm', 4, 10000000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(5, 12345, 'testFarm', 5, 10000000, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17');

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

-- doctor material info
INSERT INTO `doctor_material_infos` (`id`, `farm_id`, `farm_name`, `type`, `material_name`, `remark`, `unit_group_id`, `unit_group_name`, `unit_id`, `unit_name`, `default_consume_count`, `price`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'farmName', 1, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(2, 12345, 'farmName', 1, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(3, 12345, 'farmName', 1, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(4, 12345, 'farmName', 1, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(5, 12345, 'farmName', 2, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(6, 12345, 'farmName', 2, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(7, 12345, 'farmName', 2, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(8, 12345, 'farmName', 2, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(9, 12345, 'farmName', 3, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(10, 12345, 'farmName', 3, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(11, 12345, 'farmName', 3, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(12, 12345, 'farmName', 3, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(13, 12345, 'farmName', 4, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(14, 12345, 'farmName', 4, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(15, 12345, 'farmName', 4, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(16, 12345, 'farmName', 4, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(17, 12345, 'farmName', 5, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(18, 12345, 'farmName', 5, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(19, 12345, 'farmName', 5, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17'),
	(20, 12345, 'farmName', 5, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100000, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-06-02 16:43:17', '2016-06-02 16:43:17');

--material in ware house info
INSERT INTO `doctor_material_in_ware_houses` (`id`, `farm_id`, `farm_name`, `ware_house_id`, `ware_house_name`, `material_id`, `material_name`, `type`, `lot_number`, `unit_group_name`, `unit_name`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'farmName', 1, 'warehouseName1', 1, 'materialName1', 1, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(2, 12345, 'farmName', 1, 'warehouseName1', 5, 'materialName2', 1, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(3, 12345, 'farmName', 2, 'warehouseName2', 5, 'materialName3', 1, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(4, 12345, 'farmName', 2, 'warehouseName2', 6, 'materialName4', 1, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(5, 12345, 'farmName', 3, 'warehouseName3', 5, 'materialName5', 2, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(6, 12345, 'farmName', 3, 'warehouseName3', 6, 'materialName6', 2, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(7, 12345, 'farmName', 4, 'warehouseName4', 5, 'materialName7', 2, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(8, 12345, 'farmName', 4, 'warehouseName4', 6, 'materialName8', 2, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(9, 12345, 'farmName', 5, 'warehouseName5', 6, 'materialName9', 3, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(10, 12345, 'farmName', 5, 'warehouseName5', 10, 'materialName10', 3, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(11, 12345, 'farmName', 6, 'warehouseName6', 11, 'materialName11', 3, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(12, 12345, 'farmName', 6, 'warehouseName6', 12, 'materialName12', 3, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(13, 12345, 'farmName', 7, 'warehouseName7', 13, 'materialName13', 4, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(14, 12345, 'farmName', 7, 'warehouseName7', 14, 'materialName14', 4, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(15, 12345, 'farmName', 8, 'warehouseName8', 15, 'materialName15', 4, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(16, 12345, 'farmName', 8, 'warehouseName8', 16, 'materialName16', 4, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(17, 12345, 'farmName', 9, 'warehouseName9', 17, 'materialName17', 5, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(18, 12345, 'farmName', 9, 'warehouseName9', 18, 'materialName18', 5, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(19, 12345, 'farmName', 10, 'warehouseName10', 19, 'materialName19', 5, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32'),
	(20, 12345, 'farmName', 10, 'warehouseName10', 20, 'materialName20', 5, 2500000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-06-02 16:57:32', '2016-06-02 16:57:32');

-- warehouse track info
INSERT INTO `doctor_ware_house_tracks` (`ware_house_id`, `farm_id`, `farm_name`, `manager_id`, `manager_name`, `material_lot_number`, `lot_number`, `is_default`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'farmName', 1, 'managerName', NULL, 5000000, 1, '{"1":2500000,"5":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(2, 12345, 'farmName', 2, 'managerName', NULL, 5000000, 1, '{"5":2500000,"6":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(3, 12345, 'farmName', 3, 'managerName', NULL, 5000000, 2, '{"5":2500000,"6":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(4, 12345, 'farmName', 4, 'managerName', NULL, 5000000, 2, '{"5":2500000,"6":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(5, 12345, 'farmName', 5, 'managerName', NULL, 5000000, 3, '{"6":2500000,"10":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(6, 12345, 'farmName', 1, 'managerName', NULL, 5000000, 3, '{"11":2500000,"12":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(7, 12345, 'farmName', 2, 'managerName', NULL, 5000000, 4, '{"13":2500000,"14":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(8, 12345, 'farmName', 3, 'managerName', NULL, 5000000, 4, '{"15":2500000,"16":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(9, 12345, 'farmName', 4, 'managerName', NULL, 5000000, 5, '{"17":2500000,"18":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(10, 12345, 'farmName', 5, 'managerName', NULL, 5000000, 5, '{"19":2500000,"20":2500000}', '2016-05-25 18:43:53', '2016-05-25 18:43:53');

-- basic
INSERT INTO doctor_units VALUES (1, 'unitName', null, now(), now());


-- start 消息相关
INSERT INTO `doctor_message_rule_templates`
(name, type, category, rule_value, status, message_template, content, producer, `describe`, created_at, updated_at, updated_by)
VALUES
-- 系统消息(可配多少小时后)
('系统消息测试', 0, 0, '{"frequence":-1,"channels":"0,1,2,3"}', 1, null, '{"data":"系统消息测试"}', 'sysMessageProducer', '系统消息测试', now(), now(), null),
-- id:1 (断奶、流产、返情日期间隔(天))
('待配种母猪提示', 1, 1, '{"values":[{"id":1, "ruleType":1,"value":7, "describe":"断奶、流产、返情日期间隔(天)"}],"frequence":24,"channels":"0,1,2,3"}', 1, 'sow.breed', null, 'sowBreedingProducer', '待配种母猪提示', now(), now(), null),
('待配种母猪警报', 2, 1, '{"values":[{"id":1, "ruleType":1,"value":21, "describe":"断奶、流产、返情日期间隔(天)"}],"frequence":24,"channels":"0,1,2,3"}', 1, 'sow.breed', null, 'sowBreedingProducer', '待配种母猪警报', now(), now(), null),
-- id:1 (母猪已配种时间间隔(天))
('母猪需妊娠检查提示', 1, 2,'{"values":[{"id":1, "ruleType":2,"leftValue":19,"rightValue":25, "describe":"母猪已配种时间间隔(天)"}],"frequence":24,"channels":"0,1,2,3"}',1, 'sow.preg.check', null, 'sowPregCheckProducer', '母猪需妊娠检查提示', now(), now(), null),
-- 母猪需转入妊娠舍提示
('母猪需转入妊娠舍提示', 1, 3,'{"frequence":24,"channels":"0,1,2,3"}',1, 'sow.preg.home', null, 'sowPregHomeProducer', '母猪需转入妊娠舍提示', now(), now(), null),
-- id:1 (预产期提前多少天提醒)
('母猪预产期提示', 1, 4,'{"values":[{"id":1, "ruleType":1,"value":7, "describe":"预产期提前多少天提醒"}],"frequence":24,"channels":"0,1,2,3"}',1, 'sow.birth.date', null, 'sowBirthDateProducer', '母猪预产期提示', now(), now(), null),
-- id:1 (母猪分娩日期起的天数)
('母猪需断奶提示', 1, 5,'{"values":[{"id":1, "ruleType":1,"value":21, "describe":"母猪分娩日期起的天数"}],"frequence":24,"channels":"0,1,2,3"}',
1, 'sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶提示', now(), now(), null),
('母猪需断奶警报', 2, 5,'{"values":[{"id":1, "ruleType":1,"value":35, "describe":"母猪分娩日期起的天数"}],"frequence":24,"channels":"0,1,2,3"}',
1, 'sow.need.wean', null, 'sowNeedWeanProducer', '母猪需断奶警报', now(), now(), null),
-- id:1 (母猪胎次)
('母猪应淘汰提示', 1, 6,'{"values":[{"id":1, "ruleType":2, "leftValue":9,"rightValue":10, "describe":"胎次"}],"frequence":24,"channels":"0,1,2,3"}',1, 'sow.eliminate', null, 'sowEliminateProducer', '母猪应淘汰提示', now(), now(), null),
-- id:1 (公猪配种次数)
('公猪应淘汰提示', 1, 7,'{"values":[{"id":1, "ruleType":2, "leftValue":10,"rightValue":15, "describe":"公猪配种次数"}],"frequence":24,"channels":"0,1,2,3"}', 1, 'msg.boar.eliminate', null, 'boarEliminateProducer', '公猪应淘汰提示', now(), now(), null),
-- id:1 (母猪配种日期起的天数)
('母猪未产仔警报', 2, 10,'{"values":[{"id":1, "ruleType":1,"value":120, "describe":"母猪配种日期起的天数"}],"frequence":24,"channels":"0,1,2,3"}',1, 'sow.not.litter', null, 'sowNotLitterProducer', '母猪未产仔警报', now(), now(), null),
-- id:1 (库存量)
('仓库库存不足提示', 1, 9,'{"values":[{"id":1, "ruleType":1,"value":7, "describe":"库存量"}],"frequence":24,"channels":"0,1,2,3"}', 1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足提示', now(), now(), null),
('仓库库存不足警报', 2, 9,'{"values":[{"id":1, "ruleType":1,"value":3, "describe":"库存量"}],"frequence":24,"channels":"0,1,2,3"}', 1, 'msg.warehouse.store', null, 'storageShortageProducer', '仓库库存不足警报', now(), now(), null)
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


INSERT INTO `doctor_messages` (`id`, `farm_id`, `rule_id`, `role_id`, `user_id`, `template_id`, `message_template`, `type`, `category`, `data`, `channel`, `url`, `status`, `sended_at`, `failed_by`, `created_by`, `created_at`, `updated_at`)
VALUES
	(1,NULL,NULL,NULL,1,1,NULL,         0,0,'系统消息测试',                                     0,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(2,NULL,NULL,NULL,2,1,NULL,         0,0,'系统消息测试',                                      0,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(3,1,NULL,NULL,1,2,'template.sow',1,1,'{"pigCode":"2333", "barnName":"猪场1号"}',0,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(4,1,NULL,NULL,1,2,'template.sow',1,1,'{"pigCode":"2333", "barnName":"猪场1号"}',1,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(5,2,NULL,NULL,1,2,'template.sow',1,2,'{"pigCode":"2333", "barnName":"猪场2号"}',0,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(6,2,NULL,NULL,1,2,'template.sow',1,2,'{"pigCode":"2333", "barnName":"猪场2号"}',1,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(7,2,NULL,NULL,1,2,'template.sow',1,2,'{"pigCode":"2333", "barnName":"猪场2号"}',2,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(8,2,NULL,NULL,1,2,'template.sow',1,2,'{"pigCode":"2333", "barnName":"猪场2号"}',3,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(9,3,NULL,NULL,1,2,'template.sow',1,3,'{"pigCode":"2333", "barnName":"猪场3号"}',0,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(10,3,NULL,NULL,1,2,'template.sow',1,3,'{"pigCode":"2333","barnName":"猪场3号"}',1,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(11,3,NULL,NULL,1,2,'template.sow',1,3,'{"pigCode":"2333","barnName":"猪场3号"}',2,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00'),
	(12,4,NULL,NULL,1,2,'template.sow',1,4,'{"pigCode":"2333","barnName":"猪场4号"}',0,'http://m.doctor.com/message/message-detail',1,NULL,NULL,NULL,'2016-06-06 00:00:00','2016-06-06 00:00:00');

-- doctor farm
INSERT INTO `doctor_farms` (`id`, `name`, `org_id`, `org_name`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(12345, 'farmName', 1, 'orgName', '666666', null, now(), now());

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

-- 2016-06-08 基础表测试数据
INSERT INTO `doctor_breeds` (`id`, `name`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, '长白', NULL, NULL, NULL, NULL),
	(2, '二元', NULL, NULL, NULL, NULL),
	(3, '杜洛克', NULL, NULL, NULL, NULL),
	(4, '大白', NULL, NULL, NULL, NULL);

INSERT INTO `doctor_change_types` (`id`, `name`, `is_count_out`, `farm_id`, `farm_name`, `out_id`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, '销售', 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(2, '死亡', -1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(3, '淘汰', -1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(4, '失踪', -1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(5, '其他', -1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(6, '自宰', 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(7, '转出', -1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `doctor_change_reasons` (`id`, `change_type_id`, `reason`, `out_id`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 3, '猪流感淘汰', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
	(2, 3, '弱仔淘汰', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `doctor_customers` (`id`, `name`, `farm_id`, `farm_name`, `mobile`, `email`, `out_id`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, '测试客户', 0, NULL, '18888889999', NULL, NULL, NULL, 1, 'admin', NULL, NULL, '2016-05-31 16:48:01', '2016-05-31 16:48:01'),
	(2, '测试客户2', 0, NULL, '18888887777', NULL, NULL, NULL, 1, 'admin', NULL, NULL, '2016-05-31 16:48:01', '2016-05-31 16:48:01');

INSERT INTO `doctor_diseases` (`id`, `name`, `farm_id`, `farm_name`, `out_id`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, '胸膜肺炎', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL),
	(2, '胸膜炎', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL),
	(3, '阴道流脓', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL),
	(4, '阴道脱垂', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL),
	(5, '应激综合症', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL),
	(6, '油猪病', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `doctor_genetics` (`id`, `name`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, '台系', NULL, NULL, NULL, NULL),
	(2, '新美系', NULL, NULL, NULL, NULL),
	(3, '英系', NULL, NULL, NULL, NULL),
	(4, '美系', NULL, NULL, NULL, NULL);

INSERT INTO `doctor_units` (`id`, `name`, `extra`, `created_at`, `updated_at`)
VALUES
	(2, '吨', NULL, NULL, NULL),
	(3, '千克', NULL, NULL, NULL);

-- 猪场测试数据
INSERT INTO `doctor_farms` (`id`, `name`, `org_id`, `org_name`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(0, '测试猪场', 0, '测试公司', NULL, NULL, '2016-05-31 19:25:32', '2016-05-31 19:25:32'),
	(1, '测试猪场1', 0, '测试公司', NULL, NULL, '2016-05-31 19:25:32', '2016-05-31 19:25:32');

INSERT INTO `doctor_orgs` (`id`, `name`, `mobile`, `license`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(0, '测试公司', '1111111', NULL, NULL, NULL, '2016-05-31 19:26:07', '2016-05-31 19:26:07');

-- 猪舍测试数据
INSERT INTO `doctor_barns` (`id`, `name`, `org_id`, `org_name`, `farm_id`, `farm_name`, `pig_type`, `can_open_group`, `status`, `capacity`, `staff_id`, `staff_name`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, '保育1舍', 0, '测试公司', 0, '测试猪场名称0', 2, -1, 1, 101, 1, NULL, NULL, NULL, '2016-06-01 10:51:18', '2016-06-01 11:03:46'),
	(2, '妊娠2舍', 0, '测试公司', 0, '测试猪场名称0', 6, -1, 1, 10, 1, NULL, NULL, NULL, '2016-06-01 10:51:18', '2016-06-01 10:51:18'),
	(3, '保育2舍', 0, '测试公司', 0, '测试猪场名称0', 2, 1, 1, 101, 1, NULL, NULL, NULL, '2016-06-01 10:51:18', '2016-06-01 11:03:46'),
	(4, '保育北-1舍', 0, '测试公司', 1, '测试猪场1', 2, 1, 1, 101, 1, NULL, NULL, NULL, '2016-06-01 10:51:18', '2016-06-01 11:03:46');

-- 猪群测试数据
INSERT INTO `doctor_groups` (`id`, `org_id`, `org_name`, `farm_id`, `farm_name`, `group_code`, `batch_no`, `open_at`, `close_at`, `status`, `init_barn_id`, `init_barn_name`, `current_barn_id`, `current_barn_name`, `pig_type`, `breed_id`, `breed_name`, `genetic_id`, `genetic_name`, `staff_id`, `staff_name`, `remark`, `out_id`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(5, 0, '测试公司', 0, '测试猪场', '保育1舍(2016-06-07)', NULL, '2016-06-07 00:00:00', NULL, 1, 1, '保育1舍', 1, '保育1舍', 2, 1, '长白', 2, '新美系', NULL, NULL, '手动新建猪群事件', NULL, NULL, 1, 'admin', NULL, NULL, '2016-06-07 11:12:02', '2016-06-07 11:12:02'),
	(12, 0, '测试公司', 1, '测试猪场1', '保育北-1舍(2016-06-07)', NULL, '2016-06-07 00:00:00', NULL, 1, 4, '保育北-1舍', 4, '保育北-1舍', 2, 1, '长白', 2, '新美系', NULL, NULL, '录入转场事件', NULL, NULL, 0, NULL, NULL, NULL, '2016-06-07 15:13:24', '2016-06-07 15:13:24');

INSERT INTO `doctor_group_tracks` (`id`, `group_id`, `rel_event_id`, `sex`, `quantity`, `boar_qty`, `sow_qty`, `birth_date`, `avg_day_age`, `weight`, `avg_weight`, `price`, `amount`, `customer_id`, `customer_name`, `sale_qty`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(5, 4, 8, 2, 10, 5, 5, '2016-06-06 14:26:21', 0, 0, 0, 0, 0, NULL, NULL, NULL, '{\"type\":1,\"source\":1}', 1, '22', 1, 'admin', '2016-06-06 14:26:21', '2016-06-07 10:35:45'),
	(12, 11, 35, 2, 15, 8, 7, '2016-05-30 00:00:00', 9, 1000.0000000000001, 66.66666666666667, 0, 0, NULL, NULL, NULL, NULL, 1, 'admin', 1, 'admin', '2016-06-07 15:00:21', '2016-06-07 15:00:22');

INSERT INTO `doctor_group_events` (`id`, `org_id`, `org_name`, `farm_id`, `farm_name`, `group_id`, `group_code`, `event_at`, `type`, `name`, `desc`, `barn_id`, `barn_name`, `pig_type`, `quantity`, `weight`, `avg_weight`, `avg_day_age`, `is_auto`, `out_id`, `remark`, `extra`, `created_at`, `creator_id`, `creator_name`)
VALUES
	(9, 0, '测试公司', 0, '测试猪场', 5, '保育1舍(2016-06-07)', '2016-06-07 00:00:00', 1, '新建猪群', 'todo 事件描述', 1, '保育1舍', 2, NULL, NULL, NULL, NULL, 0, NULL, '手动新建猪群事件', '{\"source\":1}', '2016-06-07 11:12:06', 1, 'admin'),
	(10, 0, '测试公司', 0, '测试猪场', 5, '保育1舍(2016-06-07)', '2016-06-07 00:00:00', 2, '转入猪群', 'todo 事件描述', 1, '保育1舍', 2, 25, 1250, 50, 22, 0, NULL, '录入转群事件', '{\"inType\":1,\"inTypeName\":\"仔猪转入\",\"source\":1,\"sex\":1,\"breedId\":1,\"breedName\":\"长白\",\"boarQty\":10,\"sowQty\":15}', '2016-06-07 11:47:43', 1, 'admin'),
	(11, 0, '测试公司', 0, '测试猪场', 5, '保育1舍(2016-06-07)', '2016-06-07 00:00:00', 2, '转入猪群', 'todo 事件描述', 1, '保育1舍', 2, 25, 1250, 50, 22, 0, NULL, '录入转群事件', '{\"inType\":1,\"inTypeName\":\"仔猪转入\",\"source\":1,\"sex\":1,\"breedId\":1,\"breedName\":\"长白\",\"boarQty\":10,\"sowQty\":15}', '2016-06-07 13:37:05', 1, 'admin'),
	(40, 0, '测试公司', 1, '测试猪场1', 12, '保育北-1舍(2016-06-07)', '2016-06-07 00:00:00', 1, '新建猪群', 'todo 事件描述', 4, '保育北-1舍', 2, NULL, NULL, NULL, NULL, 1, NULL, '录入转场事件', '{\"source\":2}', '2016-06-07 15:13:24', 0, NULL),
	(41, 0, '测试公司', 1, '测试猪场1', 12, '保育北-1舍(2016-06-07)', '2016-06-07 00:00:00', 2, '转入猪群', 'todo 事件描述', 4, '保育北-1舍', 2, 15, 1000.0000000000001, 66.66666666666667, 18, 1, NULL, NULL, '{\"inType\":3,\"inTypeName\":\"群间转移\",\"source\":2,\"sex\":2,\"breedId\":1,\"breedName\":\"长白\",\"fromBarnId\":1,\"fromBarnName\":\"保育1舍\",\"fromGroupId\":5,\"fromGroupCode\":\"保育1舍(2016-06-07)\",\"boarQty\":8,\"sowQty\":7}', '2016-06-07 15:13:26', 1, 'admin');

