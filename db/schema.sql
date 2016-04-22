
-- 用户表: parana_users
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

-- 用户文件表: parana_user_files
CREATE TABLE IF NOT EXISTS `parana_user_files` (
  `id`          bigint(20)    unsigned  NOT NULL  AUTO_INCREMENT,
  `create_by`   bigint(20)    NOT NULL      COMMENT '用户id',
  `file_type`   smallint(6)   NOT NULL      COMMENT '文件类型',
  `group`       varchar(128)  DEFAULT   NULL      COMMENT '用户族',
  `folder_id`   bigint(20)    NOT NULL      COMMENT '文件夹id',
  `name`        varchar(128)  NULL      DEFAULT '' COMMENT '文件名称',
  `path`        varchar(128)  NOT NULL DEFAULT '' COMMENT '文件相对路径',
  `size`        int(11)       NOT NULL      COMMENT '文件大小',
  `extra`       varchar(512)  NULL      DEFAULT '' COMMENT '文件信息介绍',
  `created_at`  datetime      DEFAULT   NULL,
  `updated_at`  datetime      DEFAULT   NULL,
  PRIMARY KEY (`id`)
) COMMENT='用户文件表';
CREATE INDEX idx_user_files_create_by ON parana_user_files (`create_by`);
CREATE INDEX idx_user_files_folder_id ON parana_user_files (folder_id);
CREATE INDEX idx_user_files_name ON parana_user_files (name);


-- 用户文件夹管理表:parana_user_folders
CREATE TABLE IF NOT EXISTS `parana_user_folders` (
  `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `create_by`   bigint(20)          NOT NULL COMMENT '用户id',
  `group`       varchar(128)        DEFAULT   NULL      COMMENT '用户族',
  `pid`         bigint(20)          DEFAULT NULL COMMENT '父级id',
  `level`       tinyint(1)          DEFAULT NULL COMMENT '级别',
  `has_children`bit(1)              DEFAULT NULL COMMENT '是否有孩子',
  `folder`      varchar(128)        NOT NULL DEFAULT '' COMMENT '文件夹名称',
  `created_at`  datetime            DEFAULT NULL,
  `updated_at`  datetime            DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='用户文件夹管理表';
CREATE INDEX idx_user_folders_create_by ON parana_user_folders (`create_by`);
CREATE INDEX idx_user_folders_pid ON parana_user_folders (pid);
CREATE INDEX idx_user_folders_folder ON parana_user_folders (folder);


-- 消息中心相关的表

CREATE TABLE parana_subscriptions(
  `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
  `user_id`  BIGINT  NOT NULL  COMMENT '用户id',
  `user_name` VARCHAR(128) NULL comment '用户名称',
  `channel`  INT  NOT NULL  COMMENT '消息渠道：-1-》非法，0-》站内信，1-》短信，2-》邮箱，3-》app消息推送',
  `account`  VARCHAR(128)  NULL  COMMENT '用户的账户, 可以为用户id(站内信), 邮箱(邮件), 手机号(短信), app设备号(app消息推送)',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)COMMENT='消息订阅情况';
CREATE UNIQUE INDEX idx_subscriptions_account ON parana_subscriptions(account);
CREATE INDEX idx_subscriptions_user_id ON parana_subscriptions(user_id);
CREATE INDEX idx_subscriptions_user_name ON parana_subscriptions(user_name);

CREATE TABLE parana_receiver_groups(
  `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
  `user_id`  BIGINT  NULL  COMMENT '用户id, 唯一自然键, 发站内信时使用',
  `user_name` VARCHAR(128) null comment '用户名称',
  `email`  VARCHAR(128)  NULL  COMMENT '发邮件时使用',
  `mobile`  VARCHAR(64)  NULL  COMMENT '发短信进使用',
  `android`  VARCHAR(128)  NULL  COMMENT '用于app消息推送时的安卓设备号',
  `ios`  VARCHAR(128)  NULL  COMMENT '用于app消息推送时的iphone设备号',
  `wp`  VARCHAR(128)  NULL  COMMENT '用于app消息推送时的winPhone设备号',
  `group1`  VARCHAR(128)  NULL  COMMENT '消息群组, 具体含义由解决方案中定义, 如用户类型, 用户所属公司等信息',
  `group2`  VARCHAR(128)  NULL  COMMENT '消息群组, 具体含义由解决方案中定义, 如用户类型, 用户所属公司等信息',
  `group3`  VARCHAR(128)  NULL  COMMENT '消息群组, 具体含义由解决方案中定义, 如用户类型, 用户所属公司等信息',
  `group4`  VARCHAR(128)  NULL  COMMENT '消息群组, 具体含义由解决方案中定义, 如用户类型, 用户所属公司等信息',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)COMMENT='接收者群组';
CREATE UNIQUE INDEX idx_receiver_groups_user_id ON parana_receiver_groups(user_id);
CREATE UNIQUE INDEX idx_receiver_groups_user_name ON parana_receiver_groups(user_name);
CREATE INDEX idx_receiver_groups_group1 ON parana_receiver_groups(group1);
CREATE INDEX idx_receiver_groups_group2 ON parana_receiver_groups(group2);
CREATE INDEX idx_receiver_groups_group3 ON parana_receiver_groups(group3);
CREATE INDEX idx_receiver_groups_group4 ON parana_receiver_groups(group4);

CREATE TABLE parana_message_templates(
  `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
  `creator_id`  BIGINT  NULL  COMMENT '创建者的用户id',
  `creator_name`  VARCHAR(128)  NULL  COMMENT '创建者的名称, 冗余',
  `name`  VARCHAR(128) NOT NULL  COMMENT '模板的名称, 具有唯一性',
  `title`  VARCHAR(1024)  NULL  COMMENT '消息的默认标题',
  `content`  VARCHAR(4096) NOT  NULL  COMMENT '消息的内容模板, handlebars格式',
  `context`  VARCHAR(4096)  NULL  COMMENT '消息的内容模板相关连的上下文示例, 用于指导消息调用者有哪些变量可用',
  `channel`  INT  NULL  COMMENT '消息渠道：-1-》非法，0-》站内信，1-》短信，2-》邮箱，3-》app消息推送',
  `disabled`  BIT(1)  NULL  COMMENT '当配置为true时, 这个模板的调用者不应该发送这个消息.',
  `description`  VARCHAR(256)  NULL  COMMENT '消息模板的备注',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)COMMENT='消息模板表';
CREATE UNIQUE INDEX idx_message_templates_name ON parana_message_templates(name);


CREATE TABLE parana_messages(
  `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
  `category`  VARCHAR(128)  NULL  COMMENT '消息的类别, 主要用于归类, 如分页查询的条件, 具体值由解决方案中决定',
  `title`  VARCHAR(1024)  NULL  COMMENT '标题',
  `content`  VARCHAR(4096)  NULL  COMMENT '内容',
  `template`  VARCHAR(128)  NULL  COMMENT '消息模板名称',
  `data`  VARCHAR(4096)  NULL  COMMENT '消息模板的上下文数据，json（map）',
  `attaches`  VARCHAR(1024)  NULL  COMMENT '消息的附件, 以逗号分割的URL文件名',
  `remark`  VARCHAR(256)  NULL  COMMENT '消息备注',
  `sender_id`  BIGINT  NULL  COMMENT '发送者id',
  `sender`  VARCHAR(128)  NULL  COMMENT '发送者信息:userid, email, mobile, device_token',
  `send_at`  DATETIME  NULL  COMMENT '消息最终的发送时间',
  `start_at`  DATETIME  NULL  COMMENT '消息的发送时机, 用于决定什么时间才能尝试消息发送',
  `end_at`  DATETIME  NULL  COMMENT '消息的关闭时间',
  `receivers`  VARCHAR(4096)  NULL  COMMENT '消息的接收者列表json: userIds(站内信), emails, mobiles, device_tokens。当为群组消息时,可支持四个维度的分组, 格式如下: {"group1":"buyer", "group2":"terminus corp", "group3":"浙江省", "group4":"xxx"}',
  `group_message_type`  INT  NULL  COMMENT '标志是否为群组消息',
  `check_subscribe`  BIT(1)  NULL  COMMENT '是否检测订阅情况. 当为false时,不检测用户类型',
  `status`  INT  NULL  COMMENT '消息的状态：-2-》初始化消息失败，-1-》发送失败，0-》消息排队中，1-》发送成功， 2-》关闭',
  `fail_reason`  VARCHAR(256)  NULL  COMMENT '消息失败的原因',
  `channel`  INT  NULL  COMMENT '消息渠道：-1-》非法，0-》站内信，1-》短信，2-》邮箱，3-》app消息推送',
  `channel_output`  VARCHAR(256)  NULL  COMMENT '发送渠道的结果返回: 站内信的ids(以逗号分割)',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)COMMENT='消息表';
CREATE INDEX idx_message_category ON parana_messages(`category`);
CREATE INDEX idx_message_send_id ON parana_messages(sender_id);

CREATE TABLE parana_notifications(
  `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
  `audience_id`  BIGINT  NULL  COMMENT '听众的用户id',
  `audience_group1`  VARCHAR(128)  NULL  COMMENT '听众的群组, 具体含义由解决方案中定义, 如用户类型, 用户所属公司等信息',
  `audience_group2`  VARCHAR(128)  NULL  COMMENT '',
  `audience_group3`  VARCHAR(128)  NULL  COMMENT '',
  `audience_group4`  VARCHAR(128)  NULL  COMMENT '',
  `subject`  VARCHAR(1024)  NULL  COMMENT '消息标题',
  `content`  VARCHAR(4096)  NULL  COMMENT '消息内容',
  `checked`  BIT(1)  NULL  COMMENT '用户是否查看过此通知',
  `created_at`  DATETIME  NULL  COMMENT '创建时间',
  `updated_at`  DATETIME  NULL  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)COMMENT='站内信表';
CREATE INDEX idx_notification_audience_id ON parana_notifications(audience_id);
CREATE INDEX idx_notification_audience_group1 ON parana_notifications(audience_group1);
CREATE INDEX idx_notification_audience_group2 ON parana_notifications(audience_group2);
CREATE INDEX idx_notification_audience_group3 ON parana_notifications(audience_group3);
CREATE INDEX idx_notification_audience_group4 ON parana_notifications(audience_group4);


CREATE TABLE parana_message_boxes(
    `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
    `user_id`  BIGINT  NULL  COMMENT '用户id',
    `box_index`  INT  NULL  COMMENT '消息箱号',
    `notification_id`  BIGINT  NULL  COMMENT '站内信id',
    `created_at`  DATETIME  NULL  COMMENT '创建时间',
    `updated_at`  DATETIME  NULL  COMMENT '修改时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX idx_message_box_user_box_notification ON parana_message_boxes(user_id,box_index,notification_id);
CREATE INDEX idx_message_box_notification_id ON parana_message_boxes(notification_id);

-- 用户设备信息表 galaxy_user_devices
create table `parana_user_devices` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` bigint null COMMENT '用户ID',
  `user_name` VARCHAR(64) COMMENT '用户名',
  `device_token` VARCHAR(128) COMMENT '',
  `device_type` VARCHAR(128) COMMENT '',
  `created_at` datetime NULL ,
  `updated_at` datetime NULL ,
   PRIMARY KEY (`id`)
) COMMENT = '用户设备信息表';
CREATE INDEX idx_user_devices_user_id ON `parana_user_devices` (`user_id`);
CREATE INDEX idx_user_devices_token ON `parana_user_devices` (`device_token`);

-- sub domain 表
CREATE TABLE `parana_sub_domains` (
  `id`        BIGINT           NOT NULL AUTO_INCREMENT,
  `desc`      VARCHAR(32)      NULL COMMENT '描述',
  `value`     VARCHAR(64)      NOT NULL COMMENT 'sub domain',
  `type`      TINYINT UNSIGNED NOT NULL COMMENT '类型: 1. 用户, 2. 店铺, 3. 企业',
  `target_id` BIGINT           NOT NULL COMMENT '目标 id (user id, shop id)',
  created_at  DATETIME         NULL COMMENT '创建时间',
  updated_at  DATETIME         NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `idx_sub_domains_value_UNIQUE` ON `parana_sub_domains` (`value`);
CREATE UNIQUE INDEX `idx_sub_domains_t_target_id_UNIQUE` ON `parana_sub_domains` (`type`, `target_id`);


-- 配置表: parana_configs
DROP TABLE IF EXISTS `parana_configs`;

CREATE TABLE `parana_configs` (
  `id`          BIGINT          NOT NULL AUTO_INCREMENT,
  `biz_type`    SMALLINT        NOT NULL DEFAULT 0 COMMENT '业务类型',
  `key`         VARCHAR(128)    NOT NULL COMMENT '键',
  `value`       VARCHAR(1024)   NOT NULL COMMENT '值',
  `data_type`   VARCHAR(16)     NOT NULL DEFAULT 0 COMMENT '数据类型',
  `group`       VARCHAR(16)     NOT NULL DEFAULT 0 COMMENT '分组',
  `description` VARCHAR(256)    NULL COMMENT '描述',
  `created_at`  DATETIME        NOT NULL,
  `updated_at`  DATETIME        NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='配置中心';

CREATE UNIQUE INDEX `idx_configs_bt_key_UNIQUE` ON parana_configs(`biz_type`, `key`);

CREATE TABLE `parana_user_vat_invoices` (
  `id`                    BIGINT          NOT NULL  AUTO_INCREMENT COMMENT '自增主键' ,
  `user_id`               BIGINT          NOT NULL  COMMENT '用户标识',
  `company_name`          VARCHAR(128)    NOT NULL  COMMENT '公司名称',
  `tax_register_no`       VARCHAR(32)     NOT NULL  COMMENT '税务登记号',
  `register_address`      VARCHAR(128)    NOT NULL  COMMENT '注册地址',
  `register_phone`        VARCHAR(16)     NOT NULL  COMMENT '注册电话',
  `register_bank`         VARCHAR(128)    NOT NULL  COMMENT '注册银行',
  `bank_account`          VARCHAR(32)     NOT NULL  COMMENT '银行帐号',
  `tax_certificate`       VARCHAR(256)    NULL      COMMENT '税务登记证',
  `taxpayer_certificate`  VARCHAR(256)    NULL      COMMENT '一般纳税人证书',
  `created_at`            DATETIME        NULL      COMMENT '创建时间',
  `updated_at`            DATETIME        NULL      COMMENT '更新时间',
  PRIMARY KEY (`id`)
)COMMENT='用户增值税发票表';
CREATE UNIQUE INDEX idx_parana_uvi_user_id_uniq on `parana_user_vat_invoices`(`user_id`);

-- 2016-04-21 猪场软件表
-- 基础表
-- 公司表
CREATE TABLE `doctor_orgs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公司表';

-- 猪场表
CREATE TABLE `doctor_farms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪场表';
CREATE INDEX idx_doctor_farms_org_id ON doctor_farms(org_id);

-- 猪舍表
CREATE TABLE `doctor_barns` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '猪舍名称',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类名称  代码枚举',
  `can_open_group` smallint(6) DEFAULT NULL COMMENT '能否建群',
  `status` smallint(6) DEFAULT NULL COMMENT '使用状态',
  `staff` varchar(64) DEFAULT NULL COMMENT '工作人员',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='猪舍表';
CREATE INDEX idx_doctor_barns_farm_id ON doctor_barns(farm_id);

-- 猪场职员表
CREATE TABLE `doctor_staffs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '猪舍名称',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `role_type` smallint(6) DEFAULT NULL COMMENT '角色类型',
  `status` smallint(6) DEFAULT NULL COMMENT '状态 在职，不在职',
  `sex` smallint(6) DEFAULT NULL COMMENT '性别',
  `mobile` varchar(16) DEFAULT NULL COMMENT '手机号',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='猪场职员表';
CREATE INDEX idx_doctor_staffs_farm_id ON doctor_staffs(farm_id);

-- 变动类型表
CREATE TABLE `doctor_change_types` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(32) DEFAULT NULL COMMENT '变动类型名称',
  `is_count_out` smallint(6) DEFAULT NULL COMMENT '是否计入出栏猪',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='变动类型表';
CREATE INDEX idx_doctor_change_types_farm_id ON doctor_change_types(farm_id);

-- 疾病表
CREATE TABLE `doctor_diseases` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(32) DEFAULT NULL COMMENT '变动类型名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='变动类型表';
CREATE INDEX idx_doctor_diseases_farm_id ON doctor_diseases(farm_id);


-- 品种表
CREATE TABLE `doctor_breeds` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '品种名称',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='品种表';

-- 品系表
CREATE TABLE `doctor_genetics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '品系名称',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='品系表';

-- 计量单位表
CREATE TABLE `doctor_units` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '品系名称',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=Myisam DEFAULT CHARSET=utf8 COMMENT='计量单位表';


-- 猪群表
-- 猪群卡片表
CREATE TABLE `doctor_group_cards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `group_code` varchar(64) DEFAULT NULL COMMENT '猪群号',
  `open_at` datetime DEFAULT NULL COMMENT '建群时间',
  `close_at` datetime DEFAULT NULL COMMENT '关闭时间',
  `status` smallint(6) DEFAULT NULL COMMENT '枚举: 已建群, 已关闭',
  `init_barn_id` bigint(20) DEFAULT NULL COMMENT '初始猪舍id',
  `init_barn_name` varchar(64) DEFAULT NULL COMMENT '初始猪舍name',
  `current_barn_id` bigint(20) DEFAULT NULL COMMENT '当前猪舍id',
  `current_barn_name` varchar(64) DEFAULT NULL COMMENT '当前猪舍名称',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类名称id',
  `sex` smallint(6) DEFAULT NULL COMMENT '性别',
  `breed_id` bigint(20) DEFAULT NULL COMMENT '品种id',
  `breed_name` varchar(32) DEFAULT NULL COMMENT '品种name',
  `genetic_id` bigint(32) DEFAULT NULL COMMENT '品系id',
  `genetic_name` varchar(32) DEFAULT NULL COMMENT '品系name',
  `staff` varchar(64) DEFAULT NULL COMMENT '饲养员',
  `remark` text COMMENT '描述',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪群卡片表';
CREATE INDEX idx_doctor_group_cards_org_id ON doctor_group_cards(org_id);
CREATE INDEX idx_doctor_group_cards_farm_id ON doctor_group_cards(farm_id);
CREATE INDEX idx_doctor_group_cards_init_barn_id ON doctor_group_cards(init_barn_id);
CREATE INDEX idx_doctor_group_cards_current_barn_id ON doctor_group_cards(current_barn_id);
CREATE INDEX idx_doctor_group_cards_open_at ON doctor_group_cards(open_at);
CREATE INDEX idx_doctor_group_cards_close_at ON doctor_group_cards(close_at);

-- 猪群卡片跟踪表
CREATE TABLE `doctor_group_card_tracks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `group_card_id` bigint(20) DEFAULT NULL COMMENT '猪群卡片id',
  `rel_event_id` bigint(20) DEFAULT NULL COMMENT '关联的最新一次的事件id',
  `quantity` int(11) DEFAULT NULL COMMENT '猪只数',
  `avg_day_age` double DEFAULT NULL COMMENT '平均日龄',
  `weight` double DEFAULT NULL COMMENT '总活体重(公斤)',
  `avg_weight` double DEFAULT NULL COMMENT '平均体重(公斤)',
  `price` bigint(20) DEFAULT NULL COMMENT '单价(元)',
  `amount` bigint(20) DEFAULT NULL COMMENT '总金额(元)',
  `customer` varchar(32) DEFAULT NULL COMMENT '客户',
  `sale_qty` int(11) DEFAULT NULL COMMENT '销售数量',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪群卡片明细表';
CREATE UNIQUE INDEX idx_doctor_group_card_tracks_group_card_id ON doctor_group_card_tracks(group_card_id);
CREATE INDEX idx_doctor_group_card_tracks_rel_event_id ON doctor_group_card_tracks(rel_event_id);

-- 猪群事件表
CREATE TABLE `doctor_group_events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_id` varchar(64) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` varchar(64) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `group_card_id` varchar(64) DEFAULT NULL COMMENT '猪群卡片id',
  `group_code` varchar(64) DEFAULT NULL COMMENT '猪群号',
  `type` smallint(6) DEFAULT NULL COMMENT '事件类型 枚举 总共10种',
  `name` varchar(32) DEFAULT NULL COMMENT '事件名称 冗余',
  `desc` varchar(512) DEFAULT NULL COMMENT '事件描述',
  `barn_id` bigint(20) DEFAULT NULL COMMENT '事件发生猪舍id',
  `barn_name` varchar(63) DEFAULT NULL COMMENT '事件发生猪舍id',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类枚举',
  `operator` smallint(6) DEFAULT NULL COMMENT '操作符 + -',
  `quantity` int(11) DEFAULT 0 COMMENT '事件猪只数',
  `boar_qty` int(11) DEFAULT 0 COMMENT '事件公猪数',
  `sow_qty` int(11) DEFAULT 0 COMMENT '事件母猪数',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '具体时间的内容通过json存储',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪群事件表';
CREATE INDEX idx_doctor_doctor_group_events_farm_id ON doctor_group_events(farm_id);
CREATE INDEX idx_doctor_doctor_group_events_group_card_id ON doctor_group_events(group_card_id);
CREATE INDEX idx_doctor_doctor_group_events_barn_id ON doctor_group_events(barn_id);
CREATE INDEX idx_doctor_doctor_group_events_created_at ON doctor_group_events(created_at);

-- pig card 猪信息表
drop table if exists doctor_pig_cards;
create table doctor_pig_cards(
	id	bigint(20) UNSIGNED not null AUTO_INCREMENT comment 'ID',
	org_id bigint(20) UNSIGNED not null comment '公司Id',
	org_name varchar(128) default null comment '公司名称',
	farm_id bigint(20) UNSIGNED not null comment '猪场Id',
	farm_name varchar(128) default null comment '猪场名称',
	pig_out_id varchar(128) not null comment '关联猪外部Id',
	pig_code varchar(64) default null comment '猪编号',
	pit_type SMALLINT not null comment '猪类型(公猪，母猪， 仔猪)',
	pig_father_id bigint(20) UNSIGNED default null comment '猪父亲Id',
	pig_mother_id bigint(20) UNSIGNED default null comment '母猪Id',
	source varchar(64) default null comment '母猪来源',
	birthdate DATETIME default null comment '母猪生日',
	birthweight DOUBLE default null comment '出生重量',
	in_farm_date datetime default null comment '进厂日期',
	in_farm_day_age int default null comment '进厂日龄',
	init_barn_id bigint(20) UNSIGNED default null comment '进厂位置',
	init_barn_name varchar(128) default null comment '进厂位置名称',
	breed_id bigint(20) UNSIGNED not null comment '品种id',
	breed_name varchar(64) default null comment '品种名称',
	genetic_id bigint(20) UNSIGNED not null comment '品系Id',
	genetic_name varchar(64) default null comment '品系名称',
	extra text default null comment '公猪（公猪类型，boar_type）母猪（初始胎次:init_parity, 性别：sex, 左右乳头数量： left_nipple_count, right_nipple_count ）',
	remark varchar(128) default null comment '标注信息',
	create_at datetime default null,
	create_by varchar(64) default null,
	updated_at datetime default null,
	updated_by varchar(64) default null,
	primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪基础信息表';
create index doctor_pig_card_farm_id on doctor_pig_card(farm_id);

-- 猪 track 信息关联表
drop table if exists doctor_pig_tracks;
create table doctor_pig_tracks(
	id bigint(20) UNSIGNED not NULL comment '猪Id',
	status varchar(64) default null comment '猪状态信息',
	current_barn_id bigint(20) UNSIGNED default null comment '当前猪舍Id',
	current_barn_name bigint(20) UNSIGNED DEFAULT null comment '当前猪舍名称',
	weight double default null comment '猪重量',
	out_farm_date datetime default null comment '猪离场时间',
	rel_event_id varchar(128) default null comment '关联事件最近事件',
	extra text default null comment '事件修改猪对应信息',
	srm varchar(64) default null comment '输入码',
	current_parity int default null comment '当前胎次信息',
	remark varchar(64) default null comment '备注',
	primary key(id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='猪Track 信息表';
create index doctor_pig_track_id on doctor_pig_track(id);


-- 公猪，母猪， 仔猪事件信息表
drop table if exists doctor_pig_events;
create table doctor_pig_events(
	id BIGINT(20) UNSIGNED not null AUTO_INCREMENT comment 'id',
	org_id bigint(20) UNSIGNED not null comment '公司Id',
	org_name varchar(64) default null comment '公司名称',
	farm_id bigint(20) unsigned not null comment '猪场Id',
	farm_name varchar(64) default null comment '猪场名称',
	pig_id bigint(20) unsigned not null comment '猪Id',
	pig_code varchar(64) default null comment '猪Code',
	event_time datetime not null comment '事件时间',
	event_type int not null comment '事件类型',
	event_kind int not null comment '事件猪类型， 公猪， 母猪， 仔猪',
	event_name varchar(128) not null comment '事件名称',
	event_desc varchar(128) default null comment '事件描述',
	event_barn_id bigint(20) unsigned comment '事件地点',
	event_barn_name varchar(64) comment '地点名称',
	rel_event_id bigint(20) unsigned comment '关联事件Id',
	extra TEXT default null comment '参考设计文档',
	mark varchar(128) comment '备注信息',
	create_at datetime comment '创建日期',
	create_by varchar(64) comment '创建人',
	updated_at datetime comment '修改时间',
	updated_by varchar(64) comment '修改人',
	primary key(id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户设备信息表';;
create index doctor_pig_event_pig_id on doctor_pig_event(pig_id);

-- 仓库表
drop table if exists doctor_ware_houses;
create table doctor_ware_houses(
	id bigint(20) UNSIGNED not null AUTO_INCREMENT comment 'id',
	ware_house_name varchar(128) default null comment '仓库名称',
	farm_id bigint(20) unsigned not null comment '猪场仓库信息',
	farm_name varchar(64) default null comment '猪场名称',
	manager_id	bigint(20) unsigned default null comment '管理员Id',
	manager_name varchar(64) default null comment '管理人员姓名',
	address varchar(64) default null comment '地址信息',
	type_id bigint(20) unsigned not null comment '仓库类型',
	type_name varchar(64) not null comment '仓库类型名称',
	is_default SMALLINT not null comment '默认仓库信息',
	primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='仓库信息数据表';


-- 物料信息数据表内容（当前包含 疫苗， 药品，原料，饲料，消耗品）等
drop table if exists doctor_material_infos;
create table doctor_material_infos(
	id bigint(20) unsigned not null AUTO_INCREMENT comment 'id',
	farm_id bigint(20) unsigned not null comment '猪场信息',
	farm_name varchar(64) default null comment '猪场名称',
	material_type_id bigint(20) unsigned not null comment '原料类别',
	material_type_text varchar(64) default null comment '原料类别名称',
	remark varchar(128) default null comment '标注',
	unit_group_id bigint(20) unsigned default null comment '单位组Id',
	unit_group_name varchar(64) default null comment '单位组名称',
	unit_id bigint(20) unsigned default null comment '单位Id',
	unit_name varchar(64) default null comment '单位名称',
	price DOUBLE default null comment '价格',
	extra text default null comment '扩展信息: 药品-默认计量的大小',
	create_at datetime default null,
	create_by varchar(64) default null,
	updated_at datetime default null,
	updated_by varchar(64) default null,
	primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='物料信息表内容';

-- 原料， 仓库 关联数据信息内容, 原料可以存储不同的仓库信息
drop table if exists doctor_material_in_ware_houses;
create table doctor_material_in_ware_houses(
	id bigint(20) UNSIGNED	not null AUTO_INCREMENT comment 'id',
	farm_id bigint(20) unsigned not null comment '冗余仓库信息',
	farm_name varchar(64) default null comment '猪场姓名',
	ware_house_id bigint(20) unsigned not null comment '仓库信息',
	ware_house_name varchar(64) default null comment '仓库名称',
	material_id	bigint(20) not null comment '原料Id',
	material_name varchar(64) default null comment '原料名称',
	material_type_id bigint(20) not null comment '原料类型',
	material_type_name varchar(64) default null comment '原料名称',
	lot_number bigint(20) not null comment '数量信息',
	unit_group_name varchar(64) default null comment '单位组信息',
	unit_name varchar(64) default null comment '单位信息',
	extra text default null comment '扩展',
	created_at datetime default null,
	created_by varchar(64) default null,
	updated_at datetime default null,
	updated_by varchar(64) default null,
	primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='仓库原料信息表';

-- 原料信息表领用， 调用信息记录
drop table if exists doctor_material_consume_providers;
create table doctor_material_consume_providers(
	id bigint(20) unsigned not null AUTO_INCREMENT comment 'id',
	material_in_house_id bigint(20) unsigned not null comment '对应的仓库原料信息',
	farm_id bigint(20) unsigned not null comment '冗余仓库信息',
	farm_name varchar(64) default null comment '猪场姓名',
	ware_house_id bigint(20) unsigned not null comment '仓库信息',
	ware_house_name varchar(64) default null comment '仓库名称',
	material_id	bigint(20) not null comment '原料Id',
	material_name varchar(64) default null comment '原料名称',
	material_type_id bigint(20) not null comment '原料类型',
	material_type_name varchar(64) default null comment '原料名称',
	event_time datetime not null comment '事件日期',
	event_type SMALLINT not null comment '事件类型, provider 提供， consumer 消费',
	event_count bigint(20) not null comment '事件数量',
	staff_id bigint(20) unsigned not null comment '工作人员Id',
	staff_name varchar(64) not null comment '关联事件人',
	extra text default null comment '领用: 领用猪群的信息, 消耗天数。 提供： 供应商的名称， 相关信息',
	created_at datetime default null,
	created_by varchar(64) default null,
	updated_at datetime default null,
	updated_by varchar(64) default null,
	primary KEY(id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='领用记录信息表';
create index doctor_material_consume_provider_farm_id on doctor_material_consume_provider(farm_id);
create index doctor_material_consume_provider_ware_house_id on doctor_material_consume_provider(ware_house_id);
create index doctor_material_consume_provider_material_id on doctor_material_consume_provider(material_id);