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
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `material_id` bigint(20) NOT NULL COMMENT '物料编号',
  `handle_year` smallint(6) NOT NULL COMMENT '处理年',
  `handle_month` tinyint(2) NOT NULL COMMENT '处理月',
  `balance_quantity` decimal(23,2) NOT NULL DEFAULT '0.00' COMMENT '余量',
  `balacne_amount` bigint(20) NOT NULL DEFAULT '0' COMMENT '余额',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `handle_date` date DEFAULT NULL COMMENT '处理日期',
  PRIMARY KEY (`id`),
  KEY `warehouse_id_year_month_material_id_index` (`warehouse_id`,`handle_year`,`handle_month`,`material_id`)
) COMMENT='仓库物料月度结余表';


CREATE TABLE `doctor_warehouse_material_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `material_handle_id` bigint(20) NOT NULL COMMENT '物料处理编号',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司ID',
  `farm_id` bigint(20) DEFAULT NULL COMMENT '猪厂编号',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库编号',
  `warehouse_type` smallint(6) NOT NULL COMMENT '仓库类型',
  `warehouse_name` varchar(64) DEFAULT NULL COMMENT '仓库名',
  `pig_barn_id` bigint(20) NOT NULL COMMENT '领用猪舍编号',
  `pig_barn_name` varchar(64) DEFAULT NULL COMMENT '领用猪舍名称',
  `pig_type` smallint(6) DEFAULT NULL COMMENT '猪舍类型',
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
) ;


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
  PRIMARY KEY (`id`),
  KEY `idx_doctor_barns_farm_id` (`farm_id`)
) COMMENT='猪舍表';

CREATE TABLE `doctor_farms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_code` varchar(64) DEFAULT NULL COMMENT '猪场号',
  `org_id` bigint(20) DEFAULT NULL COMMENT '公司id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `province_id` int(11) DEFAULT NULL COMMENT '所在省id',
  `province_name` varchar(64) DEFAULT NULL COMMENT '所在省',
  `city_id` int(11) DEFAULT NULL COMMENT '所在城市id',
  `city_name` varchar(64) DEFAULT NULL COMMENT '所在城市',
  `district_id` int(11) DEFAULT NULL COMMENT '区id',
  `district_name` varchar(64) DEFAULT NULL COMMENT '区名称',
  `detail_address` varchar(256) DEFAULT NULL COMMENT '区之后的具体地址',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `source` tinyint(4) DEFAULT NULL COMMENT '来源,1:软件录入,2:excel导入,3:旧软件迁移，',
  `extra` text COMMENT '附加字段',
  `is_intelligent` smallint(6) DEFAULT '0' COMMENT '是否是智能猪舍（物联网使用默认是0）1->智能猪场 0不是猪场',
  `is_weak` smallint(6) DEFAULT '1' COMMENT '弱仔数是否作为活仔数, 1->作为活仔数 0不作为活仔数，默认为1',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_farms_org_id` (`org_id`)
)  COMMENT='猪场表';

CREATE TABLE `doctor_orgs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `mobile` varchar(16) DEFAULT NULL COMMENT '手机号码',
  `license` varchar(512) DEFAULT NULL COMMENT '营业执照复印件图片地址',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部id',
  `extra` text COMMENT '附加字段',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  `parent_id` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT='公司表';

CREATE TABLE `doctor_filed_urls` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL COMMENT '字段名称',
  `url` varchar(512) NOT NULL COMMENT '字段跳转url',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_name` (`name`)
) COMMENT='字段跳转url';


CREATE TABLE `doctor_pig_events` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `org_id` bigint(20) unsigned DEFAULT NULL COMMENT '公司Id',
  `org_name` varchar(64) DEFAULT NULL COMMENT '公司名称',
  `farm_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪场Id',
  `farm_name` varchar(64) DEFAULT NULL COMMENT '猪场名称',
  `pig_id` bigint(20) unsigned DEFAULT NULL COMMENT '猪Id',
  `pig_code` varchar(64) DEFAULT NULL COMMENT '猪Code',
  `is_auto` smallint(6) DEFAULT NULL COMMENT '是否是自动生成事件, 0 不是, 1 是',
  `event_at` date NOT NULL COMMENT '事件时间',
  `type` int(11) DEFAULT NULL COMMENT '事件类型',
  `kind` int(11) DEFAULT NULL COMMENT '事件猪类型， 公猪， 母猪， 仔猪',
  `name` varchar(64) DEFAULT NULL COMMENT '事件名称',
  `desc` varchar(512) DEFAULT NULL COMMENT '事件描述',
  `barn_id` bigint(20) unsigned DEFAULT NULL COMMENT '事件地点',
  `barn_name` varchar(64) DEFAULT NULL COMMENT '地点名称',
  `barn_type` tinyint(4) DEFAULT NULL COMMENT '猪舍类型',
  `rel_event_id` bigint(20) DEFAULT NULL COMMENT '关联事件Id',
  `rel_group_event_id` bigint(20) DEFAULT NULL COMMENT '关联猪群事件id(比如转种猪事件)',
  `rel_pig_event_id` bigint(20) DEFAULT NULL COMMENT '关联猪事件id(比如拼窝事件)',
  `change_type_id` bigint(20) DEFAULT NULL COMMENT '变动类型id',
  `basic_id` bigint(20) DEFAULT NULL COMMENT '基础数据id(流产原因id,疾病id,防疫项目id)',
  `basic_name` varchar(32) DEFAULT NULL COMMENT '基础数据名(流产原因,疾病,防疫)',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户id',
  `customer_name` varchar(64) DEFAULT NULL COMMENT '客户名',
  `vaccination_id` bigint(20) DEFAULT NULL COMMENT '疫苗',
  `vaccination_name` varchar(32) DEFAULT NULL COMMENT '疫苗名称',
  `price` bigint(20) DEFAULT NULL COMMENT '销售单价(分)',
  `amount` bigint(20) DEFAULT NULL COMMENT '销售总额(分)',
  `quantity` int(11) DEFAULT NULL COMMENT '数量(拼窝数量,被拼窝数量,仔猪变动数量)',
  `weight` double DEFAULT NULL COMMENT '重量(变动重量)',
  `pig_status_before` smallint(6) DEFAULT NULL COMMENT '事件发生之前猪的状态',
  `pig_status_after` smallint(6) DEFAULT NULL COMMENT '事件发生之后猪的状态',
  `parity` int(11) DEFAULT NULL COMMENT '事件发生母猪的胎次），记住是前一个事件是妊娠检查事件',
  `is_impregnation` smallint(6) DEFAULT NULL COMMENT '是否可以进行受胎统计，就是妊娠检查阳性之后这个字段为true 0否1是',
  `is_delivery` smallint(6) DEFAULT NULL COMMENT '是否可以进行分娩，就是分娩事件之后这个字段为true 0否1是',
  `preg_days` int(11) DEFAULT NULL COMMENT '孕期，分娩时候统计',
  `feed_days` int(11) DEFAULT NULL COMMENT '哺乳天数，断奶事件发生统计',
  `preg_check_result` smallint(6) DEFAULT NULL COMMENT '妊娠检查结果，从extra中拆出来',
  `dp_npd` int(11) DEFAULT '0' COMMENT '断奶到配种的非生产天数',
  `pf_npd` int(11) DEFAULT '0' COMMENT '配种到返情非生产天数',
  `pl_npd` int(11) DEFAULT '0' COMMENT '配种到流产非生产天数',
  `ps_npd` int(11) DEFAULT '0' COMMENT '配种到死亡非生产天数',
  `py_npd` int(11) DEFAULT '0' COMMENT '配种到阴性非生产天数',
  `pt_npd` int(11) DEFAULT '0' COMMENT '配种到淘汰非生产天数',
  `jp_npd` int(11) DEFAULT '0' COMMENT '配种到配种非生产天数',
  `npd` int(11) DEFAULT '0' COMMENT '非生产天数 前面的总和',
  `group_id` bigint(20) DEFAULT NULL COMMENT '哺乳状态的母猪关联的猪群id',
  `farrow_weight` double DEFAULT NULL COMMENT '分娩总重(kg)',
  `live_count` int(11) DEFAULT NULL COMMENT '活仔数',
  `health_count` int(11) DEFAULT NULL COMMENT '键仔数',
  `weak_count` int(11) DEFAULT NULL COMMENT '弱仔数',
  `mny_count` int(11) DEFAULT NULL COMMENT '木乃伊数',
  `jx_count` int(11) DEFAULT NULL COMMENT '畸形数',
  `dead_count` int(11) DEFAULT NULL COMMENT '死胎数',
  `black_count` int(11) DEFAULT NULL COMMENT '黑胎数',
  `wean_count` int(11) DEFAULT NULL COMMENT '断奶数',
  `wean_avg_weight` double DEFAULT NULL COMMENT '断奶均重(kg)',
  `current_mating_count` double DEFAULT NULL COMMENT '当前配种次数',
  `check_date` datetime DEFAULT NULL COMMENT '检查时间',
  `matting_date` datetime DEFAULT NULL COMMENT '配种时间',
  `farrowing_date` datetime DEFAULT NULL COMMENT '分娩时间',
  `abortion_date` datetime DEFAULT NULL COMMENT '流产时间',
  `partwean_date` datetime DEFAULT NULL COMMENT '断奶时间',
  `judge_preg_date` date DEFAULT NULL COMMENT '预产期',
  `doctor_mate_type` smallint(6) DEFAULT NULL COMMENT '配种类型',
  `mate_type` tinyint(4) DEFAULT NULL COMMENT '配种类型(人工、自然)',
  `boar_code` varchar(64) DEFAULT NULL COMMENT '配种的公猪',
  `out_id` varchar(128) DEFAULT NULL COMMENT '外部Id',
  `status` tinyint(4) NOT NULL COMMENT '是否有效1：有效，0：正在处理， -1：无效',
  `event_source` tinyint(4) DEFAULT NULL COMMENT '事件来源,1、软件录入,2、excel导入,3、旧场迁移',
  `extra` text COMMENT '参考设计文档',
  `source` tinyint(4) DEFAULT NULL COMMENT '进场来源，1：本场，2：外购',
  `breed_id` bigint(20) DEFAULT NULL COMMENT '品种',
  `breed_name` varchar(32) DEFAULT NULL COMMENT '品种',
  `breed_type_id` bigint(20) DEFAULT NULL COMMENT '品系',
  `breed_type_name` varchar(32) DEFAULT NULL COMMENT '品系',
  `boar_type` tinyint(4) DEFAULT NULL COMMENT '公猪类型,1：活公猪，2：冷冻精液，3：新鲜精液',
  `remark` text COMMENT '备注信息',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人id',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人id',
  `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `updator_id` bigint(20) DEFAULT NULL COMMENT '创建人Id',
  `updator_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `doctor_pig_events_farm_id` (`farm_id`),
  KEY `doctor_pig_events_pig_id` (`pig_id`),
  KEY `doctor_pig_events_rel_event_id` (`rel_event_id`),
  KEY `doctor_pig_events_boar_code` (`boar_code`),
  KEY `doctor_pig_events_type` (`type`),
  KEY `doctor_pig_events_parity` (`parity`),
  KEY `idx_doctor_pig_events_rel_group_event_id` (`rel_group_event_id`),
  KEY `idx_doctor_pig_events_rel_pig_event_id` (`rel_pig_event_id`),
  KEY `idx_doctor_pig_events_group_id` (`group_id`),
  KEY `idx_doctor_pig_events_barn_id` (`barn_id`),
  KEY `idx_doctor_pig_events_event_at` (`event_at`),
  KEY `doctor_pig_events_barn_id` (`barn_id`)
)  COMMENT='用户设备信息表';

CREATE TABLE `doctor_report_materials` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sum_at` date NOT NULL,
  `sum_at_name` varchar(32) NOT NULL DEFAULT '',
  `date_type` tinyint(4) NOT NULL COMMENT '日期类型，1->日,2->周,3->月,4->季,5->年',
  `orz_id` bigint(20) NOT NULL COMMENT '组织ID',
  `orz_name` varchar(64) NOT NULL DEFAULT '' COMMENT '组织名称',
  `orz_type` tinyint(4) NOT NULL COMMENT '组织类型，1->集团,2->公司，3->猪场',
  `houbei_feed_amount` decimal(12,4) NOT NULL COMMENT '后备饲料金额',
  `houbei_feed_quantity` int(11) NOT NULL COMMENT '后备饲料数量',
  `houbei_material_amount` decimal(12,4) NOT NULL COMMENT '后备原料金额',
  `houbei_material_quantity` int(11) NOT NULL COMMENT '后备原料数量',
  `houbei_vaccination_amount` decimal(12,4) NOT NULL COMMENT '后备疫苗金额',
  `houbei_medicine_amount` decimal(12,4) NOT NULL COMMENT '后备兽药金额',
  `houbei_consume_amount` decimal(12,4) NOT NULL COMMENT '后备消耗品金额',
  `peihuai_feed_amount` decimal(12,4) NOT NULL COMMENT '配怀饲料金额',
  `peihuai_feed_quantity` int(11) NOT NULL COMMENT '配怀饲料数量',
  `peihuai_material_amount` decimal(12,4) NOT NULL COMMENT '配怀原料金额',
  `peihuai_material_quantity` int(11) NOT NULL COMMENT '配怀原料数量',
  `peihuai_vaccination_amount` decimal(12,4) NOT NULL COMMENT '配怀疫苗金额',
  `peihuai_medicine_amount` decimal(12,4) NOT NULL COMMENT '配怀兽药金额',
  `peihuai_consume_amount` decimal(12,4) NOT NULL COMMENT '配怀消耗品金额',
  `sow_feed_amount` decimal(12,4) NOT NULL COMMENT '产房母猪饲料金额',
  `sow_feed_quantity` int(11) NOT NULL COMMENT '产房母猪饲料数量',
  `sow_material_amount` decimal(12,4) NOT NULL COMMENT '产房母猪原料金额',
  `sow_material_quantity` int(11) NOT NULL COMMENT '产房母猪原料数量',
  `sow_vaccination_amount` decimal(12,4) NOT NULL COMMENT '产房母猪疫苗金额',
  `sow_medicine_amount` decimal(12,4) NOT NULL COMMENT '产房母猪兽药金额',
  `sow_consume_amount` decimal(12,4) NOT NULL COMMENT '产房母猪消耗品金额',
  `piglet_feed_amount` decimal(12,4) NOT NULL COMMENT '产房仔猪饲料金额',
  `piglet_feed_quantity` int(11) NOT NULL COMMENT '产房仔猪饲料数量',
  `piglet_material_amount` decimal(12,4) NOT NULL COMMENT '产房仔猪原料金额',
  `piglet_material_quantity` int(11) NOT NULL COMMENT '产房仔猪原料数量',
  `piglet_vaccination_amount` decimal(12,4) NOT NULL COMMENT '产房仔猪疫苗金额',
  `piglet_medicine_amount` decimal(12,4) NOT NULL COMMENT '产房仔猪兽药金额',
  `piglet_consume_amount` decimal(12,4) NOT NULL COMMENT '产房仔猪消耗品金额',
  `baoyu_feed_amount` decimal(12,4) NOT NULL COMMENT '保育饲料金额',
  `baoyu_feed_quantity` int(11) NOT NULL COMMENT '保育饲料数量',
  `baoyu_material_amount` decimal(12,4) NOT NULL COMMENT '保育原料金额',
  `baoyu_material_quantity` int(11) NOT NULL COMMENT '保育原料数量',
  `baoyu_vaccination_amount` decimal(12,4) NOT NULL COMMENT '保育疫苗金额',
  `baoyu_medicine_amount` decimal(12,4) NOT NULL COMMENT '保育兽药金额',
  `baoyu_consume_amount` decimal(12,4) NOT NULL COMMENT '保育消耗品金额',
  `yufei_feed_amount` decimal(12,4) NOT NULL COMMENT '育肥饲料金额',
  `yufei_feed_quantity` int(11) NOT NULL COMMENT '育肥饲料数量',
  `yufei_material_amount` decimal(12,4) NOT NULL COMMENT '育肥原料金额',
  `yufei_material_quantity` int(11) NOT NULL COMMENT '育肥原料数量',
  `yufei_vaccination_amount` decimal(12,4) NOT NULL COMMENT '育肥疫苗金额',
  `yufei_medicine_amount` decimal(12,4) NOT NULL COMMENT '育肥兽药金额',
  `yufei_consume_amount` decimal(12,4) NOT NULL COMMENT '育肥消耗品金额',
  `boar_feed_amount` decimal(12,4) NOT NULL COMMENT '公猪饲料金额',
  `boar_feed_quantity` int(11) NOT NULL COMMENT '公猪饲料数量',
  `boar_material_amount` decimal(12,4) NOT NULL COMMENT '公猪原料金额',
  `boar_material_quantity` int(11) NOT NULL COMMENT '公猪原料数量',
  `boar_vaccination_amount` decimal(12,4) NOT NULL COMMENT '公猪疫苗金额',
  `boar_medicine_amount` decimal(12,4) NOT NULL COMMENT '公猪兽药金额',
  `boar_consume_amount` decimal(12,4) NOT NULL COMMENT '公猪消耗品金额',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='物料消耗报表';

CREATE TABLE `doctor_report_efficiencies` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sum_at` date NOT NULL,
  `sum_at_name` varchar(32) NOT NULL DEFAULT '',
  `date_type` tinyint(8) NOT NULL COMMENT '日期类型，1->日,2->周,3->月,4->季,5->年',
  `orz_id` bigint(20) NOT NULL COMMENT '组织ID',
  `orz_name` varchar(64) NOT NULL DEFAULT '' COMMENT '组织名称',
  `orz_type` tinyint(4) NOT NULL COMMENT '组织类型，1->集团,2->公司，3->猪场',
  `npd` int(11) DEFAULT NULL COMMENT '非生产天数',
  `birth_per_year` int(11) DEFAULT NULL COMMENT '年生产胎次',
  `psy` int(11) DEFAULT NULL COMMENT 'psy',
  `pregnancy` int(11) NOT NULL COMMENT '妊娠期',
  `lactation` int(11) NOT NULL COMMENT '哺乳期',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)  COMMENT='效率报表';
