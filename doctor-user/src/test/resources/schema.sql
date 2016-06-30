-- 卖家角色表: doctor_seller_roles

CREATE TABLE `doctor_seller_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NULL COMMENT '用户名',
  `desc` VARCHAR(32) NULL COMMENT '角色描述',
  `shop_id` bigint(20) NULL COMMENT '店铺 ID',
  `status` SMALLINT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` VARCHAR(1024) NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` VARCHAR(1024) NULL COMMENT '',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='卖家角色表';
CREATE INDEX idx_seller_roles_shop_id ON `doctor_seller_roles` (`shop_id`);

-- 运营角色表: doctor_operator_roles

CREATE TABLE `doctor_operator_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NULL COMMENT '用户名',
  `desc` VARCHAR(32) NULL COMMENT '角色描述',
  `status` SMALLINT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` VARCHAR(1024) NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` VARCHAR(1024) NULL COMMENT '',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='运营角色表';

-- 用户运营表: doctor_user_operators

CREATE TABLE `doctor_user_operators` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT          NULL COMMENT '用户 ID',
  `role_id`    BIGINT          NULL COMMENT '运营角色 ID',
  `status`     TINYINT         NULL COMMENT '运营状态',
  `extra_json` VARCHAR(1024)   NULL COMMENT '运营额外信息, 建议json字符串',
  `created_at` DATETIME        NOT NULL,
  `updated_at` DATETIME        NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '用户运营表';
CREATE INDEX idx_user_operator_user_id ON `doctor_user_operators` (`user_id`);
CREATE INDEX idx_user_operator_role_id ON `doctor_user_operators` (`role_id`);

-- 商家子账户表: doctor_user_sub_sellers

CREATE TABLE `doctor_user_sub_sellers` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT(20)          NULL COMMENT '用户 ID',
  `user_name`  VARCHAR(64)         NULL COMMENT '用户名 (冗余)',
  `shop_id`    BIGINT(20)          NULL COMMENT '店铺 ID',
  `status`     TINYINT             NULL COMMENT '状态',
  `roles_json` VARCHAR(1024)       NULL COMMENT '角色 ID 列表',
  `extra_json` VARCHAR(1024)       NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` DATETIME            NOT NULL,
  `updated_at` DATETIME            NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '商家子账户表';
CREATE INDEX idx_user_sub_seller_user_id ON `doctor_user_sub_sellers` (`user_id`);
CREATE INDEX idx_user_sub_seller_sub_id ON `doctor_user_sub_sellers` (shop_id);


-- 用户设备信息表 doctor_user_devices

create table `doctor_user_devices` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint null COMMENT '用户ID',
  `user_name` VARCHAR(64) COMMENT '用户名',
  `device_token` VARCHAR(128) COMMENT '',
  `device_type` VARCHAR(128) COMMENT '',
  `created_at` datetime NULL ,
  `updated_at` datetime NULL ,
   PRIMARY KEY (`id`)
) COMMENT = '用户设备信息表';
CREATE INDEX idx_user_devices_user_id ON `doctor_user_devices` (`user_id`);
CREATE INDEX idx_user_devices_token ON `doctor_user_devices` (`device_token`);


-- 商家子账户表: doctor_user_sub_sellers

CREATE TABLE `doctor_user_sellers` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT(20)          NULL COMMENT '用户 ID',
  `user_name`  VARCHAR(64)         NULL COMMENT '用户名 (冗余)',
  `shop_id`    BIGINT(20)          NULL COMMENT '店铺 ID',
  `shop_name`  VARCHAR(64)         NULL COMMENT '店铺名 (冗余)',
  `status`     TINYINT             NULL COMMENT '状态',
  `extra_json` VARCHAR(1024)       NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` DATETIME            NOT NULL,
  `updated_at` DATETIME            NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '商家子账户表';
CREATE INDEX idx_user_seller_user_id ON `doctor_user_sellers` (`user_id`);
CREATE INDEX idx_user_seller_sub_id ON `doctor_user_sellers` (shop_id);

CREATE TABLE `doctor_farms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `province_id` int(11) DEFAULT NULL COMMENT '所在省id',
  `province_name` varchar(64) DEFAULT NULL COMMENT '所在省',
  `city_id` int(11) DEFAULT NULL COMMENT '所在城市id',
  `city_name` varchar(64) DEFAULT NULL COMMENT '所在城市',
  `district_id` int(11) DEFAULT NULL COMMENT '区id',
  `district_name` varchar(64) DEFAULT NULL COMMENT '区名称',
  `detail_address` varchar(256) DEFAULT NULL COMMENT '区之后的具体地址',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='猪场表';

CREATE TABLE `doctor_orgs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `mobile` VARCHAR(16) DEFAULT NULL COMMENT '手机号码',
  `license` varchar(512) DEFAULT NULL COMMENT '营业执照复印件图片地址',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='公司表';

CREATE TABLE `doctor_service_reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `user_mobile` VARCHAR (16) DEFAULT NULL COMMENT '用户手机号,冗余字段,同表parana_users中的mobile',
  `real_name` VARCHAR (16) DEFAULT NULL COMMENT '用户申请服务时填写的真实姓名',
  `type` smallint(6) DEFAULT NULL COMMENT  '服务类型 1 猪场软件, 2 新融电商, 3 大数据, 4 生猪交易',
  `status` smallint(6) DEFAULT NULL COMMENT '审核状态 0 未审核, 2 待审核(提交申请) 1 通过，-1 不通过, -2 冻结',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审批人id',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_service_review_UNIQUE` (`user_id`,`type`)
) COMMENT='用户服务审批表';

CREATE TABLE `doctor_service_status` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pigdoctor_status` smallint(6) NOT NULL DEFAULT '0' COMMENT '猪场软件服务状态，1-开通，0-关闭',
  `pigdoctor_reason` varchar(256) DEFAULT NULL COMMENT '冗余，猪场软件审批不通过或被冻结申请资格的原因',
  `pigdoctor_review_status` smallint(6) DEFAULT NULL COMMENT '冗余，猪场软件服务的审批状态',
  `pigmall_status` smallint(6) NOT NULL DEFAULT '0' COMMENT '电商服务状态，1-开通，0-关闭',
  `pigmall_reason` varchar(256) DEFAULT NULL COMMENT '冗余，电商服务审批不通过或被冻结申请资格的原因',
  `pigmall_review_status` smallint(6) DEFAULT NULL COMMENT '冗余，电商服务的审批状态',
  `neverest_status` smallint(6) NOT NULL DEFAULT '0' COMMENT '大数据服务状态，1-开通，0-关闭',
  `neverest_reason` varchar(256) DEFAULT NULL COMMENT '冗余，大数据服务审批不通过或被冻结申请资格的原因',
  `neverest_review_status` smallint(6) DEFAULT NULL COMMENT '冗余，大数据服务的审批状态',
  `pigtrade_status` smallint(6) NOT NULL DEFAULT '0' COMMENT '生猪交易服务状态，1-开通，0-关闭',
  `pigtrade_reason` varchar(256) DEFAULT NULL COMMENT '冗余，生猪交易审批不通过或被冻结申请资格的原因',
  `pigtrade_review_status` smallint(6) DEFAULT NULL COMMENT '冗余，生猪交易服务的审批状态',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_service_status_UNIQUE` (`user_id`)
) COMMENT='用户服务状态表';

CREATE TABLE `doctor_staffs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色id',
  `role_name` varchar(64) DEFAULT NULL COMMENT '角色名称(冗余)',
  `status` smallint(6) DEFAULT NULL COMMENT '状态 1:在职，-1:不在职',
  `sex` smallint(6) DEFAULT NULL COMMENT '性别',
  `avatar` varchar(128) DEFAULT NULL COMMENT '用户头像',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT  '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT  '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='猪场职员表';

CREATE TABLE `doctor_user_data_permissions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `farm_ids` varchar(512) DEFAULT NULL COMMENT '猪场ids, 逗号分隔',
  `barn_ids` varchar(512) DEFAULT NULL COMMENT '猪舍ids, 逗号分隔',
  `ware_house_types` varchar(512) DEFAULT NULL COMMENT '仓库类型, 逗号分隔',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT  '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT  '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='用户数据权限表';

CREATE TABLE `doctor_user_primarys` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户 ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名 (冗余)',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_primary_user_id` (`user_id`)
) COMMENT='猪场主账户表';

CREATE TABLE `doctor_service_review_tracks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `type` smallint(6) NOT NULL COMMENT '服务类型',
  `old_status` smallint(6) NOT NULL COMMENT '原状态',
  `new_status` smallint(6) NOT NULL COMMENT '新状态',
  `reason` varchar(256) DEFAULT NULL COMMENT '状态变更的原因',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '操作人id',
  `reviewer_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='用户服务状态变更历史记录表';

CREATE TABLE `doctor_user_subs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户 ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名 (冗余)',
  `parent_user_id` bigint(20) DEFAULT NULL COMMENT '主账号ID',
  `parent_user_name` varchar(64) DEFAULT NULL COMMENT '主账号用户名(冗余)',
  `role_id` bigint(20) DEFAULT NULL COMMENT '子账号角色 ID',
  `role_name` varchar(40) DEFAULT NULL COMMENT '子账号角色名称',
  `contact` varchar(40) DEFAULT NULL COMMENT '子账号联系方式',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_sub_user_id` (`user_id`),
  KEY `idx_user_parent_sub_id` (`parent_user_id`),
  KEY `idx_user_sub_roles_id` (`role_id`)
) COMMENT='猪场子账户表';

CREATE TABLE `doctor_sub_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL COMMENT '角色名',
  `desc` varchar(32) DEFAULT NULL COMMENT '角色描述',
  `user_id` bigint(20) DEFAULT NULL COMMENT '所属主账号ID',
  `app_key` varchar(16) DEFAULT NULL COMMENT '角色所属',
  `status` smallint(6) DEFAULT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` varchar(1024) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sub_roles_user_id` (`user_id`)
) COMMENT='子账号角色表';

CREATE TABLE `doctor_user_binds` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `target_system` int(11) NOT NULL COMMENT '目标系统，参见项目中的枚举 TargetSystem',
  `uuid` varchar(64) NOT NULL DEFAULT '' COMMENT '系统为该用户生成的在目标系统的key（其实就是一个去掉横杠的UUID）',
  `target_user_name` varchar(128) DEFAULT NULL COMMENT '该用户在目标系统绑定的账号的名称',
  `target_user_mobile` varchar(16) DEFAULT NULL COMMENT '该用户在目标系统绑定的账号的手机号',
  `target_user_email` varchar(100) DEFAULT NULL COMMENT '该用户在目标系统绑定的账号的邮箱',
  `extra` varchar(1000) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_bind_UNIQUE1` (`user_id`,`target_system`),
  UNIQUE KEY `idx_user_bind_UNIQUE2` (`uuid`)
) COMMENT='用户账户与其他系统账户的绑定关系';

CREATE TABLE `parana_users` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NULL COMMENT '用户名',
  `email` VARCHAR(32) NULL COMMENT '邮件',
  `mobile` VARCHAR(16) NULL COMMENT '手机号码',
  `password` VARCHAR(32) NULL COMMENT '登录密码',
  `type` SMALLINT NOT NULL COMMENT '用户类型',
  `status` tinyint(1) NOT NULL COMMENT '状态 0:未激活, 1:正常, -1:锁定, -2:冻结, -3: 删除',
  `roles_json` VARCHAR(512) NULL COMMENT '角色列表, 以json表示',
  `extra_json` VARCHAR(1024) NULL COMMENT '商品额外信息,建议json字符串',
  `tags_json` VARCHAR(1024) NULL COMMENT '商品标签的json表示形式,只能运营操作, 对商家不可见',
  `item_info_md5` CHAR(32) NULL COMMENT '商品信息的m5值, 商品快照需要和这个摘要进行对比',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY idx_users_name(name),
  UNIQUE KEY idx_users_email(email),
  UNIQUE KEY idx_users_mobile(mobile)
) COMMENT='用户表';

CREATE TABLE `parana_user_profiles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `realname` VARCHAR(32) NULL COMMENT '真实姓名',
  `gender` SMALLINT NULL COMMENT '性别1男2女',
  `province_id` bigint(20) NULL COMMENT '省id',
  `province` VARCHAR(100) NULL COMMENT '省',
  `city_id` bigint(20) NULL COMMENT '城id',
  `city` VARCHAR(100) NULL COMMENT '城',
  `region_id` bigint(20) NULL COMMENT '区id',
  `region` VARCHAR(100) NULL COMMENT '区',
  `street` VARCHAR(130) NULL COMMENT '地址',
  `extra_json` VARCHAR(2048) NULL COMMENT '其他信息, 以json形式存储',
  `avatar` VARCHAR(512) NULL COMMENT '头像',
  `birth` VARCHAR(40) NULL COMMENT '出生日期',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY idx_user_id(user_id)
) COMMENT='用户详情表';