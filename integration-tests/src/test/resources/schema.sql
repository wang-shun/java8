
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







CREATE TABLE parana_message_boxes(
    `id` BIGINT NOT NULL AUTO_INCREMENT  COMMENT '自增主键',
    `user_id`  BIGINT  NULL  COMMENT '用户id',
    `box_index`  INT  NULL  COMMENT '消息箱号',
    `notification_id`  BIGINT  NULL  COMMENT '站内信id',
    `created_at`  DATETIME  NULL  COMMENT '创建时间',
    `updated_at`  DATETIME  NULL  COMMENT '修改时间',
    PRIMARY KEY (`id`)
);



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
)  COMMENT='公司表';

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
)  COMMENT='猪场表';


CREATE TABLE `doctor_barns` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '猪舍名称',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类名称 枚举9种',
  `can_open_group` smallint(6) DEFAULT NULL COMMENT '能否建群 -1:不能, 1:能',
  `status` smallint(6) DEFAULT NULL COMMENT '使用状态 0:未用 1:在用 -1:已删除',
  `capacity` int(11) DEFAULT NULL COMMENT '猪舍容量',
  `staff_id` bigint(20) DEFAULT NULL COMMENT '工作人员id',
  `staff_name` varchar(64) DEFAULT NULL COMMENT '工作人员name',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='猪舍表';

CREATE TABLE `doctor_change_types` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(32) DEFAULT NULL COMMENT '变动类型名称',
  `is_count_out` smallint(6) DEFAULT NULL COMMENT '是否计入出栏猪 1:计入, -1:不计入',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='变动类型表';

CREATE TABLE `doctor_change_reasons` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `change_type_id` bigint(20) DEFAULT NULL COMMENT '变动类型id',
  `reason` varchar(128) DEFAULT NULL COMMENT '变动原因',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='变动原因表';

CREATE TABLE `doctor_customers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '客户名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `out_id`  varchar(128) DEFAULT NULL COMMENT  '外部id',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT  '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT  '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='客户表';

CREATE TABLE `doctor_groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `group_code` varchar(64) DEFAULT NULL COMMENT '猪群号',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '猪群批次号(雏鹰模式)',
  `open_at` datetime DEFAULT NULL COMMENT '建群时间',
  `close_at` datetime DEFAULT NULL COMMENT '关闭时间',
  `status` smallint(6) DEFAULT NULL COMMENT '枚举: 1:已建群, -1:已关闭',
  `init_barn_id` bigint(20) DEFAULT NULL COMMENT '初始猪舍id',
  `init_barn_name` varchar(64) DEFAULT NULL COMMENT '初始猪舍name',
  `current_barn_id` bigint(20) DEFAULT NULL COMMENT '当前猪舍id',
  `current_barn_name` varchar(64) DEFAULT NULL COMMENT '当前猪舍名称',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类 枚举9种',
  `breed_id` bigint(20) DEFAULT NULL COMMENT '品种id',
  `breed_name` varchar(64) DEFAULT NULL COMMENT '品种name',
  `genetic_id` bigint(20) DEFAULT NULL COMMENT '品系id',
  `genetic_name` varchar(64) DEFAULT NULL COMMENT '品系name',
  `staff_id` bigint(20) DEFAULT NULL COMMENT '工作人员id',
  `staff_name` varchar(64) DEFAULT NULL COMMENT '工作人员name',
  `remark` text COMMENT '备注',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='猪群卡片表';

CREATE TABLE `doctor_group_tracks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `group_id` bigint(20) DEFAULT NULL COMMENT '猪群卡片id',
  `rel_event_id` bigint(20) DEFAULT NULL COMMENT '关联的最新一次的事件id',
  `sex` smallint(6) DEFAULT NULL COMMENT '性别 0母 1公 2混合',
  `quantity` int(11) DEFAULT NULL COMMENT '猪只数',
  `boar_qty` int(11) DEFAULT NULL COMMENT '公猪数',
  `sow_qty` int(11) DEFAULT NULL COMMENT '母猪数',
  `birth_date` datetime DEFAULT NULL COMMENT '出生日期(此日期仅用于计算日龄)',
  `avg_day_age` int(11) DEFAULT NULL COMMENT '平均日龄',
  `weight` double DEFAULT NULL COMMENT '总活体重(公斤)',
  `avg_weight` double DEFAULT NULL COMMENT '平均体重(公斤)',
  `price` bigint(20) DEFAULT NULL COMMENT '单价(分)',
  `amount` bigint(20) DEFAULT NULL COMMENT '总金额(分)',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户id',
  `customer_name` varchar(64) DEFAULT NULL COMMENT '客户名称',
  `sale_qty` int(11) DEFAULT NULL COMMENT '销售数量',
  `extra` text COMMENT '附加字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='猪群卡片明细表';

CREATE TABLE `doctor_group_events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `group_id` bigint(20) DEFAULT NULL COMMENT '猪群卡片id',
  `group_code` varchar(64) DEFAULT NULL COMMENT '猪群号',
  `event_at` datetime DEFAULT NULL COMMENT '事件发生日期',
  `type` smallint(6) DEFAULT NULL COMMENT '事件类型 枚举 总共10种',
  `name` varchar(32) DEFAULT NULL COMMENT '事件名称 冗余枚举的name',
  `desc` varchar(512) DEFAULT NULL COMMENT '事件描述',
  `barn_id` bigint(20) DEFAULT NULL COMMENT '事件发生猪舍id',
  `barn_name` varchar(64) DEFAULT NULL COMMENT '事件发生猪舍name',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类枚举 9种',
  `quantity` int(11) DEFAULT '0' COMMENT '事件猪只数',
  `weight` double DEFAULT NULL COMMENT '总活体重(公斤)',
  `avg_weight` double DEFAULT NULL COMMENT '平均体重(公斤)',
  `avg_day_age` int(11) DEFAULT NULL COMMENT '平均日龄',
  `is_auto` smallint(6) DEFAULT NULL COMMENT '是否是自动生成事件, 0 不是, 1 是',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `remark` text COMMENT '备注',
  `extra` text COMMENT '具体事件的内容通过json存储',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人name',
  PRIMARY KEY (`id`)
)  COMMENT='猪群事件表';

-- 猪
CREATE TABLE `doctor_pigs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` bigint(20) unsigned DEFAULT NULL COMMENT '公司Id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场Id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `out_id` varchar(128) DEFAULT NULL COMMENT '关联猪外部Id',
  `pig_code` varchar(64) DEFAULT NULL COMMENT '猪编号',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪类型(公猪，母猪， 仔猪)',
  `is_removal` SMALLINT(6) DEFAULT NULL COMMENT '是否离场',
  `pig_father_code` VARCHAR(64) unsigned DEFAULT NULL COMMENT '猪父亲Id',
  `pig_mother_code` VARCHAR(64) unsigned DEFAULT NULL COMMENT '母猪Id',
  `source` smallint(6) DEFAULT NULL COMMENT '母猪来源',
  `birth_date` datetime DEFAULT NULL COMMENT '母猪生日',
  `birth_weight` double DEFAULT NULL COMMENT '出生重量',
  `in_farm_date` datetime DEFAULT NULL COMMENT '进厂日期',
  `in_farm_day_age` int(11) DEFAULT NULL COMMENT '进厂日龄',
  `init_barn_id` bigint(20) unsigned DEFAULT NULL COMMENT '进厂位置',
  `init_barn_name` varchar(64) DEFAULT NULL COMMENT '进厂位置名称',
  `breed_id` bigint(20) unsigned DEFAULT NULL COMMENT '品种id',
  `breed_name` varchar(64) DEFAULT NULL COMMENT '品种名称',
  `genetic_id` bigint(20) unsigned DEFAULT NULL COMMENT '品系Id',
  `genetic_name` varchar(64) DEFAULT NULL COMMENT '品系名称',
  `extra` text COMMENT '公猪（公猪类型，boar_type）母猪（初始胎次:init_parity, 性别：sex, 左右乳头数量： left_nipple_count, right_nipple_count ）',
  `remark` text COMMENT '标注信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='猪基础信息表';

-- 猪 track 信息关联表
CREATE TABLE `doctor_pig_tracks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `farm_id` bigint(20) unsigned NOT NULL comment '猪场Id',
  `pig_id` bigint(20) DEFAULT NULL COMMENT '猪id',
  `status` smallint(6) DEFAULT NULL COMMENT '猪状态信息',
  `is_removal` smallint(6) DEFAULT 0 COMMENT '是否离场',
  `current_barn_id` bigint(20) unsigned DEFAULT NULL COMMENT '当前猪舍Id',
  `current_barn_name` varchar(64) DEFAULT NULL COMMENT '当前猪舍名称',
  `weight` double DEFAULT NULL COMMENT '猪重量',
  `out_farm_date` datetime DEFAULT NULL COMMENT '猪离场时间',
  `rel_event_ids` text DEFAULT NULL COMMENT '关联事件最近事件',
  `extra` text COMMENT '事件修改猪对应信息',
  `current_parity` int(11) DEFAULT NULL COMMENT '当前胎次信息',
  `remark` text COMMENT '备注',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='猪Track 信息表';

-- 添加猪快照信息表数据内容
create table doctor_pig_snapshots(
	`id` bigint(20) unsigned not null AUTO_INCREMENT comment 'id',
	`org_id` bigint(20) unsigned default null comment 'org_id',
	`farm_id` bigint(20) unsigned default null comment 'farm_id',
	`pig_id` bigint(20) unsigned default null comment 'pig_id',
	`event_id` bigint(20) unsigned default null comment 'event_id',
	`pig_info` text default null comment '公猪快照信息',
	`created_at` datetime DEFAULT NULL,
  	`updated_at` datetime DEFAULT NULL,
  	 primary key(id)
) COMMENT='猪群事件记录数据表';

-- 公猪，母猪， 仔猪事件信息表
CREATE TABLE `doctor_pig_events` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `org_id` bigint(20) unsigned DEFAULT NULL COMMENT '公司Id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场Id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `pig_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪Id',
  `pig_code` varchar(64) DEFAULT NULL COMMENT '猪Code',
  `event_at` datetime DEFAULT NULL COMMENT '事件时间',
  `type` int(11) DEFAULT NULL COMMENT '事件类型',
  `kind` int(11) DEFAULT NULL COMMENT '事件猪类型， 公猪， 母猪， 仔猪',
  `name` varchar(64) DEFAULT NULL COMMENT '事件名称',
  `desc` varchar(512) DEFAULT NULL COMMENT '事件描述',
  `barn_id` bigint(20) unsigned DEFAULT NULL COMMENT '事件地点',
  `barn_name` varchar(64) DEFAULT NULL COMMENT '地点名称',
  `rel_event_id` bigint(20) DEFAULT NULL COMMENT '关联事件Id',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部Id',
  `extra` text COMMENT '参考设计文档',
  `remark` text COMMENT '备注信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='用户设备信息表';

-- 猪只设置免疫程序统计方式
drop Table if exists doctor_vaccination_pig_warns;
create table doctor_vaccination_pig_warns (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `pig_type` smallint(6) unsigned DEFAULT NULL comment '猪类id',
  `material_id` bigint(20) default null comment '疫苗Id',
  `material_name` varchar(128) default null comment '疫苗名称',
  `start_date` datetime default null comment '开始时间',
  `end_date` datetime default null comment '结束时间',
  `vaccination_date_type` bigint(20) default null comment '免疫日期类型, 枚举类VaccinationDateType',
  `input_value` smallint(6) default null comment '录入的天数/重量',
  `input_date` datetime default null comment '录入的日期',
  `dose` bigint(20) default null comment '消耗剂量',
  `remark` varchar(128) default null comment '备注',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='猪只设置免疫程序统计方式';



drop table if exists doctor_farm_ware_house_types;
create table doctor_farm_ware_house_types(
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `type` SMALLINT(6) unsigned DEFAULT NULL COMMENT '猪场仓库类型',
  `lot_number` bigint(20) DEFAULT NULL COMMENT '类型原料的数量',
  `extra` text default null comment '扩展字段',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
   primary key(id)
) COMMENT='猪场仓库类型数量';


CREATE TABLE `doctor_material_infos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `type` smallint(6) DEFAULT NULL comment '物料所属原料的名称',
  `material_name` VARCHAR (128) DEFAULT NULL comment '物料名称',
  `input_code` VARCHAR(128) DEFAULT NULL comment '输入码信息',
  `remark` text COMMENT '标注',
  `unit_group_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位组Id',
  `unit_group_name` varchar(64) DEFAULT NULL COMMENT '单位组名称',
  `unit_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位Id',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '单位名称',
  `default_consume_count` bigint(20) DEFAULT NULL COMMENT '原料默认消耗数量',
  `price` bigint(20) DEFAULT NULL COMMENT '价格',
  `extra` text COMMENT '扩展信息: 药品-默认计量的大小',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='物料信息表内容';

CREATE TABLE `doctor_ware_houses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `manager_id` bigint(20) unsigned DEFAULT NULL COMMENT '管理员Id',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '管理人员姓名',
  `address` varchar(64) DEFAULT NULL COMMENT '地址信息',
  `type` smallint(6) DEFAULT NULL comment '仓库类型，一个仓库只能属于一个',
  `extra` text DEFAULT NULL comment '扩展信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='仓库信息数据表';


CREATE TABLE `doctor_ware_house_tracks` (
  `ware_house_id` bigint(20) unsigned NOT NULL COMMENT 'ware_house_id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `manager_id` bigint(20) unsigned DEFAULT NULL COMMENT '管理员Id',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '管理人员姓名',
  `material_lot_number` text DEFAULT NULL comment '各种原料的数量信息',
  `lot_number` bigint(20) DEFAULT NULL comment '仓库物品的总数量信息',
  `is_default` smallint(6) DEFAULT NULL COMMENT '默认仓库信息',
  `extra` text DEFAULT NULL comment '扩展信息',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`ware_house_id`)
)  COMMENT='仓库信息Track数据表';



CREATE TABLE `doctor_material_in_ware_houses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场姓名',
  `ware_house_id` bigint(20) unsigned DEFAULT NULL COMMENT '仓库信息',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `material_id` bigint(20) DEFAULT NULL COMMENT '原料Id',
  `material_name` varchar(64) DEFAULT NULL COMMENT '原料名称',
  `type` smallint(6) DEFAULT NULL comment '仓库类型, 冗余',
  `lot_number` bigint(20) DEFAULT NULL COMMENT '数量信息',
  `unit_group_name` varchar(64) DEFAULT NULL COMMENT '单位组信息',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '单位信息',
  `extra` text COMMENT '扩展',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='仓库原料信息表';

CREATE TABLE `doctor_material_consume_providers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` bigint(20) unsigned DEFAULT NULL COMMENT '领取货物属于的类型',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场姓名',
  `ware_house_id` bigint(20) unsigned DEFAULT NULL COMMENT '仓库信息',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `material_id` bigint(20) DEFAULT NULL COMMENT '原料Id',
  `material_name` varchar(64) DEFAULT NULL COMMENT '原料名称',
  `event_time` datetime DEFAULT NULL COMMENT '事件日期',
  `event_type` smallint(6) DEFAULT NULL COMMENT '事件类型, provider 提供， consumer 消费',
  `event_count` bigint(20) DEFAULT NULL COMMENT '事件数量',
  `staff_id` bigint(20) unsigned DEFAULT NULL COMMENT '工作人员Id',
  `staff_name` varchar(64) DEFAULT NULL COMMENT '关联事件人',
  `extra` text COMMENT '领用: 领用猪群的信息, 消耗天数。 提供： 供应商的名称， 相关信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='领用记录信息表';



-- 物料消耗的平均数量统计
-- 物料消耗的平均数量统计
drop table if exists doctor_material_consume_avgs;
CREATE TABLE `doctor_material_consume_avgs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `ware_house_id` bigint(20) unsigned DEFAULT NULL COMMENT '仓库信息',
  `material_id` bigint(20) DEFAULT NULL COMMENT '原料Id',
  `type` smallint(6) unsigned DEFAULT NULL COMMENT '领取货物属于的类型',
  `lot_consume_day` int(11) DEFAULT NULL COMMENT '剩余消耗天数',
  `consume_avg_count` bigint(20) DEFAULT NULL COMMENT '平均消耗数量',
  `consume_count` bigint(20) DEFAULT NULL COMMENT '消耗数量',
  `consume_date` datetime DEFAULT NULL comment '消耗日期',
  `extra` text DEFAULT NULL comment 'extra',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物料消耗信息统计方式';


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




CREATE TABLE `doctor_user_operators` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户 ID',
  `role_id` bigint(20) DEFAULT NULL COMMENT '运营角色 ID',
  `status` tinyint(4) DEFAULT NULL COMMENT '运营状态',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '运营额外信息, 建议json字符串',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
  -- ,KEY `idx_user_operator_user_id` (`user_id`),
  -- KEY `idx_user_operator_role_id` (`role_id`)
)  COMMENT='用户运营表';

CREATE TABLE `doctor_operator_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL COMMENT '用户名',
  `desc` varchar(32) DEFAULT NULL COMMENT '角色描述',
  `app_key` varchar(16) DEFAULT NULL COMMENT '角色所属',
  `status` smallint(6) DEFAULT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` varchar(1024) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='运营角色表';

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
  PRIMARY KEY (`id`)
  -- ,KEY `idx_user_sub_user_id` (`user_id`),
  -- KEY `idx_user_parent_sub_id` (`parent_user_id`),
  -- KEY `idx_user_sub_roles_id` (`role_id`)
) COMMENT='猪场子账户表';

CREATE TABLE `doctor_sub_roles` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL COMMENT '用户名',
  `desc` varchar(32) DEFAULT NULL COMMENT '角色描述',
  `user_id` bigint(20) DEFAULT NULL COMMENT '所属主账号ID',
  `app_key` varchar(16) DEFAULT NULL COMMENT '角色所属',
  `status` smallint(6) DEFAULT NULL COMMENT '0. 未生效(冻结), 1. 生效, -1. 删除',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '用户额外信息,建议json字符串',
  `allow_json` varchar(1024) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
  -- ,KEY `idx_sub_roles_user_id` (`user_id`)
)  COMMENT='子账号角色表';


CREATE TABLE `doctor_user_primarys` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户 ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名 (冗余)',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态',
  `extra_json` varchar(1024) DEFAULT NULL COMMENT '用户额外信息, 建议json字符串',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
  -- ,KEY `idx_user_primary_user_id` (`user_id`)
)  COMMENT='猪场主账户表';




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
)  COMMENT='用户数据权限表';


CREATE TABLE `doctor_service_reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `user_mobile` VARCHAR (16) DEFAULT NULL COMMENT '用户手机号,冗余字段',
  `real_name` VARCHAR (16) DEFAULT NULL COMMENT '用户申请服务时填写的真实姓名',
  `type` smallint(6) DEFAULT NULL COMMENT  '服务类型 1 猪场软件, 2 新融电商, 3 大数据, 4 生猪交易',
  `status` smallint(6) DEFAULT NULL COMMENT '审核状态 0 未审核, 2 待审核(提交申请) 1 通过，-1 不通过, -2 冻结',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审批人id',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='用户服务审批表';

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

CREATE TABLE `doctor_group_snapshots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `event_type` smallint(6) DEFAULT NULL COMMENT '猪群事件类型',
  `from_group_id` bigint(20) DEFAULT NULL COMMENT '操作前的猪群id',
  `to_group_id` bigint(20) DEFAULT NULL COMMENT '操作后的猪群id',
  `from_event_id` bigint(20) DEFAULT NULL COMMENT '操作前的事件id',
  `to_event_id` bigint(20) DEFAULT NULL COMMENT '操作后的事件id',
  `from_info` text COMMENT '操作前的信息',
  `to_info` text COMMENT '操作后的信息',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
)  COMMENT='猪群快照表';


CREATE TABLE `doctor_revert_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `type` smallint(6) DEFAULT NULL COMMENT '回滚类型 1 母猪，2 公猪，3 猪群',
  `from_info` text COMMENT  '回滚前的信息',
  `to_info` text COMMENT  '回滚后的信息',
  `reverter_id` bigint(20) DEFAULT NULL COMMENT '回滚人id',
  `reverter_name` varchar(64) DEFAULT NULL COMMENT '回滚人姓名',
  `created_at` datetime DEFAULT NULL COMMENT '回滚时间',
  PRIMARY KEY (`id`)
)  COMMENT='回滚记录表';

CREATE TABLE `doctor_carousel_figures` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `index` int(11) DEFAULT NULL COMMENT '轮播图顺序, asc排序',
  `status` smallint(6) DEFAULT NULL COMMENT '状态: 1 启用, -1 不启用',
  `url` varchar(512) DEFAULT NULL COMMENT '轮播图链接地址',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)  COMMENT='轮播图表';

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

-- 消息中心 2016-06-06
CREATE TABLE `doctor_message_rule_template_tracks` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则, 是farm对应的默认值, json值, 类: Rule',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`message_template`	VARCHAR(128) DEFAULT NULL COMMENT '规则数据模板名称, 对应parana_message_templates表name字段',
	`content`	TEXT DEFAULT NULL COMMENT '规则的内容, 针对系统消息',
	`producer`	VARCHAR(128) DEFAULT NULL COMMENT '消息生成者(类的简单类名)',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则模板描述',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	`updator_id`	BIGINT(20) DEFAULT NULL COMMENT '修改人id',
	`updator_name`	VARCHAR(32) DEFAULT NULL COMMENT '修改人name',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则模板表(历史跟踪)';

CREATE TABLE `doctor_message_rule_templates` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则, 是farm对应的默认值, json值, 类: Rule',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`message_template`	VARCHAR(128) DEFAULT NULL COMMENT '规则数据模板名称, 对应parana_message_templates表name字段',
	`content`	TEXT DEFAULT NULL COMMENT '规则的内容, 针对系统消息',
	`producer`	VARCHAR(128) DEFAULT NULL COMMENT '消息生成者(类的简单类名)',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则模板描述',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	`updated_by`	BIGINT(20) DEFAULT NULL COMMENT '修改人id',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则模板表';

CREATE TABLE `doctor_message_rules` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`template_name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则值, 是role对应表的默认值, json值, 类: Rule',
	`use_default`	SMALLINT(6) DEFAULT NULL COMMENT '是否使用默认配置, 0:不使用, 1:使用',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:正常, -1:删除, -2:禁用',
	`describe`	VARCHAR(1024) DEFAULT NULL COMMENT '消息规则模板描述',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则表';

CREATE TABLE `doctor_message_rule_roles` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`rule_id`	BIGINT(20) DEFAULT NULL COMMENT '消息规则id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`role_id`	BIGINT(20) DEFAULT NULL COMMENT '子账号的角色id',
	`rule_value`	TEXT DEFAULT NULL COMMENT '规则值, json值, 类: Rule',
	`use_default`	SMALLINT(6) DEFAULT NULL COMMENT '是否使用默认配置, 0:不使用, 1:使用',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息规则与角色表';

CREATE TABLE `doctor_messages` (
	`id`	BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`farm_id`	BIGINT(20) DEFAULT NULL COMMENT '猪场id',
	`rule_id`	BIGINT(20) DEFAULT NULL COMMENT '消息规则id',
	`role_id`	BIGINT(20) DEFAULT NULL COMMENT '子账号的角色id',
	`user_id` BIGINT(20) DEFAULT NULL COMMENT '用户id',
	`template_id` 	BIGINT(20) DEFAULT NULL COMMENT '消息规则模板id',
	`template_name` 	VARCHAR(128) DEFAULT NULL COMMENT '消息规则模板名称',
	`message_template`	VARCHAR(128) DEFAULT NULL COMMENT '规则数据模板名称, 对应parana_message_templates表name字段',
	`type` 	SMALLINT(6) DEFAULT NULL COMMENT '消息类型: 0->系统消息, 1->预警消息, 2->警报消息',
	`category`	SMALLINT(6) DEFAULT NULL COMMENT '消息种类',
	`data`	TEXT DEFAULT NULL COMMENT '发送的内容填充数据, json(map). 或系统消息',
	`content`	TEXT DEFAULT NULL COMMENT '发送的内容(模板编译之后)',
	`channel`	SMALLINT(6) DEFAULT NULL COMMENT '消息发送渠道. 0->站内信, 1->短信, 2->邮箱, 3->app推送',
	`url`		VARCHAR(4096)	DEFAULT NULL COMMENT 'app回调url',
	`status`	SMALLINT(6) DEFAULT NULL COMMENT '状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败',
	`sended_at`	DATETIME DEFAULT NULL COMMENT '发送时间',
	`failed_by`	VARCHAR(4096) DEFAULT NULL COMMENT '失败原因',
	`created_by` BIGINT(20) DEFAULT NULL COMMENT '操作人id',
	`created_at`	DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`	DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
)COMMENT='猪场软件消息表';


-- -----------------------------------------------------------------
-- ------- 流程定义表 ------------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_definitions`;
CREATE TABLE IF NOT EXISTS `workflow_definitions` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识, 按照版本号区分',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程定义name属性名称',
	`version` BIGINT(20) DEFAULT NULL COMMENT '版本号，用于获取最新的流程定义',
	`resource_name` VARCHAR(128) DEFAULT NULL COMMENT '资源文件的名称(通常指流程定义的xml文件)',
	`resource` text COMMENT '流程定义的资源内容(通常是xml中的内容)',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程定义的状态(1:正常, -1:删除, -2:禁用)',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '发布者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '发布者姓名',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程定义表';


-- -----------------------------------------------------------------
-- ------- 流程节点表 ------------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_definition_nodes`;
CREATE TABLE IF NOT EXISTS `workflow_definition_nodes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`name` VARCHAR(32) DEFAULT NULL COMMENT '节点name属性名称',
	`value` VARCHAR(32) DEFAULT NULL COMMENT '节点value属性值',
	`node_name` VARCHAR(32) DEFAULT NULL COMMENT '节点标签名称',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '节点类型, 1->开始节点, 2->任务节点, 3->选择节点, 4->并行节点, 5->并行汇聚节点, 10->子流程开始节点, -10->子流程结束节点, -1->结束节点',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`timer` VARCHAR(32) DEFAULT NULL COMMENT '定时表达式',
	`point_x` DOUBLE DEFAULT NULL COMMENT '节点x轴偏移量',
	`point_y` DOUBLE DEFAULT NULL COMMENT '节点y轴偏移量',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程定义节点表';

-- -----------------------------------------------------------------
-- ------- 流程节点事件驱动表 -----------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_definition_node_events`;
CREATE TABLE IF NOT EXISTS `workflow_definition_node_events`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '连线name属性名称',
	`value` VARCHAR(32) DEFAULT NULL COMMENT '连线value属性值',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id,冗余',
	`source_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程源节点的id',
	`handler` VARCHAR(128) DEFAULT NULL COMMENT '事件驱动处理类(一般为类标识)',
	`expression` text COMMENT '事件驱动表达式',
	`target_node_id` BIGINT(20) DEFAULT NULL COMMENT '驱动的目标节点id',
	`describe` text COMMENT '连线描述',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程定义节点连线事件表';



-- -----------------------------------------------------------------
-- ------- 流程实例表 ------------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_process_instances`;
CREATE TABLE IF NOT EXISTS `workflow_process_instances`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程实例的名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`flow_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识,冗余',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '与流程实例相关联的业务id',
	`business_data` text COMMENT '流程实例全局业务数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程实例的状态, 1->正常, 2->正常结束, -1->删除, -2->挂起',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '流程实例类型, 1-> 主流程, 2-> 子流程',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`parent_instance_id` BIGINT(20) DEFAULT NULL COMMENT '父流程实例id',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例表';



-- -----------------------------------------------------------------
-- ------- 当前流程实例的活动节点表 ------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_processes`;
CREATE TABLE IF NOT EXISTS `workflow_processes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态, 1->正常, 2->正常结束, -1->删除, -2->挂起',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留),该值优先于流程定义节点的值',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例的当前活动节点表';


-- -----------------------------------------------------------------
-- ------- 流程处理节点跟踪表 -----------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_process_tracks`;
CREATE TABLE IF NOT EXISTS `workflow_process_tracks`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态,冗余workflow_process表',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程处理节点跟踪表';


-- -----------------------------------------------------------------
-- ------- 流程实例历史表 --------------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_history_process_instances`;
CREATE TABLE IF NOT EXISTS `workflow_history_process_instances`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`name` VARCHAR(128) DEFAULT NULL COMMENT '流程实例的名称',
	`flow_definition_id` BIGINT(20) DEFAULT NULL COMMENT '流程定义id',
	`flow_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义唯一标识,冗余',
	`business_id` BIGINT(20) DEFAULT NULL COMMENT '与流程实例相关联的业务id',
	`business_data` text COMMENT '流程实例全局业务数据',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程实例的状态,冗余流程实例表',
	`type` SMALLINT(6) DEFAULT NULL COMMENT '流程实例类型, 1-> 主流程, 2-> 子流程',
	`describe` text COMMENT '历史流程描述',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`parent_instance_id` BIGINT(20) DEFAULT NULL COMMENT '父流程实例id',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例历史表';

-- -----------------------------------------------------------------
-- ------- 流程实例的活动节点历史表 ------------------------------------
-- -----------------------------------------------------------------
DROP TABLE IF EXISTS `workflow_history_processes`;
CREATE TABLE IF NOT EXISTS `workflow_history_processes`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`flow_definition_node_id` BIGINT(20) DEFAULT NULL COMMENT '流程节点的id',
	`pre_flow_definition_node_id` VARCHAR(128) DEFAULT NULL COMMENT '上一个流程节点的id, 可能存在多个, 用逗号隔开',
	`flow_instance_id` BIGINT(20) DEFAULT NULL COMMENT '流程实例的id',
	`flow_data` text COMMENT '流程节点之间的暂时性流转数据',
	`describe` text COMMENT '历史节点流转描述',
	`status` SMALLINT(6) DEFAULT NULL COMMENT '流程节点的状态,冗余workflow_process表',
	`operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作者id',
	`operator_name` VARCHAR(32) DEFAULT NULL COMMENT '操作者姓名',
	`assignee` VARCHAR(32) DEFAULT NULL COMMENT '处理人(暂时保留)',
	`fork_node_id` BIGINT(20) DEFAULT NULL COMMENT 'fork节点id, 便于join',
	`created_at` DATETIME DEFAULT NULL COMMENT '创建时间',
	`updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY(`id`)
) COMMENT='流程实例的活动节点历史表';

CREATE TABLE `parana_addresses` (
  `id` int(11) NOT NULL,
  `pid` int(11) DEFAULT NULL COMMENT '父级ID',
  `name` varchar(50) DEFAULT NULL COMMENT '名称',
  `level` int(3) DEFAULT NULL COMMENT '级别',
  `pinyin` varchar(100) DEFAULT NULL COMMENT '拼音',
  `english_name` varchar(100) DEFAULT NULL COMMENT '英文名',
  `unicode_code` varchar(200) DEFAULT NULL COMMENT 'ASCII码',
  `order_no` varchar(32) DEFAULT NULL COMMENT '排序号',
  PRIMARY KEY (`id`)
);

CREATE TABLE `doctor_pig_type_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `boar` int(11) DEFAULT NULL COMMENT '公猪数',
  `sow` int(11) DEFAULT NULL COMMENT '母猪数',
  `farrow` int(11) DEFAULT NULL COMMENT '产房仔猪数',
  `nursery` int(11) DEFAULT NULL COMMENT '保育猪数',
  `fatten` int(11) DEFAULT NULL COMMENT '育肥猪数',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_pig_type_statistics_farm_id` (`farm_id`),
  KEY `idx_doctor_pig_type_statistics_org_id` (`org_id`)
) COMMENT='猪只数统计表';

-- 2016-06-27 增加基础表
CREATE TABLE `doctor_basics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(32) DEFAULT NULL COMMENT '基础数据内容',
  `type` smallint(6) DEFAULT NULL COMMENT '基础数据类型 枚举',
  `type_name` varchar(32) DEFAULT NULL COMMENT '数据类型名称',
  `is_valid` smallint(6) DEFAULT NULL COMMENT '逻辑删除字段, -1 表示删除',
  `srm` varchar(32) DEFAULT NULL COMMENT '输入码(快捷输入用)',
  `context` varchar(64) DEFAULT NULL COMMENT '基础数据内容',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '更新人id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '更新人name',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='基础数据表';