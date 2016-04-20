
-- 后台类目表: parana_back_categories
drop table if exists `parana_back_categories`;
CREATE TABLE `parana_back_categories` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pid` bigint(20) NOT NULL COMMENT '父级id',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `level` tinyint(1) NOT NULL COMMENT '级别',
  `status` tinyint(1) NOT NULL COMMENT '状态,1启用,-1禁用',
  `has_children` tinyint(1) NOT NULL COMMENT '是否有孩子',
  `has_spu` tinyint(1) NOT NULL COMMENT '是否有spu关联',
  `outer_id` VARCHAR(256) NULL COMMENT '外部 id',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='后台类目表';
CREATE INDEX idx_back_categories_pid ON parana_back_categories (pid);


-- 品牌表: parana_brands
drop table if exists `parana_brands`;

CREATE TABLE `parana_brands` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '名称',
  `en_name` VARCHAR(100) NULL COMMENT '英文名称',
  `en_cap` CHAR(1) NULL COMMENT '首字母',
  `logo` VARCHAR(128) NULL COMMENT '品牌logo',
  `description` varchar(200)  NULL COMMENT '描述',
  `status` tinyint(1)  NULL COMMENT '状态,1启用,-1禁用',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='品牌表';
CREATE INDEX idx_brands_name ON parana_brands (name);
CREATE INDEX idx_brands_en_name ON `parana_brands` (`en_name`);

-- 类目属性表: parana_category_attributes
drop table if exists `parana_category_attributes`;

CREATE TABLE `parana_category_attributes` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `category_id` int(11) NOT NULL COMMENT '类目id',
  `attr_key` VARCHAR(20) NOT NULL COMMENT '属性名',
  `group` VARCHAR(20) NULL COMMENT '所属组名',
  `index` SMALLINT(3) NULL COMMENT '顺序编号',
  `status` tinyint(1) NOT NULL COMMENT '状态,1启用,-1删除',
  `attr_metas_json` varchar(255) NULL COMMENT 'json 格式存储的属性元信息',
  `attr_vals_json` VARCHAR(4096) NULL  COMMENT 'json 格式存储的属性值信息',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='品牌表';
CREATE INDEX idx_pca_category_id ON parana_category_attributes (category_id);

-- 前台类目表:
drop table if exists `parana_front_categories`;

CREATE TABLE `parana_front_categories` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pid` bigint(20) NOT NULL COMMENT '父级id',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `level` tinyint(1)  NULL COMMENT '级别',
  `has_children` tinyint(1)  NULL COMMENT '是否有孩子',
  `logo` VARCHAR(256) NULL COMMENT 'logo',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='前台类目表';
CREATE INDEX idx_front_categories_pid ON parana_front_categories (pid);

-- 前后台叶子类目映射表: parana_category_bindings
drop table if exists `parana_category_bindings`;

CREATE TABLE `parana_category_bindings` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `front_category_id` bigint(20) NOT NULL COMMENT '前台叶子类目id',
  `back_category_id` bigint(20) NOT NULL COMMENT '后台叶子类目id',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='前后台叶子类目映射表';

-- 店铺内类目表: parana_shop_categories
drop table if exists `parana_shop_categories`;

CREATE TABLE `parana_shop_categories` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(20) NOT NULL COMMENT '店铺id',
  `name` VARCHAR(20) NOT NULL COMMENT '类目名称',
  `pid` bigint(20) NOT NULL COMMENT '父级id',
  `level` tinyint(1) NOT NULL COMMENT '级别',
  `has_children` tinyint(1)  NULL COMMENT '是否有孩子',
  `has_item` tinyint(1)  NULL COMMENT '是否有商品关联',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
)COMMENT = '店铺内类目表';
CREATE INDEX idx_shopcats_shop_id ON `parana_shop_categories` (shop_id);

-- 店铺内类目和商品关联表: parana_shop_category_items
drop table if exists `parana_shop_category_items`;

CREATE TABLE `parana_shop_category_items` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(20) NOT NULL COMMENT '店铺id',
  `item_id` bigint(20) NOT NULL COMMENT '商品id',
  `item_name` varchar(100) NOT NULL COMMENT '商品名称',
  `shop_category_id` bigint(20) NOT NULL COMMENT '店铺内类目id',
  `shop_category_name` VARCHAR(20) NOT NULL COMMENT '店铺内类目名称',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
)COMMENT = '店铺内类目表';
CREATE INDEX idx_shopcis_shop_id ON `parana_shop_category_items` (shop_id);


-- spu表: parana_spus
drop table if exists `parana_spus`;

CREATE TABLE `parana_spus` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `spu_code` VARCHAR(40) NULL COMMENT 'spu编码',
  `category_id` int(11) UNSIGNED NOT NULL COMMENT '后台类目 ID',
  `brand_id` bigint(20)  NULL COMMENT '品牌id',
  `brand_name` varchar(100) NULL COMMENT '品牌名称',
  `name` varchar(200) NOT NULL COMMENT 'spu名称',
  `main_image` varchar(128)  NULL COMMENT '主图',
  `low_price` int(11) NULL COMMENT '实际售卖价格(所有sku的最低实际售卖价格)',
  `high_price` int(11) NULL COMMENT '实际售卖价格(所有sku的最高实际售卖价格)',
  `stock_type` TINYINT NULL COMMENT '库存类型, 0: 不分仓存储, 1: 分仓存储',
  `stock_quantity` int(11)  NULL COMMENT '库存',
  `status` tinyint(1) NOT NULL COMMENT '状态',
  `advertise` varchar(255) COMMENT '广告语',
  `specification` varchar(128) COMMENT '规格型号',
  `type` SMALLINT  NULL COMMENT 'spu类型 1为普通spu, 2为组合spu',
  `reduce_stock_type` SMALLINT DEFAULT 1 COMMENT '减库存方式, 1为拍下减库存, 2为付款减库存',
  `extra_json` VARCHAR(1024) COMMENT 'spu额外信息,建议json字符串',
  `spu_info_md5` CHAR(32) NULL COMMENT 'spu信息的m5值, 交易快照可能需要和这个摘要进行对比',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='SPU表';
CREATE UNIQUE INDEX idx_spus_spu_code ON `parana_spus` (`spu_code`);
CREATE INDEX idx_spus_cid ON `parana_spus` (`category_id`);

-- SPU详情: parana_spu_details
drop table if exists `parana_spu_details`;

CREATE TABLE `parana_spu_details` (
  `spu_id` bigint(20)  NOT NULL COMMENT 'spu id',
  `images_json` varchar(1024) DEFAULT NULL COMMENT '图片列表, json表示',
  `detail` text  NULL COMMENT '富文本详情',
  `packing_json` varchar(1024) COMMENT '包装清单,kv对, json表示',
  `service` text NULL COMMENT '售后服务',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`spu_id`)
) COMMENT='SPU详情';

-- spu属性: parana_spu_attributes
drop table if exists `parana_spu_attributes`;

CREATE TABLE `parana_spu_attributes` (
  `spu_id` bigint(20) NOT NULL COMMENT 'spu id',
  `sku_attributes` varchar(4096)  NULL COMMENT 'spu的sku属性, json存储',
  `other_attributes` varchar(8192)  NULL COMMENT 'spu的其他属性, json存储',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`spu_id`)
) COMMENT='spu属性';



-- SKU模板表: parana_skus
drop table if EXISTS `parana_sku_templates`;

CREATE TABLE `parana_sku_templates` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sku_code` VARCHAR(40) NULL COMMENT 'SKU 编码 (标准库存单位编码)',
  `spu_id` bigint(20) NOT NULL COMMENT '商品id',
  `specification` VARCHAR(50) NULL COMMENT '型号/款式',
  `status` TINYINT(1) NOT NULL COMMENT 'sku template 状态, 1: 上架, -1:下架,  -3:删除',
  `image` varchar(128)  NULL COMMENT '图片url',
  `thumbnail` VARCHAR(128) NULL COMMENT '样本图 (SKU 缩略图) URL',
  `name` VARCHAR(100) NULL COMMENT '名称',
  `extra_price_json` VARCHAR(255)  NULL COMMENT '其他各种价格的json表示形式',
  `price` int(11) NULL COMMENT '实际售卖价格',
  `attrs_json` varchar(1024)  NULL COMMENT 'json存储的sku属性键值对',
  `stock_type` TINYINT NOT NULL COMMENT '库存类型, 0: 不分仓存储, 1: 分仓存储, (冗余自SPU表)',
  `stock_quantity` int(11) DEFAULT NULL COMMENT '库存',
  `extra`     TEXT         DEFAULT NULL COMMENT 'sku额外信息',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='SKU模板表';
CREATE INDEX idx_skutmpls_spu_id ON `parana_sku_templates` (`spu_id`);
CREATE INDEX idx_skutmpls_sku_code ON `parana_sku_templates` (`sku_code`);


-- 商品表: parana_items
drop table if exists `parana_items`;

CREATE TABLE `parana_items` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `item_code` VARCHAR(40) NULL COMMENT '外部商品编码',
  `category_id` int(11) UNSIGNED NOT NULL COMMENT '后台类目 ID',
  `spu_id` int(11) NULL COMMENT 'SPU编号',
  `shop_id` int(11) NOT NULL COMMENT '店铺id',
  `shop_name` varchar(100) NOT NULL DEFAULT '' COMMENT '店铺名称',
  `brand_id` bigint(20) NULL COMMENT '品牌id',
  `brand_name` varchar(100) NULL COMMENT '品牌名称',
  `name` varchar(200) NOT NULL  COMMENT '商品名称',
  `main_image` varchar(128) DEFAULT NULL COMMENT '主图',
  `low_price` int(11) NULL COMMENT '实际售卖价格(所有sku的最低实际售卖价格)',
  `high_price` int(11) NULL COMMENT '实际售卖价格(所有sku的最高实际售卖价格)',
  `stock_type` TINYINT NULL COMMENT '库存类型, 0: 不分仓存储, 1: 分仓存储',
  `stock_quantity` int(11)  NULL COMMENT '库存',
  `sale_quantity` int(11)  NULL COMMENT '销量',
  `status` tinyint(1) NOT NULL COMMENT '状态 1: 上架, -1:下架, -2:冻结, -3:删除',
  `on_shelf_at` datetime  NULL COMMENT '上架时间',
  `advertise` varchar(255) COMMENT '广告语',
  `specification` varchar(128) COMMENT '规格型号',
  `type` SMALLINT  NULL COMMENT '商品类型 1为普通商品, 2为组合商品',
  `reduce_stock_type` SMALLINT DEFAULT 1 COMMENT '减库存方式, 1为拍下减库存, 2为付款减库存',
  `extra_json` VARCHAR(1024) NULL COMMENT '商品额外信息,建议json字符串',
  `tags_json` VARCHAR(1024) NULL COMMENT '商品标签的json表示形式,只能运营操作, 对商家不可见',
  `item_info_md5` CHAR(32) NULL COMMENT '商品信息的m5值, 商品快照需要和这个摘要进行对比',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='商品表';
CREATE INDEX idx_items_item_code ON `parana_items` (`item_code`);
CREATE INDEX idx_items_shop_id ON parana_items (shop_id);
CREATE INDEX idx_items_updated_at ON parana_items (updated_at);



-- 商品SKU表: parana_skus
drop table if EXISTS `parana_skus`;

CREATE TABLE `parana_skus` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sku_code` VARCHAR(40) NULL COMMENT 'SKU 编码 (标准库存单位编码)',
  `item_id` bigint(20) NOT NULL COMMENT '商品id',
  `shop_id` BIGINT UNSIGNED NOT NULL COMMENT '店铺 ID (冗余自商品表)',
  `status` TINYINT(1) NOT NULL COMMENT 'sku状态, 1: 上架, -1:下架, -2:冻结, -3:删除',
  `specification` VARCHAR(50) NULL COMMENT '型号/款式',
  `outer_sku_id` VARCHAR(32) NULL COMMENT '外部sku编号',
  `outer_shop_id` VARCHAR(32) NULL COMMENT '外部店铺id',
  `image` varchar(128)  NULL COMMENT '图片url',
  `thumbnail` VARCHAR(128) NULL COMMENT '样本图 (SKU 缩略图) URL',
  `name` VARCHAR(100) NULL COMMENT '名称',
  `extra_price_json` VARCHAR(255)  NULL COMMENT 'sku其他各种价格的json表示形式',
  `price` int(11) NULL COMMENT '实际售卖价格',
  `attrs_json` varchar(1024)  NULL COMMENT 'json存储的sku属性键值对',
  `stock_type` TINYINT NOT NULL COMMENT '库存类型, 0: 不分仓存储, 1: 分仓存储, (冗余自商品表)',
  `stock_quantity` int(11) DEFAULT NULL COMMENT '库存',
  `extra`     TEXT         DEFAULT NULL COMMENT 'sku额外信息',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='商品SKU表';
CREATE INDEX idx_skus_item_id ON `parana_skus` (`item_id`);
CREATE INDEX idx_skus_shop_id ON `parana_skus` (`shop_id`);
CREATE INDEX idx_skus_sku_code ON `parana_skus` (`sku_code`);

-- 商品详情: parana_item_details
drop table if exists `parana_item_details`;

CREATE TABLE `parana_item_details` (
  `item_id` bigint(20)  NULL COMMENT '商品id',
  `images_json` varchar(1024)  NULL COMMENT '图片列表, json表示',
  `detail` VARCHAR(2048)  NULL COMMENT '富文本详情',
  `packing_json` varchar(1024) COMMENT '包装清单,kv对, json表示',
  `service` VARCHAR(1024) NULL COMMENT '售后服务',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`item_id`)
) COMMENT='商品详情';

-- 商品属性: parana_item_attributes
drop table if exists `parana_item_attributes`;

CREATE TABLE `parana_item_attributes` (
  `item_id` bigint(20) NOT NULL COMMENT '商品id',
  `sku_attributes` varchar(4096)  NULL COMMENT '商品的sku属性, json存储',
  `other_attributes` varchar(8192)  NULL COMMENT '商品的其他属性, json存储',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`item_id`)
) COMMENT='商品属性';

-- 商品快照表: parana_item_snapshots
drop table if exists `parana_item_snapshots`;


-- 店铺表: parana_shops
drop table if exists `parana_shops`;

CREATE TABLE `parana_shops` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `outer_id` VARCHAR(32)  NULL COMMENT '外部店铺编码',
  `user_id` BIGINT(20) NOT NULL COMMENT '商家id',
  `user_name` VARCHAR(32) NOT NULL COMMENT '商家名称',
  `name` VARCHAR(64) NOT NULL COMMENT '店铺名称',
  `status`  TINYINT(1) NOT NULL COMMENT '状态 1:正常, -1:关闭, -2:冻结',
  `type` TINYINT(1) NOT NULL  COMMENT '店铺状态',
  `phone` varchar(32) NULL COMMENT '联系电话',
  `business_id` int(4)  NULL COMMENT '行业id',
  `image_url` varchar(128) NULL COMMENT '店铺图片url',
  `address` varchar(128) NULL COMMENT '店铺地址',
  `extra_json` VARCHAR(1024) NULL COMMENT '商品额外信息,建议json字符串',
  `tags_json` VARCHAR(1024) NULL COMMENT '商品标签的json表示形式,只能运营操作, 对商家不可见',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='店铺表';

-- 店铺附加表: parana_shop_extra
drop table if exists `parana_shop_extras`;

CREATE TABLE `parana_shop_extras` (
  `shop_id` bigint(20) NOT NULL COMMENT '店铺id',
  `deposit_cost` int(11) NULL COMMENT '保证金金额',
  `rate` int(4) NULL COMMENT '商家费率',
  `account` VARCHAR(64) NULL COMMENT '店铺名称',
  `account_type`  SMALLINT(1) NULL COMMENT '1:支付宝 2:银行卡',
  `account_name` VARCHAR(64) NULL  COMMENT '开户人姓名',
  `bank_name` varchar(32) NULL COMMENT '银行名称',
  `pre_im_id` VARCHAR(32)  NULL COMMENT '售前客服联系方式id',
  `post_im_id` varchar(32) NULL COMMENT '售后客服联系方式id',
  `commission_type` SMALLINT(4) NULL COMMENT '抽佣类型 1 费率 2 差价',
  `billing_period` SMALLINT(4) NULL COMMENT '帐期,  1、5、10、15、30五类',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`shop_id`)
) COMMENT='店铺附加表';



-- 用户表: parana_users
drop table if exists `parana_users`;

CREATE TABLE `parana_users` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(40) NULL COMMENT '用户名',
  `email` VARCHAR(32) NULL COMMENT '邮件',
  `mobile` VARCHAR(16) NULL COMMENT '手机号码',
  `password` VARCHAR(32) NULL COMMENT '登录密码',
  `type` SMALLINT NOT NULL COMMENT '用户类型',
  `status` tinyint(1) NOT NULL COMMENT '状态 0:未激活, 1:正常, -1:锁定, -2:冻结, -3: 删除',
  `roles_json` VARCHAR(512) NULL COMMENT '角色列表, 以json表示',
  `extra_json` VARCHAR(1024) NULL COMMENT '用户额外信息,建议json字符串',
  `tags_json` VARCHAR(1024) NULL COMMENT '用户标签的json表示形式,只能运营操作, 对用户不可见',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY idx_users_name(name),
  UNIQUE KEY idx_users_email(email),
  UNIQUE KEY idx_users_mobile(mobile)
) COMMENT='用户表';

-- 购物车商品
CREATE TABLE IF NOT EXISTS `parana_cart_items` (
  `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `buyer_id`       BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '买家ID',
  `shop_id`        BIGINT(20) UNSIGNED NOT NULL COMMENT '店铺ID',
  `sku_id`         BIGINT(20) UNSIGNED NOT NULL COMMENT 'SKU ID',
  `quantity`       INT(10) UNSIGNED    NOT NULL COMMENT '商品数量',
  `snapshot_price` INT                 NULL     COMMENT '快照价格',
  `extra_json` VARCHAR(1024) NULL COMMENT 'json储存的其他属性键值对',
  `created_at`     DATETIME DEFAULT NULL  COMMENT '创建时间',
  `updated_at`     DATETIME DEFAULT NULL  COMMENT '更新时间',
  PRIMARY KEY (`id`)
);

-- 订单节点实例表: parana_order_node_instances

DROP TABLE IF EXISTS `parana_order_node_instances`;

CREATE TABLE `parana_order_node_instances` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fid` bigint(20) NOT NULL COMMENT '订单流程ID',
  `first_node` tinyint NOT NULL COMMENT '首节点',
  `names` varchar(128) DEFAULT NULL COMMENT '节点名称(状态名称),对于同一个节点,角色不同对应的显示文案也不同',
  `desc` varchar(256) DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='订单流程节点实例表';

-- 订单动作实例表: parana_order_action_instances

DROP TABLE IF EXISTS `parana_order_action_instances`;

CREATE TABLE `parana_order_action_instances` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `node_instance_id` bigint(20) NOT NULL COMMENT '节点实例ID',
  `action` varchar(128) DEFAULT NULL COMMENT 'action bean id',
  `display` tinyint NOT NULL COMMENT '该动作是否显示',
  `type`    SMALLINT  NOT NULL COMMENT '动作类型 1->普通, 2->定时任务, 3->通知',
  `belong_user_types` VARCHAR(128) NOT NULL COMMENT '动作执行对象的用户类型 0:管理员, 1: 买家, 2: 卖家.一个用户类型的jsonList',
  `name` varchar(128) DEFAULT NULL COMMENT '动作名称',
  `desc`  VARCHAR(256)  DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT='动作实例表';

CREATE INDEX idx_order_action_instance_node_instance_id ON `parana_order_action_instances` (`node_instance_id`);

-- 订单流转规则表: parana_order_transfer_rules

DROP TABLE IF EXISTS `parana_order_transfer_rules`;

CREATE TABLE `parana_order_transfer_rules`(
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `start_node_instance_id` BIGINT NOT NULL COMMENT '流转起始节点id',
  `end_node_instance_id` BIGINT NOT NULL COMMENT '流转终止节点id',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
)COMMENT='流转规则表';

-- shopOrder 表: parana_shop_orders

DROP TABLE IF EXISTS `parana_shop_orders`;

CREATE TABLE `parana_shop_orders` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT NOT NULL COMMENT '父订单id, 没有则填-1',
  `parent_type` INT NOT NULL COMMENT '父订单类型, 没有则填-1',
  `flow_id` BIGINT NOT NULL COMMENT '流程id',
  `node_instance_id`  BIGINT NOT NULL COMMENT '订单当前所属节点id',
  `next_action_instance_ids`  VARCHAR(1024) NULL COMMENT '接下来可执行的actionId列表,用一个json对象描述每个角色可以进行的操作',
  `type` INT NOT NULL COMMENT '订单类型,同parent_type',
  `out_id` VARCHAR(64) NULL COMMENT '外部订单id',
  `out_from` VARCHAR(64) NULL COMMENT '外部订单来源',
  `extra_json` VARCHAR(2048)  NULL COMMENT '订单额外信息',
  `tags_json` VARCHAR(2048) NULL COMMENT '订单tag信息',
  `origin_fee`  INT NULL COMMENT '原价',
  `fee` INT NULL COMMENT '实付金额',
  `discount`  INT NULL COMMENT '优惠金额',
  `ship_fee`  INT NULL COMMENT '运费',
  `ship_fee_discount` INT NULL COMMENT '运费优惠金额',
  `integral`  INT NULL COMMENT '积分减免金额',
  `balance` INT NULL COMMENT '余额减免金额',
  `sale_tax`  INT NULL COMMENT '消费税',
  `ship_fee_sale_tax` INT NULL COMMENT '运费中包含的消费税',
  `buyer_id`  BIGINT  NULL COMMENT '买家id',
  `buyer_name` VARCHAR(64)  NULL COMMENT '买家名称',
  `out_buyer_id`  VARCHAR(64) NULL COMMENT '外部买家id',
  `shop_id` BIGINT  NULL COMMENT '店铺id',
  `shop_name` VARCHAR(64) NULL COMMENT '店铺名称',
  `out_shop_id` VARCHAR(64) NULL COMMENT '外部店铺id',
  `company_id`  BIGINT  NULL COMMENT '公司id',
  `deliver_type`  SMALLINT NULL COMMENT '配送方式',
  `pay_type`  SMALLINT NOT NULL COMMENT '支付类型, 1-在线支付 2-货到付款',
  `channel` SMALLINT NOT NULL COMMENT '订单渠道 1-手机 2-pc',
  `has_refund` tinyint NULL COMMENT '是否申请过逆向流程',
  `created_at`  DATETIME NOT NULL COMMENT '订单创建时间',
  `updated_at`  DATETIME  NOT NULL COMMENT '订单更新时间',
  PRIMARY KEY (`id`)
) COMMENT='店铺维度订单';

-- itemOrder 表: parana_sku_orders

DROP TABLE IF EXISTS `parana_sku_orders`;

CREATE TABLE `parana_sku_orders` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT NOT NULL COMMENT '父订单id, 没有则填-1',
  `parent_type` INT NOT NULL COMMENT '父订单类型, 没有则填-1',
  `flow_id` BIGINT NOT NULL COMMENT '流程id',
  `node_instance_id`  BIGINT NOT NULL COMMENT '订单当前所属节点id',
  `next_action_instance_ids`  VARCHAR(1024) NULL COMMENT '接下来可执行的actionId列表,用一个json对象描述每个角色可以进行的操作',
  `type` INT NOT NULL COMMENT '订单类型,同parent_type',
  `out_id` VARCHAR(64) NULL COMMENT '外部订单id',
  `out_from` VARCHAR(64) NULL COMMENT '外部订单来源',
  `extra_json` VARCHAR(2048)  NULL COMMENT '订单额外信息',
  `tags_json` VARCHAR(2048) NULL COMMENT '订单tag信息',
  `origin_fee`  INT NULL COMMENT '原价',
  `fee` INT NULL COMMENT '实付金额',
  `discount`  INT NULL COMMENT '优惠金额',
  `ship_fee`  INT NULL COMMENT '运费',
  `ship_fee_discount` INT NULL COMMENT '运费优惠金额',
  `integral`  INT NULL COMMENT '积分减免金额',
  `balance` INT NULL COMMENT '余额减免金额',
  `sale_tax`  INT NULL COMMENT '消费税',
  `ship_fee_sale_tax` INT NULL COMMENT '运费中包含的消费税',
  `buyer_id`  BIGINT  NULL COMMENT '买家id',
  `buyer_name` VARCHAR(64)  NULL COMMENT '买家名称',
  `out_buyer_id`  VARCHAR(64) NULL COMMENT '外部买家id',
  `shop_id` BIGINT  NULL COMMENT '店铺id',
  `shop_name` VARCHAR(64) NULL COMMENT '店铺名称',
  `out_shop_id` VARCHAR(64) NULL COMMENT '外部店铺id',
  `company_id`  BIGINT  NULL COMMENT '公司id',
  `sku_id`  BIGINT NOT NULL COMMENT '销售属性Id',
  `out_sku_id`  VARCHAR(64) NULL COMMENT '外部销售属性id',
  `sku_attributes`  VARCHAR(512) NULL COMMENT 'sku属性 json',
  `item_id` BIGINT  NULL COMMENT '商品id',
  `item_snapshot_id`  BIGINT  NULL COMMENT '商品快照id',
  `item_name` VARCHAR(64) NULL COMMENT '商品名称',
  `item_image`  VARCHAR(128)  NULL COMMENT '商品主图',
  `out_item_id` VARCHAR(64) NULL COMMENT '外部商品id',
  `quantity`  INT NULL COMMENT '购买数量',
  `deliver_type`  SMALLINT NULL COMMENT '配送方式',
  `pay_type`  SMALLINT NOT NULL COMMENT '支付类型, 1-在线支付 2-货到付款',
  `channel` SMALLINT NOT NULL COMMENT '订单渠道 1-手机 2-pc',
  `has_refund` tinyint NULL COMMENT '是否申请过逆向流程',
  `created_at`  DATETIME NOT NULL COMMENT '订单创建时间',
  `updated_at`  DATETIME  NOT NULL COMMENT '订单更新时间',
  PRIMARY KEY (`id`)
) COMMENT='sku维度订单';

-- 订单关联信息表

DROP TABLE IF EXISTS `parana_order_extra`;

CREATE TABLE `parana_order_extra` (
  `id`                    BIGINT          NOT NULL  AUTO_INCREMENT COMMENT '自增主键' ,
  `order_id`  BIGINT NOT NULL COMMENT '关联订单id',
  `order_type` SMALLINT NOT NULL COMMENT '订单类型',
  `channel` VARCHAR(64) NULL COMMENT '支付方式,alipay之类的',
  `trade_no`  VARCHAR(128)  NULL COMMENT '交易流水号',
  `batch_no`  VARCHAR(128)  NULL COMMENT '退款流水号',
  `payment_code`  VARCHAR(128)  NULL COMMENT '第三方交易流水号',
  `system_no`  VARCHAR(128)  NULL COMMENT '系统内部流水号',
  `trade_info`  VARCHAR(1024)  NULL COMMENT '收货地址',
  `buyer_notes` VARCHAR(256)  NULL COMMENT '买家留言',
  `seller_notes`  VARCHAR(256)  NULL COMMENT '卖家留言',
  `invoice` VARCHAR(1024) NULL COMMENT '发票信息',
  `paid_at` DATETIME  NULL COMMENT '支付时间',
  `buyer_cancel_at` DATETIME NULL COMMENT '买家取消订单时间',
  `seller_cancel_at`  DATETIME  NULL COMMENT '卖家取消订单时间',
  `deliver_at`  DATETIME  NULL COMMENT '发货时间',
  `done_at` DATETIME  NULL COMMENT '完成时间',
  `apply_at`  DATETIME  NULL COMMENT '申请时间',
  `check_at`  DATETIME  NULL COMMENT '审核时间',
  `buyer_deliver_at`  DATETIME  NULL COMMENT '买家发货时间',
  `seller_deliver_at` DATETIME  NULL COMMENT '卖家发货时间',
  `refund_at` DATETIME  NULL COMMENT '退款时间',
  `created_at`  DATETIME  NULL COMMENT '创建时间',
  `updated_at` DATETIME  NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) COMMENT='订单关联信息';
