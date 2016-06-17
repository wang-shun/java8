alter table parana_user_profiles
modify column user_id  bigint(20) NOT NULL COMMENT '用户id',
modify column `province_id` bigint(20) NULL COMMENT '省id',
modify column `province` VARCHAR(100) NULL COMMENT '省',
modify column `avatar` VARCHAR(512) NULL COMMENT '头像';

alter table doctor_service_reviews
add column `real_name` VARCHAR (16) DEFAULT NULL COMMENT '用户申请服务时填写的真实姓名' after user_mobile;