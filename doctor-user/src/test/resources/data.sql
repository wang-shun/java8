INSERT INTO `doctor_farms` (`id`, `name`, `org_id`, `org_name`, `province_id`, `province_name`, `city_id`, `city_name`, `district_id`, `district_name`, `detail_address`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, '猪场1', 1, '这是公司名称', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-06-02 10:54:14', '2016-06-02 10:54:14'),
	(2, '猪场2', 1, '这是公司名称', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-06-02 10:54:14', '2016-06-02 10:54:14'),
	(3, '猪场3', 1, '这是公司名称', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-06-02 10:54:14', '2016-06-02 10:54:14'),
	(4, '猪场4', 1, '这是公司名称', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-06-02 10:54:14', '2016-06-02 10:54:14');

INSERT INTO `doctor_orgs` (`id`, `name`, `mobile`, `license`, `out_id`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, '这是公司名称', '18800001111', 'http://fuck.you.com', NULL, NULL, '2016-06-01 21:20:22', '2016-06-02 10:54:14'),
	(2, '这是公司名称2', '18800060000', 'http://fuck.you.com666', NULL, NULL, '2016-06-04 16:09:02', '2016-06-04 16:13:18');

INSERT INTO `doctor_service_reviews` (`id`, `user_id`, `user_mobile`, `real_name`, `type`, `status`, `reviewer_id`, `created_at`, `updated_at`)
VALUES
	(1, 1, '18800001111', NULL, 1, 1, 7, '2016-06-07 11:11:11', '2016-06-07 21:16:25'),
	(2, 1, '18800001111', NULL, 2, 1, 7, '2016-06-07 11:11:11', '2016-06-07 21:21:04'),
	(3, 1, '18800001111', NULL, 3, -1, 7, '2016-06-07 11:11:11', '2016-06-07 21:23:06'),
	(4, 1, '18800001111', NULL, 4, 0, NULL, '2016-06-07 11:11:11', '2016-06-07 11:11:11'),
	(5, 2, '18800060000', '测试', 1, 1, -1, '2016-06-07 11:11:11', '2016-06-21 11:01:19'),
	(6, 2, '18800060000', NULL, 2, 0, NULL, '2016-06-07 11:11:11', '2016-06-07 19:33:28'),
	(7, 2, '18800060000', NULL, 3, 2, NULL, '2016-06-07 11:11:11', '2016-06-07 19:33:34'),
	(8, 2, '18800060000', NULL, 4, 0, NULL, '2016-06-07 11:11:11', '2016-06-07 11:11:11');

INSERT INTO `doctor_service_status` (`id`, `user_id`, `pigdoctor_status`, `pigdoctor_reason`, `pigdoctor_review_status`, `pigmall_status`, `pigmall_reason`, `pigmall_review_status`, `neverest_status`, `neverest_reason`, `neverest_review_status`, `pigtrade_status`, `pigtrade_reason`, `pigtrade_review_status`, `created_at`, `updated_at`)
VALUES
	(1, 1, 1, NULL, 1, -1, '内测中，敬请期待', 1, -1, '内测中，敬请期待', -1, -1, '内测中，敬请期待', 0, '2016-06-07 11:11:11', '2016-06-07 21:23:06'),
	(2, 2, 1, NULL, 1, -1, '内测中，敬请期待', 2, -1, '内测中，敬请期待', 2, -1, '内测中，敬请期待', 0, '2016-06-07 11:11:11', '2016-06-21 11:01:19');

INSERT INTO `doctor_staffs` (`id`, `org_id`, `org_name`, `user_id`, `role_id`, `role_name`, `status`, `sex`, `avatar`, `out_id`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 1, '这是公司名称', 1, NULL, NULL, 1, NULL, NULL, NULL, NULL, 11, NULL, 11, NULL, '2016-06-07 19:32:42', '2016-06-07 19:32:42'),
	(2, 2, '测试公司', 2, NULL, NULL, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `doctor_user_data_permissions` (`id`, `user_id`, `farm_ids`, `barn_ids`, `ware_house_types`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 1, '1,2,3', NULL, NULL, NULL, 7, '18811111111@123', 7, '18811111111@123', now(), now());

INSERT INTO `doctor_user_primarys` (`id`, `user_id`, `user_name`, `status`, `extra_json`, `created_at`, `updated_at`)
VALUES
	(1, 1, '122', 1, NULL, now(), now());

INSERT INTO `doctor_service_review_tracks` (`id`, `user_id`, `type`, `old_status`, `new_status`, `reason`, `reviewer_id`, `reviewer_name`, `created_at`, `updated_at`)
VALUES
	(1, 1, 1, 0, 2, NULL, NULL, NULL, now(), now()),
	(2, 1, 2, 0, 2, NULL, NULL, NULL, now(), now()),
	(3, 1, 3, 0, 2, NULL, NULL, NULL, now(), now());

INSERT INTO `doctor_user_subs` (`id`, `user_id`, `user_name`, `parent_user_id`, `parent_user_name`, `role_id`, `role_name`, `contact`, `status`, `extra_json`, `created_at`, `updated_at`)
VALUES
	(1, 1, '18811111111@2423', 1, '12312', 1, NULL, NULL, NULL, NULL, now(), now());

INSERT INTO `doctor_sub_roles` (`id`, `name`, `desc`, `user_id`, `app_key`, `status`, `extra_json`, `allow_json`, `created_at`, `updated_at`)
VALUES
	(1, '测试', '测试', 1, 'MOBILE', 1, NULL, '["manage_back_category"]', now(), now());

INSERT INTO `doctor_user_binds` (`id`, `user_id`, `target_system`, `uuid`, `target_user_name`, `target_user_mobile`, `target_user_email`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, 1, 1, 'uuid', 'n', 'm', 'e', NULL, NULL, NULL);

INSERT INTO `parana_users` (`id`, `name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `item_info_md5`, `created_at`, `updated_at`)
VALUES
	(1, 'admin', 'i@terminus.io', '18888888888', 'db16@0aa27fe12819fe1a453c', 0, 1, '[]', '{"seller":"haha"}', '{"good":"man"}', NULL, now(), now());

INSERT INTO `parana_user_profiles` (`id`, `user_id`, `realname`, `gender`, `province_id`, `province`, `city_id`, `city`, `region_id`, `region`, `street`, `extra_json`, `avatar`, `birth`, `created_at`, `updated_at`)
VALUES
	(1, 1, NULL, NULL, 0, '', NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, now(), now());

