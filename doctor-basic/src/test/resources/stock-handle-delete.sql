--单据删除测试模拟数据
insert into doctor_warehouse_stock_handle (id)values (1),(2),(3);
insert into doctor_warehouse_material_handle(id,stock_handle_id,warehouse_id,material_id,type,quantity,warehouse_name,material_name,other_transfer_handle_id,handle_year,handle_month)
values(1,1,33,87,1,5,'一号大仓','稻草',null,2017,11),(2,1,33,87,7,7,'一号大仓','稻草',null,2017,11),(3,1,33,89,9,1,'一号大仓','小麦',null,2017,11),(4,1,33,87,2,1,'一号大仓','稻草',null,2017,11),(5,1,33,88,2,1,'一号大仓','石头',null,2017,11)
,(6,2,33,87,10,4,'一号大仓','稻草',7,2017,11),(7,null,34,87,9,4,'二号大仓','稻草',6,2017,11),(8,3,34,87,2,3,'二号大仓','稻草',null,2017,11);
insert into doctor_warehouse_stock(warehouse_id,sku_id,quantity)values(33,87,10),(33,88,0),(33,89,20),(34,87,5);
insert into doctor_warehouse_sku(id,unit)values (87,'千克'),(88,'克'),(89,'个');
insert into doctor_warehouse_handle_detail(material_purchase_id,material_handle_id,quantity)values (1,6,1),(2,6,3),(3,7,4),(4,8,2);
insert into doctor_warehouse_purchase(id,quantity,handle_quantity,handle_finish_flag)values (1,200,1,1),(2,100,30,1),(3,20,0,1),(4,2,2,0);
insert into doctor_warehouse_stock_monthly(warehouse_id,material_id,handle_year,handle_month,balance_quantity)values (34,87,2017,11,10);
insert into doctor_warehouse_material_apply(material_handle_id,apply_type)values (8,0),(8,1)

