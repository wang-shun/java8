INSERT INTO `doctor_pigs` (`id`, `org_id`, `org_name`, `farm_id`, `farm_name`, `out_id`, `pig_code`, `pig_type`, `pig_father_id`, `pig_mother_id`, `source`, `birth_date`, `birth_weight`, `in_farm_date`, `in_farm_day_age`, `init_barn_id`, `init_barn_name`, `breed_id`, `breed_name`, `genetic_id`, `genetic_name`, `extra`, `remark`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, NULL, 12345, NULL, NULL, 'pigCode', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(2, 12345, NULL, 12345, NULL, NULL, 'pigCode', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(3, 12345, NULL, 12345, NULL, NULL, 'pigCode', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(4, 12345, NULL, 12345, NULL, NULL, 'pigCode', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(5, 12345, NULL, 12345, NULL, NULL, 'pigCode', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `doctor_pig_tracks` (`id`, `farm_id`, `pig_id`, `status`, `current_barn_id`, `current_barn_name`, `weight`, `out_farm_date`, `rel_event_id`, `extra`, `current_parity`, `remark`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(6, 12345, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(7, 12345, 2, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(8, 12345, 3, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(9, 12345, 4, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(10, 12345, 5, 5, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO `doctor_pig_events` (`id`, `org_id`, `org_name`, `farm_id`, `farm_name`, `pig_id`, `pig_code`, `event_at`, `type`, `kind`, `name`, `desc`, `barn_id`, `barn_name`, `rel_event_id`, `out_id`, `extra`, `remark`, `creator_id`, `creator_name`, `updator_id`, `updator_name`, `created_at`, `updated_at`)
VALUES
	(1, 12345, 'orgName', 12345, 'farmName', 1, 'pigCode', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(2, 12345, 'orgName', 12345, 'farmName', 2, 'pigCode', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(3, 12345, 'orgName', 12345, 'farmName', 3, 'pigCode', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(4, 12345, 'orgName', 12345, 'farmName', 4, 'pigCode', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(5, 12345, 'orgName', 12345, 'farmName', 5, 'pigCode', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

