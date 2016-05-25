-- ware house type
INSERT INTO `doctor_farm_ware_house_types` (`id`, `farm_id`, `farm_name`, `type`, `log_number`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'testFarm', 1, 200, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(2, 12345, 'testFarm', 2, 200, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(3, 12345, 'testFarm', 3, 200, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(4, 12345, 'testFarm', 4, 200, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17'),
	(5, 12345, 'testFarm', 5, 200, NULL, 1, 'creator', 1, 'updator', '2016-05-25 12:34:17', '2016-05-25 12:34:17');


-- create ware house
INSERT INTO `doctor_ware_houses` (`id`, `ware_house_name`, `farm_id`, `farm_name`, `manager_id`, `manager_name`, `address`, `type`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 'wareHouseName', 12345, 'farmIdName', 1, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-05-25 17:04:34', '2016-05-25 17:04:34'),
	(2, 'wareHouseName', 12345, 'farmIdName', 2, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-05-25 17:04:34', '2016-05-25 17:04:34'),
	(3, 'wareHouseName', 12345, 'farmIdName', 3, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-05-25 17:04:34', '2016-05-25 17:04:34'),
	(4, 'wareHouseName', 12345, 'farmIdName', 4, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-05-25 17:04:34', '2016-05-25 17:04:34'),
	(5, 'wareHouseName', 12345, 'farmIdName', 5, 'managerName', 'addressName', 1, NULL, 1, 'craetorName', 1, 'updateName', '2016-05-25 17:04:34', '2016-05-25 17:04:34');

-- ware house track info
INSERT INTO `doctor_ware_house_tracks` (`ware_house_id`, `farm_id`, `farm_name`, `manager_id`, `manager_name`, `material_lot_number`, `lot_number`, `is_default`, `extra`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'farmName', 1, 'managerName', NULL, 1000, 1, NULL, '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(2, 12345, 'farmName', 2, 'managerName', NULL, 1000, 1, NULL, '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(3, 12345, 'farmName', 3, 'managerName', NULL, 1000, 1, NULL, '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(4, 12345, 'farmName', 4, 'managerName', NULL, 1000, 1, NULL, '2016-05-25 18:43:53', '2016-05-25 18:43:53'),
	(5, 12345, 'farmName', 5, 'managerName', NULL, 1000, 1, NULL, '2016-05-25 18:43:53', '2016-05-25 18:43:53');


-- create material info
INSERT INTO `doctor_material_infos` (`id`, `farm_id`, `farm_name`, `type`, `material_name`, `remark`, `unit_group_id`, `unit_group_name`, `unit_id`, `unit_name`, `default_consume_count`, `price`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(null, 1, 'farmName', 1, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-05-25 17:22:43', '2016-05-25 17:22:43'),
	(null, 1, 'farmName', 2, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-05-25 17:22:43', '2016-05-25 17:22:43'),
	(null, 1, 'farmName', 3, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-05-25 17:22:43', '2016-05-25 17:22:43'),
	(null, 1, 'farmName', 4, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-05-25 17:22:43', '2016-05-25 17:22:43'),
	(null, 1, 'farmName', 5, 'materialName', 'remark', 1, 'unitgroupName', 2, 'unit_name', 100, 1000, NULL, 1, 'creatorName', 2, 'updatorName', '2016-05-25 17:22:43', '2016-05-25 17:22:43');

-- create material inf warehouse info
INSERT INTO `doctor_material_in_ware_houses` (`id`, `farm_id`, `farm_name`, `ware_house_id`, `ware_house_name`, `material_id`, `material_name`, `type`, `lot_number`, `unit_group_name`, `unit_name`, `extra`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'farmName', 1, 'warehouseName', 1, 'materialName', 1, 1000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-05-25 17:48:52', '2016-05-25 17:48:52'),
	(2, 12345, 'farmName', 2, 'warehouseName', 2, 'materialName', 2, 1000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-05-25 17:48:52', '2016-05-25 17:48:52'),
	(3, 12345, 'farmName', 3, 'warehouseName', 3, 'materialName', 3, 1000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-05-25 17:48:52', '2016-05-25 17:48:52'),
	(4, 12345, 'farmName', 4, 'warehouseName', 4, 'materialName', 4, 1000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-05-25 17:48:52', '2016-05-25 17:48:52'),
	(5, 12345, 'farmName', 5, 'warehouseName', 5, 'materialName', 5, 1000, 'groupName', 'unitName', NULL, 1, 'creatorName', 1, 'updatorName', '2016-05-25 17:48:52', '2016-05-25 17:48:52');

