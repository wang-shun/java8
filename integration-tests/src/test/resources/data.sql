INSERT INTO `parana_back_categories`
(`pid`,`name`,`level`,`status`,`has_children`,`has_spu`,`outer_id`,`created_at`,`updated_at`)
VALUES
  (0, 'back-level1-cat1',1, 1, FALSE ,FALSE ,null,now(),now()),
  (0, 'back-level1-cat2',1, 1, FALSE ,FALSE ,null,now(),now()),
  (0, 'back-level1-cat3',1, 1, FALSE ,FALSE ,null,now(),now());

INSERT INTO `parana_category_attributes`
(`category_id`,  `attr_key`, `group`, `index`, `status`, `attr_metas_json`, `attr_vals_json`, `created_at`,`updated_at`)
VALUES
  (1, '颜色', '基本参数', 0,1, '{"SKU_CANDIDATE":"true","REQUIRED":"true","IMPORTANT":"false","VALUE_TYPE":"STRING","USER_DEFINED":"false"}', '["红色","黑色","绿色","黄色"]', now(), now()),
  (1, '尺码', '基本参数', 1, 1, '{"SKU_CANDIDATE":"true","REQUIRED":"true","IMPORTANT":"false","VALUE_TYPE":"NUMBER_cm","USER_DEFINED":"true"}', '["110","120"]', now(), now()),
  (1, '等级', '其他参数', 2, 1, '{"REQUIRED":"false","IMPORTANT":"false","USER_DEFINED":"false"}', '["高","中","低"]', now(), now()),
  (1, '重量', '其他参数', 2, 1, '{"REQUIRED":"false","IMPORTANT":"false","VALUE_TYPE":"NUMBER","USER_DEFINED":"true"}', null, now(), now()),
  (1, '产地', '其他参数', 2, 1, '{"REQUIRED":"true","IMPORTANT":"false","USER_DEFINED":"true"}', null, now(), now());

INSERT INTO `parana_front_categories`
(`pid`,`name`,`level`,`has_children`,`created_at`,`updated_at`)
VALUES
  (0, 'front-level1-cat1',1,  FALSE ,now(),now()),
  (0, 'front-level1-cat2',1,  FALSE ,now(),now()),
  (0, 'front-level1-cat3',1,  FALSE ,now(),now());


-- init category binding table
INSERT INTO
  parana_category_bindings (`front_category_id`, `back_category_id`, `created_at`, `updated_at`)
VALUES
  (1, 1, now(), now()),
  (1, 2, now(), now()),
  (2, 3, now(), now()),
  (3, 2, now(), now());

-- init shop_category_items table

INSERT INTO
  parana_shop_category_items
  (`shop_id`, `item_id`, `item_name`, `shop_category_id`, `shop_category_name`, `created_at`, `updated_at`)
VALUES
  (1, 1, 'item1', 1, 'cat1',now(), now()),
  (1, 2, 'item2', 2, 'cat2', now(), now()),
  (1, 3, 'item3', 1, 'cat1', now(), now()),
  (-1, 1, 'foobar',-1, 'barfoo', now(), now());

INSERT INTO
  parana_brands
  (`name`, `en_name`, `en_cap`, `logo`, `description`, `status`, `created_at`, `updated_at`)
  VALUES
    ('nike','nike','N',null,'nike is good',1, now(), now());



INSERT INTO `parana_spus` (`id`, `spu_code`, `category_id`, `brand_id`, `brand_name`, `name`,
                           `main_image`, `low_price`, `high_price`, `stock_type`, `stock_quantity`, `status`,
                           `advertise`, `specification`, `type`, `reduce_stock_type`, `extra_json`,
                           `spu_info_md5`, `created_at`, `updated_at`)
VALUES
  (1,NULL,1,1,'nike','testSpu1','http://taobao.com',6000,6000,0,24,1,NULL,NULL,1,1,'{"unit":"双"}',
   'c268a27a7fcbd244f413875d34421ea7',now(),now());


INSERT INTO `parana_spu_details` (`spu_id`, `images_json`, `detail`, `packing_json`, `service`, `created_at`, `updated_at`)
VALUES
  (1,'[{"name":"xx","url":"http://xx0oo.com"}]','i am detail','{"包邮":"true"}',NULL,now(),now());


INSERT INTO `parana_spu_attributes` (`spu_id`, `sku_attributes`, `other_attributes`, `created_at`, `updated_at`)
VALUES
  (1,'[{"attrKey":"尺码","skuAttributes":[{"attrKey":"尺码","attrVal":"120","unit":"cm"}]}]',
   '[{"group":"其他参数","otherAttributes":[{"attrKey":"产地","attrVal":"杭州"}]}]',now(),now());

INSERT INTO `parana_sku_templates` (`id`, `sku_code`, `spu_id`, `specification`, `status`,
                                    `image`, `thumbnail`, `name`, `extra_price_json`, `price`, `attrs_json`,
                                    `stock_type`, `stock_quantity`, `extra`, `created_at`, `updated_at`)
VALUES
  (1,'skuCode1',1,'ZUC-CW000-RED',1,'image1','thumbnail1','sku1','{"originPrice":3,"platformPrice":4}',
     6000,'[{"attrKey":"尺码","attrVal":"120","unit":"cm"}]',0,830869938,NULL,now(),now());




INSERT INTO
  parana_shops
  (`outer_id`, `user_id`, `user_name`, `name`, `status`, `type`, `phone`, `business_id`,
   `image_url`, `address`, `extra_json`, `tags_json`, `created_at`, `updated_at`)
VALUES
  ('outer_shop_xx',1,'jlchen', 'my shop name', 1, 1, '18611111111', 1,
                   'https://aecpm.alicdn.com/simba/img/TB1iOQELpXXXXa8aXXXSutbFXXX.jpg',
                   '杭州市滨江区', '{"hello":"jlchen"}','{"niubi":"man"}', now(),now());

INSERT INTO
  parana_users
  (`name`, `email`, `mobile`, `password`, `type`, `status`, `roles_json`, `extra_json`, `tags_json`, `created_at`, `updated_at`)
VALUES
  ('jlchen','i@terminus.io', '18888888888', '9f8c@a97758b955efdaf60fe4', 2, 1, null, '{"seller":"haha"}', '{"good":"man"}', now(), now());

-- init item and sku
INSERT INTO `parana_items` (`category_id`, `shop_id`, `shop_name`, `brand_id`, `brand_name`, `name`, `main_image`, `low_price`,
`high_price`, `stock_type`, `stock_quantity`, `sale_quantity`, `status`, `on_shelf_at`, `reduce_stock_type`, `extra_json`, `created_at`, `updated_at`)
VALUES
	(1, 1, 'Zcy Test Shop', 1, 'nike', 'A Test Item', 'http://zcy-test.img-cn-hangzhou.aliyuncs.com/users/2/20160114110114277.jpg', 18000,
	 19000, 0, 198, 0, 1, now(), 1, '{"unit":"件"}', now(), now());

INSERT INTO `parana_skus` (id, `item_id`, `shop_id`, `status`, `specification`, `outer_sku_id`, `outer_shop_id`, `image`, `name`,
`extra_price_json`, `price`, `attrs_json`, `stock_type`, `stock_quantity`, `extra`, `created_at`, `updated_at`, `thumbnail`)
VALUES
	(1, 1, 1, 1, NULL, NULL, NULL, NULL, NULL,
	'{"origin_price":"20000"}', 18000, '[{"attrKey": "长度", "attrVal": "18cm"}]', 0, 99, NULL, now(), now(), NULL),
	(2, 1, 1, 1, NULL, NULL, NULL, NULL, NULL,
	'{"origin_price":"22000"}', 20000, '[{"attrKey": "长度", "attrVal": "19cm"}]', 0, 99, NULL, now(), now(), NULL),
	(3, 1, 1, -1, NULL, NULL, NULL, NULL, NULL,
	'{"origin_price":"25000"}', 25000, '[{"attrKey": "长度", "attrVal": "20cm"}]', 0, 99, NULL, now(), now(), NULL);




INSERT INTO `parana_order_action_instances` (`id`, `node_instance_id`, `action`, `display`, `type`, `belong_user_types`, `name`, `desc`, `created_at`, `updated_at`)
VALUES
	(1, 1, 'orderPrePayAction', 1, 1, '[1]', '支付', '店铺订单支付', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(2, 2, 'orderDeliveAction', 1, 1, '[2]', '发货', '店铺订单发货', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(3, 3, 'orderConfirmAction', 1, 1, '[1]', '确认收货', '店铺订单确认收货', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(4, 1, 'orderBuyerCancelAction', 1, 1, '[1]', '买家取消订单', '店铺订单买家取消订单', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(5, 1, 'orderSellerCancelAction', 1, 1, '[2]', '卖家取消订单', '店铺订单卖家取消订单', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(17, 16, 'skuOrderApplyRefundAction', 1, 1, '[1]', '申请退款', 'sku订单申请退款', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(18, 29, 'skuOrderPreRefundAction', 1, 1, '[2]', '同意退款,生成退款地址', 'sku退款单同意退款', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(19, 29, 'skuOrderRefuseRefundAction', 1, 1, '[2]', '拒绝退款', 'sku退款单拒绝退款', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(20, 17, 'skuOrderApplyReturnsAction', 1, 1, '[1]', '申请退货', 'sku订单申请退货', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(21, 32, 'skuOrderAgreeReturnsAction', 1, 1, '[2]', '同意退货', 'sku退款单同意退货', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(22, 32, 'skuOrderRefuseReturnsAction', 1, 1, '[2]', '拒绝退货', 'sku退款单拒绝退货', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(23, 33, 'skuOrderBuyerDeliveReturnsAction', 1, 1, '[1]', '买家发退还货物', 'sku退款单买家发出退还货物', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(24, 35, 'skuOrderPreReturnsAction', 1, 1, '[2]', '卖家确认收到退还货物,发起退款', 'sku退款单卖家确认收到退款货物', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(25, 29, 'skuOrderUndoRefundAction', 1, 1, '[1]', '撤销退款申请', 'sku退款单撤销退款申请', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(26, 32, 'skuOrderUndoReturnsAfterApplyAction', 1, 1, '[1]', '撤销退货申请', 'sku退款单撤销退货申请', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(27, 33, 'skuOrderUndoReturnsAfterAgreeAction', 1, 1, '[1]', '撤销退货申请', 'sku退款单撤销退货申请', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(28, 1, 'orderPaidAction', 0, 1, '[1]', '支付回调动作', '店铺订单支付回调动作，由第三方支付平台触发', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(29, 29, 'skuOrderRefundCallBackAction', 0, 1, '[2]', '退款回调', 'sku退款单退款回调', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(30, 35, 'skuOrderReturnsCallBackAction', 0, 1, '[2]', '退货退款回调', 'sku退款单退货退款回调', '2016-03-11 15:38:49', '2016-03-11 15:38:49'),
	(31, 23, 'skuOrderApplyRefundAction', 1, 1, '[1]', '申请退款, 退款被拒绝后的再次申请退款', 'sku订单申请退款', '2016-03-11 15:38:49', '2016-03-11 15:38:49');


INSERT INTO `parana_order_node_instances` (`id`, `fid`, `first_node`, `names`, `desc`, `created_at`, `updated_at`)
VALUES
	(1, 1, 1, '{\"buyer\":\"等待买家付款\",\"seller\":\"等待买家付款\",\"admin\":\"等待买家付款\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(2, 1, 0, '{\"buyer\":\"等待卖家发货\",\"seller\":\"待发货\",\"admin\":\"等待卖家发货\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(3, 1, 0, '{\"buyer\":\"卖家已发货\",\"seller\":\"已发货\",\"admin\":\"卖家已发货\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(4, 1, 0, '{\"buyer\":\"交易完成\",\"seller\":\"交易完成\",\"admin\":\"交易完成\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(5, 1, 0, '{\"buyer\":\"订单已取消\",\"seller\":\"买家取消订单\",\"admin\":\"买家取消订单\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(6, 1, 0, '{\"buyer\":\"卖家取消订单\",\"seller\":\"订单已取消\",\"admin\":\"卖家取消订单\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(7, 1, 0, '{\"buyer\":\"已申请退款\",\"seller\":\"买家申请退款\",\"admin\":\"买家申请退款\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(8, 1, 0, '{\"buyer\":\"卖家同意退款\",\"seller\":\"同意退款\",\"admin\":\"卖家同意退款\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(9, 1, 0, '{\"buyer\":\"卖家拒绝退款\",\"seller\":\"拒绝退款\",\"admin\":\"卖家拒绝退款\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(10, 1, 0, '{\"buyer\":\"已申请退货\",\"seller\":\"买家申请退货\",\"admin\":\"买家申请退货\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(11, 1, 0, '{\"buyer\":\"卖家同意退货\",\"seller\":\"同意退货\",\"admin\":\"卖家同意退货\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(12, 1, 0, '{\"buyer\":\"卖家拒绝退货\",\"seller\":\"拒绝退货\",\"admin\":\"卖家拒绝退货\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(13, 1, 0, '{\"buyer\":\"已发退还货物\",\"seller\":\"买家已发退还货物\",\"admin\":\"买家已发退还货物\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(14, 1, 0, '{\"buyer\":\"卖家已确认收货,退款成功\",\"seller\":\"确认收货,退款成功\",\"admin\":\"卖家已确认收货,退款成功\"}', '店铺订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(15, 1, 0, '{\"buyer\":\"等待买家付款\",\"seller\":\"等待买家付款\",\"admin\":\"等待买家付款\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(16, 1, 0, '{\"buyer\":\"等待卖家发货\",\"seller\":\"待发货\",\"admin\":\"等待卖家发货\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(17, 1, 0, '{\"buyer\":\"卖家已发货\",\"seller\":\"已发货\",\"admin\":\"卖家已发货\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(18, 1, 0, '{\"buyer\":\"交易完成\",\"seller\":\"交易完成\",\"admin\":\"交易完成\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(19, 1, 0, '{\"buyer\":\"订单已取消\",\"seller\":\"买家取消订单\",\"admin\":\"买家取消订单\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(20, 1, 0, '{\"buyer\":\"卖家取消订单\",\"seller\":\"订单已取消\",\"admin\":\"卖家取消订单\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(21, 1, 0, '{\"buyer\":\"已申请退款\",\"seller\":\"买家申请退款\",\"admin\":\"买家申请退款\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(22, 1, 0, '{\"buyer\":\"卖家同意退款\",\"seller\":\"同意退款\",\"admin\":\"卖家同意退款\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(23, 1, 0, '{\"buyer\":\"卖家拒绝退款\",\"seller\":\"拒绝退款\",\"admin\":\"卖家拒绝退款\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(24, 1, 0, '{\"buyer\":\"已申请退货\",\"seller\":\"买家申请退货\",\"admin\":\"买家申请退货\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(25, 1, 0, '{\"buyer\":\"卖家同意退货\",\"seller\":\"同意退货\",\"admin\":\"卖家同意退货\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(26, 1, 0, '{\"buyer\":\"卖家拒绝退货\",\"seller\":\"拒绝退货\",\"admin\":\"卖家拒绝退货\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(27, 1, 0, '{\"buyer\":\"已发退还货物\",\"seller\":\"买家已发退还货物\",\"admin\":\"买家已发退还货物\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(28, 1, 0, '{\"buyer\":\"卖家已确认收货,退款成功\",\"seller\":\"确认收货,退款成功\",\"admin\":\"卖家已确认收货,退款成功\"}', 'sku订单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(29, 1, 0, '{\"buyer\":\"已申请退款\",\"seller\":\"买家申请退款\",\"admin\":\"买家申请退款\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(30, 1, 0, '{\"buyer\":\"卖家同意退款\",\"seller\":\"同意退款\",\"admin\":\"卖家同意退款\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(31, 1, 0, '{\"buyer\":\"卖家拒绝退款\",\"seller\":\"拒绝退款\",\"admin\":\"卖家拒绝退款\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(32, 1, 0, '{\"buyer\":\"已申请退货\",\"seller\":\"买家申请退货\",\"admin\":\"买家申请退货\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(33, 1, 0, '{\"buyer\":\"卖家同意退货\",\"seller\":\"同意退货\",\"admin\":\"卖家同意退货\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(34, 1, 0, '{\"buyer\":\"卖家拒绝退货\",\"seller\":\"拒绝退货\",\"admin\":\"卖家拒绝退货\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(35, 1, 0, '{\"buyer\":\"已发退还货物\",\"seller\":\"买家已发退还货物\",\"admin\":\"买家已发退还货物\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(36, 1, 0, '{\"buyer\":\"卖家已确认收货,退款成功\",\"seller\":\"确认收货,退款成功\",\"admin\":\"卖家已确认收货,退款成功\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(37, 1, 0, '{\"buyer\":\"已撤销退款\",\"seller\":\"买家撤销退款\",\"admin\":\"买家撤销退款\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(38, 1, 0, '{\"buyer\":\"已撤销退货\",\"seller\":\"买家撤销退货\",\"admin\":\"买家撤销退货\"}', 'sku退款单在线支付', '2016-04-01 14:36:34', '2016-04-01 14:36:34'),
	(39, 1, 0, '{\"buyer\":\"卖家同意退款\",\"seller\":\"同意退款\",\"admin\":\"卖家同意退款\"}', '店铺订单在线支付,由skuOrder退款触发', '2016-04-01 14:36:34', '2016-04-01 14:36:34');


INSERT INTO `parana_order_transfer_rules` (`id`, `start_node_instance_id`, `end_node_instance_id`, `created_at`, `updated_at`)
VALUES
	(15, 1, 2, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(16, 2, 3, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(17, 3, 4, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(18, 1, 5, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(19, 1, 6, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(20, 2, 39, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(21, 3, 41, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(22, 15, 16, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(23, 16, 17, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(24, 17, 18, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(25, 15, 19, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(26, 15, 20, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(27, 17, 42, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(28, 16, 43, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(29, 29, 30, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(30, 29, 31, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(31, 29, 37, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(32, 32, 33, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(33, 33, 35, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(34, 35, 36, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(35, 32, 34, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(36, 32, 38, '2016-03-25 17:56:35', '2016-03-25 17:56:35'),
	(37, 33, 40, '2016-03-25 17:56:35', '2016-03-25 17:56:35');


INSERT INTO `parana_shop_orders` (`id`, `parent_id`, `parent_type`, `flow_id`, `node_instance_id`, `next_action_instance_ids`, `type`, `out_id`, `out_from`, `extra_json`, `tags_json`, `origin_fee`, `fee`, `discount`, `ship_fee`, `ship_fee_discount`, `integral`, `balance`, `sale_tax`, `ship_fee_sale_tax`, `buyer_id`, `buyer_name`, `out_buyer_id`, `shop_id`, `shop_name`, `out_shop_id`, `company_id`, `deliver_type`, `pay_type`, `channel`, `has_refund`, `created_at`, `updated_at`)
VALUES
	(71, -1, -1, 1, 1, '[{\"type\":1,\"actionInstanceIds\":[1,4]},{\"type\":2,\"actionInstanceIds\":[5]}]', 2, NULL, NULL, NULL, NULL, 18000, 18000, 0, 0, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, 1, 'my shop name', NULL, NULL, 1, 1, 1, NULL, '2016-04-04 10:33:54', '2016-04-04 10:33:54');

INSERT INTO `parana_sku_orders` (`id`, `parent_id`, `parent_type`, `flow_id`, `node_instance_id`, `next_action_instance_ids`, `type`, `out_id`, `out_from`, `extra_json`, `tags_json`, `origin_fee`, `fee`, `discount`, `ship_fee`, `ship_fee_discount`, `integral`, `balance`, `sale_tax`, `ship_fee_sale_tax`, `buyer_id`, `buyer_name`, `out_buyer_id`, `shop_id`, `shop_name`, `out_shop_id`, `company_id`, `sku_id`, `out_sku_id`, `sku_attributes`, `item_id`, `item_snapshot_id`, `item_name`, `item_image`, `out_item_id`, `quantity`, `deliver_type`, `pay_type`, `channel`, `has_refund`, `created_at`, `updated_at`)
VALUES
	(65, 71, 2, 1, 15, '[]', 3, NULL, NULL, NULL, NULL, 18000, 18000, 0, 0, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, 1, 'my shop name', NULL, NULL, 1, NULL, '[{\"attrKey\": \"长度\", \"attrVal\": \"18cm\"}]', 1, NULL, 'A Test Item', 'http://zcy-test.img-cn-hangzhou.aliyuncs.com/users/2/20160114110114277.jpg', NULL, 1, NULL, 1, 1, NULL, '2016-04-04 10:33:54', '2016-04-04 10:33:54');
