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



CREATE TABLE `doctor_material_price_in_ware_houses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '冗余仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场姓名',
  `ware_house_id` bigint(20) unsigned NOT NULL COMMENT '仓库信息',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `material_id` bigint(20) NOT NULL COMMENT '物料Id',
  `material_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `type` smallint(6) DEFAULT NULL COMMENT '仓库类型, 冗余',
  `provider_id` bigint(20) NOT NULL COMMENT '入库事件id',
  `unit_price` bigint(20) NOT NULL COMMENT '本次入库单价，单位为“分”',
  `remainder` decimal(23,3) NOT NULL COMMENT '本次入库量的剩余量，比如本次入库100个，那么就是这100个的剩余量，减少到0时删除',
  `provider_time` datetime NOT NULL COMMENT '入库时间，冗余字段',
  `extra` text COMMENT '扩展',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `provider_id_UNIQUE` (`provider_id`),
  KEY `ware_house_id_index` (`ware_house_id`)
) COMMENT='仓库中各物料每次入库的剩余量';



CREATE TABLE `doctor_basic_materials` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `type` smallint(6) DEFAULT NULL COMMENT '物料类型',
  `sub_type` bigint(20) unsigned DEFAULT NULL COMMENT '物料的子类别，关联 doctor_basics 表的id',
  `name` varchar(128) DEFAULT NULL COMMENT '物料名称',
  `srm` varchar(32) DEFAULT NULL,
  `is_valid` smallint(6) NOT NULL DEFAULT '1' COMMENT '逻辑删除字段, -1 表示删除, 1 表示可用',
  `unit_group_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位组id',
  `unit_group_name` varchar(64) DEFAULT NULL COMMENT '单位组名称',
  `unit_id` bigint(20) unsigned DEFAULT NULL COMMENT '单位id',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '单位名称',
  `default_consume_count` int(11) DEFAULT NULL COMMENT '默认消耗数量',
  `price` bigint(20) DEFAULT NULL COMMENT '价格(元)',
  `remark` text COMMENT '标注',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='基础物料表';

CREATE TABLE `doctor_farm_basics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `basic_ids` text COMMENT '基础数据ids, 逗号分隔',
  `reason_ids` text COMMENT '变动原因ids, 逗号分隔',
  `material_ids` text COMMENT '物料基础数据ids',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doctor_farm_basics_farm_id` (`farm_id`)
)  COMMENT='猪场基础数据关联表';

CREATE TABLE `doctor_ware_houses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ware_house_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场仓库信息',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `manager_id` bigint(20) unsigned DEFAULT NULL COMMENT '管理员Id',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '管理人员姓名',
  `address` varchar(64) DEFAULT NULL COMMENT '地址信息',
  `type` smallint(6) DEFAULT NULL COMMENT '仓库类型，一个仓库只能属于一个',
  `extra` text COMMENT '扩展信息',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `doctor_ware_houses_farm_id` (`farm_id`)
) COMMENT='仓库信息数据表';

-- 库存表
CREATE TABLE `doctor_warehouse_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `warehouse_type` smallint(6) DEFAULT NULL COMMENT '仓库类型，冗余，方便查询',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `sku_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `sku_id` bigint(20) DEFAULT NULL COMMENT '物料编号',
  `quantity` decimal(23,2) DEFAULT NULL COMMENT '数量',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_warehouse_sku` (`warehouse_id`,`sku_id`)
)COMMENT='仓库物料库存表';


CREATE TABLE `doctor_warehouse_purchase` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `warehouse_type` smallint(6) DEFAULT NULL COMMENT '仓库类型',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物料编号',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '物料供应商名称',
  `unit_price` bigint(20) DEFAULT NULL COMMENT '单价，单位分',
  `quantity` decimal(23,2) DEFAULT NULL COMMENT '数量',
  `handle_date` date DEFAULT NULL COMMENT '处理日期',
  `handle_year` smallint(12) DEFAULT NULL COMMENT '处理年',
  `handle_month` tinyint(4) DEFAULT NULL COMMENT '处理月份',
  `handle_quantity` decimal(23,2) DEFAULT NULL COMMENT '已出库的数量',
  `handle_finish_flag` tinyint(2) DEFAULT NULL COMMENT '是否该批入库已出库完。0出库完，1未出库完。handle_quantity=quantity就表示出库完',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) COMMENT='仓库物料入库表';

CREATE TABLE `doctor_warehouse_material_handle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `stock_handle_id` bigint(20) DEFAULT NULL COMMENT '库存处理ID',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `warehouse_type` smallint(6) DEFAULT NULL COMMENT '仓库类型',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名称',
  `other_transfer_handle_id` bigint(20) DEFAULT NULL COMMENT '另一条调拨物料处理单的编号',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '物料供应商名称',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `material_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `type` tinyint(4) NOT NULL COMMENT '处理类别，入库，出库，调拨，盘点',
  `unit_price` bigint(20) NOT NULL COMMENT '单价，单位分',
  `unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `delete_flag` tinyint(2) DEFAULT '1' COMMENT '删除标志',
  `before_inventory_quantity` decimal(23,2) DEFAULT NULL COMMENT '盘点前库存数量',
  `quantity` decimal(23,2) NOT NULL COMMENT '数量',
  `handle_date` datetime DEFAULT NULL COMMENT '处理日期',
  `handle_year` smallint(12) NOT NULL COMMENT '处理年',
  `handle_month` tinyint(4) NOT NULL COMMENT '处理月',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人编号',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名',
  `remark` varchar(64) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='仓库物料处理表';


CREATE TABLE `doctor_warehouse_handle_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细编号',
  `material_purchase_id` bigint(20) NOT NULL COMMENT '物料采购记录编号',
  `material_handle_id` bigint(20) NOT NULL COMMENT '物料处理记录编号',
  `handle_year` smallint(6) DEFAULT NULL,
  `handle_month` tinyint(2) DEFAULT NULL,
  `quantity` decimal(23,2) NOT NULL COMMENT '处理数量',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_material_handle_id` (`material_handle_id`)
) COMMENT='仓库物料处理明细';

CREATE TABLE `doctor_material_vendor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `vendor_name` varchar(64) NOT NULL COMMENT '供应商名称',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物料供应商表';
CREATE TABLE `doctor_material_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库编号',
  `material_id` bigint(20) DEFAULT NULL COMMENT '物料编号',
  `vendor_name` varchar(64) DEFAULT NULL COMMENT '供应商名',
  `specification` varchar(64) DEFAULT NULL COMMENT '规格',
  `code` varchar(64) DEFAULT NULL COMMENT '编码',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='物料编码表';

CREATE TABLE `doctor_warehouse_stock_handle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `farm_id` bigint(20) NOT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名',
  `warehouse_type` tinyint(4) DEFAULT NULL COMMENT '仓库类型',
  `serial_no` varchar(45) NOT NULL COMMENT '流水号',
  `handle_date` date NOT NULL COMMENT '处理日期',
  `handle_sub_type` tinyint(4) DEFAULT NULL COMMENT '事件子类型',
  `handle_type` tinyint(4) DEFAULT NULL COMMENT '事件类型',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '创建人名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `operator_id` bigint(20) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  KEY `index_serial_no_warehouse_id` (`serial_no`,`warehouse_id`)
) COMMENT='库存处理表';


CREATE TABLE `doctor_warehouse_stock_monthly` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪场id',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `balance_quantity` decimal(23,2) NOT NULL DEFAULT '0.00' COMMENT '余量',
  `balance_amount` decimal(23,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `settlement_date` date DEFAULT NULL COMMENT '会计年月',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `warehouse_id_year_month_material_id_index` (`warehouse_id`,`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COMMENT='仓库物料月度结余表';



CREATE TABLE `doctor_warehouse_material_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `material_handle_id` bigint(20) NOT NULL COMMENT '物料处理编号',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `warehouse_type` smallint(6) NOT NULL COMMENT '仓库类型',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名',
  `pig_barn_id` bigint(20) NOT NULL COMMENT '领用猪舍编号',
  `pig_barn_name` varchar(64) DEFAULT NULL COMMENT '领用猪舍名称',
  `pig_group_id` bigint(20) DEFAULT NULL COMMENT '领用猪群编号',
  `pig_group_name` varchar(64) DEFAULT NULL COMMENT '领用猪群名称',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `apply_date` datetime DEFAULT NULL COMMENT '领用日期',
  `apply_staff_id` bigint(20) DEFAULT NULL COMMENT '领用人编号',
  `apply_staff_name` varchar(64) DEFAULT NULL COMMENT '领用人',
  `apply_year` smallint(12) NOT NULL COMMENT '领用年',
  `apply_month` tinyint(4) NOT NULL COMMENT '领用月',
  `material_name` varchar(64) DEFAULT NULL COMMENT '物料名称',
  `type` smallint(6) DEFAULT NULL COMMENT '物料类型，易耗品，原料，饲料，药品，饲料',
  `unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `quantity` decimal(23,2) NOT NULL COMMENT '数量',
  `unit_price` bigint(20) NOT NULL COMMENT '单价，单位分',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `apply_type` tinyint(4) NOT NULL COMMENT '领用类型。0猪舍，1猪群，2母猪',
  PRIMARY KEY (`id`),
  KEY `index_farm` (`farm_id`),
  KEY `index_warehouse` (`warehouse_id`)
) COMMENT='仓库物料领用表';

CREATE TABLE `doctor_warehouse_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `org_id` bigint(20) NOT NULL COMMENT '公司编号',
  `item_id` bigint(20) DEFAULT NULL COMMENT '物料类型编号',
  `item_name` varchar(128) DEFAULT NULL COMMENT '基础物料名称',
  `type` smallint(6) DEFAULT NULL COMMENT '基础物料类型',
  `name` varchar(128) NOT NULL COMMENT '物料名称',
  `code` varchar(64) NOT NULL COMMENT '编码,用于跨厂调拨',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态',
  `srm` varchar(32) DEFAULT NULL COMMENT '短码,用于查询',
  `vendor_id` bigint(20) DEFAULT NULL COMMENT '供应商编号',
  `unit` varchar(64) DEFAULT NULL COMMENT '单位',
  `specification` varchar(64) DEFAULT NULL COMMENT '规格',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='仓库物料表';
